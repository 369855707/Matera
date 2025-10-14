package com.maternity.service;

import com.maternity.dto.*;
import com.maternity.exception.ResourceNotFoundException;
import com.maternity.model.User;
import com.maternity.repository.UserRepository;
import com.maternity.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final WeChatAuthService weChatAuthService;
    private final VerificationCodeService verificationCodeService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager,
                      WeChatAuthService weChatAuthService, VerificationCodeService verificationCodeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.weChatAuthService = weChatAuthService;
        this.verificationCodeService = verificationCodeService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        return new AuthResponse(token, UserDTO.fromEntity(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Authenticate with username (can be email, phone, or username)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Find user by email or phone
        User user = userRepository.findByEmail(request.getUsername())
                .or(() -> userRepository.findByPhone(request.getUsername()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Use the same identifier that was used for login
        String identifier = user.getEmail() != null ? user.getEmail() : user.getPhone();
        String token = jwtTokenProvider.generateToken(identifier);

        return new AuthResponse(token, UserDTO.fromEntity(user));
    }

    @Transactional
    public AuthResponse loginWithWeChat(WeChatLoginRequest request) {
        log.info("Processing WeChat login for role: {}", request.getRole());

        // Step 1: Authenticate with WeChat and get user info
        WeChatUserInfo weChatUserInfo = weChatAuthService.authenticateWithWeChat(request.getCode());

        // Step 2: Find or create user
        User user = userRepository.findByWechatOpenId(weChatUserInfo.getOpenId())
                .orElseGet(() -> {
                    log.info("Creating new user from WeChat login: {}", weChatUserInfo.getOpenId());
                    return createUserFromWeChat(weChatUserInfo, request.getRole());
                });

        // Step 3: Update user info from WeChat (in case nickname or avatar changed)
        updateUserFromWeChat(user, weChatUserInfo);
        user = userRepository.save(user);

        // Step 4: Generate JWT token using WeChat OpenID as identifier
        String token = jwtTokenProvider.generateToken(user.getWechatOpenId());

        log.info("WeChat login successful for user: {}", user.getName());
        return new AuthResponse(token, UserDTO.fromEntity(user));
    }

    private User createUserFromWeChat(WeChatUserInfo weChatUserInfo, String roleString) {
        User user = new User();
        user.setName(weChatUserInfo.getNickname());
        user.setWechatOpenId(weChatUserInfo.getOpenId());
        user.setWechatUnionId(weChatUserInfo.getUnionId());
        user.setWechatNickname(weChatUserInfo.getNickname());
        user.setWechatAvatarUrl(weChatUserInfo.getAvatarUrl());
        user.setAvatar(weChatUserInfo.getAvatarUrl());

        // Parse role
        User.UserRole role;
        try {
            role = User.UserRole.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role provided: {}, defaulting to MOTHER", roleString);
            role = User.UserRole.MOTHER;
        }
        user.setRole(role);

        // WeChat users don't have email/password initially
        // They can add these later if needed
        return userRepository.save(user);
    }

    private void updateUserFromWeChat(User user, WeChatUserInfo weChatUserInfo) {
        // Update nickname and avatar if changed
        if (weChatUserInfo.getNickname() != null) {
            user.setWechatNickname(weChatUserInfo.getNickname());
            user.setName(weChatUserInfo.getNickname());
        }
        if (weChatUserInfo.getAvatarUrl() != null) {
            user.setWechatAvatarUrl(weChatUserInfo.getAvatarUrl());
            user.setAvatar(weChatUserInfo.getAvatarUrl());
        }
        if (weChatUserInfo.getUnionId() != null) {
            user.setWechatUnionId(weChatUserInfo.getUnionId());
        }
    }

    /**
     * Send phone verification code
     */
    public PhoneSendCodeResponse sendPhoneVerificationCode(PhoneSendCodeRequest request) {
        log.info("📱 Sending verification code to {} {}", request.getCountryCode(), request.getPhoneNumber());

        // Check rate limit
        if (verificationCodeService.isRateLimitExceeded(request.getCountryCode(), request.getPhoneNumber())) {
            throw new RuntimeException("Too many requests. Please wait before requesting another code.");
        }

        // Send code
        String code = verificationCodeService.sendCode(request.getCountryCode(), request.getPhoneNumber());

        log.info("✅ Verification code sent successfully: {}", code);
        return new PhoneSendCodeResponse(true, "Verification code sent successfully", 300);
    }

    /**
     * Verify phone code and login/register
     */
    @Transactional
    public AuthResponse verifyPhoneAndLogin(PhoneVerifyRequest request) {
        log.info("📱 Verifying phone {} {} with code {}",
                 request.getCountryCode(), request.getPhoneNumber(), request.getVerificationCode());

        // Verify code
        boolean isValid = verificationCodeService.verifyCode(
                request.getCountryCode(),
                request.getPhoneNumber(),
                request.getVerificationCode()
        );

        if (!isValid) {
            throw new RuntimeException("Invalid or expired verification code");
        }

        // Find or create user
        String phoneKey = request.getCountryCode() + request.getPhoneNumber();
        User user = userRepository.findByPhone(phoneKey)
                .orElseGet(() -> {
                    log.info("Creating new user from phone login: {}", phoneKey);
                    return createUserFromPhone(request);
                });

        // Generate JWT token using phone as identifier
        String token = jwtTokenProvider.generateToken(user.getPhone());

        log.info("✅ Phone verification successful for user: {}", user.getName());

        // Create response with user info
        UserDTO userDTO = UserDTO.fromEntity(user);
        AuthResponse response = new AuthResponse(token, userDTO);

        // Note: In the response, we should include isNewUser flag
        // This would require modifying AuthResponse class
        return response;
    }

    private User createUserFromPhone(PhoneVerifyRequest request) {
        User user = new User();

        // Set basic info
        String defaultName = request.getName() != null && !request.getName().isEmpty()
                ? request.getName()
                : "User" + System.currentTimeMillis();
        user.setName(defaultName);

        // Set phone
        String phoneKey = request.getCountryCode() + request.getPhoneNumber();
        user.setPhone(phoneKey);

        // Parse and set role
        user.setRole(request.getRole());

        // Phone users don't have password initially
        // Set profileCompleted to false for new users
        user.setProfileCompleted(false);

        User savedUser = userRepository.save(user);
        log.info("New user created via phone: ID={}, Name={}, Role={}",
                 savedUser.getId(), savedUser.getName(), savedUser.getRole());

        return savedUser;
    }
}
