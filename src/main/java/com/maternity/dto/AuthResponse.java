package com.maternity.dto;

public class AuthResponse {
    private String token;
    private Object user;
    private String userType;

    public AuthResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
        this.userType = "USER";
    }

    public AuthResponse(String token, AdminDTO admin, String userType) {
        this.token = token;
        this.user = admin;
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
