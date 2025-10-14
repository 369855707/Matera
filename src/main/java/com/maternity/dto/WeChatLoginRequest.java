package com.maternity.dto;

import jakarta.validation.constraints.NotBlank;

public class WeChatLoginRequest {

    @NotBlank(message = "WeChat auth code is required")
    private String code;

    @NotBlank(message = "User role is required")
    private String role; // "MOTHER" or "MATRON"

    // Constructors
    public WeChatLoginRequest() {
    }

    public WeChatLoginRequest(String code, String role) {
        this.code = code;
        this.role = role;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
