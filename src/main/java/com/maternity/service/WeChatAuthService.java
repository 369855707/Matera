package com.maternity.service;

import com.maternity.dto.WeChatAccessTokenResponse;
import com.maternity.dto.WeChatUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeChatAuthService {

    private static final Logger log = LoggerFactory.getLogger(WeChatAuthService.class);

    private final WebClient webClient;

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo";

    public WeChatAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Exchange authorization code for access token
     */
    public WeChatAccessTokenResponse getAccessToken(String code) {
        log.debug("Exchanging code for WeChat access token");

        try {
            Mono<WeChatAccessTokenResponse> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ACCESS_TOKEN_URL)
                            .queryParam("appid", appId)
                            .queryParam("secret", appSecret)
                            .queryParam("code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .bodyToMono(WeChatAccessTokenResponse.class);

            WeChatAccessTokenResponse tokenResponse = response.block();

            if (tokenResponse != null && tokenResponse.getErrCode() != null) {
                log.error("WeChat API error: {} - {}", tokenResponse.getErrCode(), tokenResponse.getErrMsg());
                throw new RuntimeException("WeChat authentication failed: " + tokenResponse.getErrMsg());
            }

            return tokenResponse;
        } catch (Exception e) {
            log.error("Failed to get WeChat access token", e);
            throw new RuntimeException("Failed to authenticate with WeChat", e);
        }
    }

    /**
     * Get user info using access token
     */
    public WeChatUserInfo getUserInfo(String accessToken, String openId) {
        log.debug("Fetching WeChat user info for openId: {}", openId);

        try {
            Mono<WeChatUserInfo> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(USER_INFO_URL)
                            .queryParam("access_token", accessToken)
                            .queryParam("openid", openId)
                            .queryParam("lang", "zh_CN")
                            .build())
                    .retrieve()
                    .bodyToMono(WeChatUserInfo.class);

            WeChatUserInfo userInfo = response.block();

            if (userInfo == null) {
                throw new RuntimeException("Failed to get user info from WeChat");
            }

            return userInfo;
        } catch (Exception e) {
            log.error("Failed to get WeChat user info", e);
            throw new RuntimeException("Failed to get user info from WeChat", e);
        }
    }

    /**
     * Complete WeChat OAuth flow: exchange code for token and get user info
     */
    public WeChatUserInfo authenticateWithWeChat(String code) {
        log.info("Starting WeChat authentication flow");

        // Step 1: Exchange code for access token
        WeChatAccessTokenResponse tokenResponse = getAccessToken(code);

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("Failed to get access token from WeChat");
        }

        // Step 2: Get user info using access token
        WeChatUserInfo userInfo = getUserInfo(tokenResponse.getAccessToken(), tokenResponse.getOpenId());

        // Set unionId from token response if not already set
        if (userInfo.getUnionId() == null && tokenResponse.getUnionId() != null) {
            userInfo.setUnionId(tokenResponse.getUnionId());
        }

        log.info("WeChat authentication successful for openId: {}", userInfo.getOpenId());
        return userInfo;
    }
}
