package com.miguel.api_lumina.dto;

public class AuthResponseDTO {

    private String token;
    private String tipo = "Bearer";
    private String email;
    private String rol;
    private String refreshToken;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String tipo, String email, String rol) {
        this.token = token;
        this.tipo = tipo;
        this.email = email;
        this.rol = rol;
    }

    public AuthResponseDTO(String token, String tipo, String email, String rol, String refreshToken) {
        this.token = token;
        this.tipo = tipo;
        this.email = email;
        this.rol = rol;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
