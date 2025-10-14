package com.maternity.dto;

public class PhoneSendCodeResponse {
    private boolean success;
    private String message;
    private int expiresIn; // seconds

    public PhoneSendCodeResponse() {
    }

    public PhoneSendCodeResponse(boolean success, String message, int expiresIn) {
        this.success = success;
        this.message = message;
        this.expiresIn = expiresIn;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
