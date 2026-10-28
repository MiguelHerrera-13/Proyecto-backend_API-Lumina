package com.miguel.api_lumina.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class LogoutRequestDTO {

    @JsonAlias({"token", "refreshToken"})
    private String refreshToken;

    public LogoutRequestDTO() {
    }

    public LogoutRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
