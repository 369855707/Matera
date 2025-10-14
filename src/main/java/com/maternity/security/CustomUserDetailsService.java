package com.maternity.security;

import com.maternity.model.Admin;
import com.maternity.model.User;
import com.maternity.repository.AdminRepository;
import com.maternity.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Check if it's an admin login (prefixed with "admin:")
        if (identifier.startsWith("admin:")) {
            String username = identifier.substring(6);
            Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            if (admin.getRole() == Admin.AdminRole.SUPER_ADMIN) {
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            }

            return new org.springframework.security.core.userdetails.User(
                identifier,
                admin.getPassword(),
                admin.getEnabled(),
                true,
                true,
                true,
                authorities
            );
        }

        // Regular user login
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .or(() -> userRepository.findByWechatOpenId(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        // IMPORTANT: Return the same identifier that was used in the token
        // This ensures JWT authentication works correctly
        // Use empty password for WeChat and phone users
        String password = user.getPassword() != null ? user.getPassword() : "";

        return new org.springframework.security.core.userdetails.User(
                identifier,  // Use the same identifier from the token
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
