package com.miguel.api_lumina.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final JwtService jwtService;
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        Instant expirationTime = null;
        try {
            if (jwtService != null) {
                Date expDate = jwtService.extractExpiration(token);
                if (expDate != null) {
                    expirationTime = expDate.toInstant();
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer la fecha de expiración del JWT, usando expiración por defecto: {}", e.getMessage());
        }

        if (expirationTime == null) {
            expirationTime = Instant.now().plus(24, ChronoUnit.HOURS);
        }

        blacklistedTokens.put(token, expirationTime);
        log.info("Token JWT revocado y agregado a la lista negra en memoria hasta [{}]", expirationTime);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Instant expiration = blacklistedTokens.get(token);
        if (expiration == null) {
            return false;
        }

        if (Instant.now().isAfter(expiration)) {
            // El token ya expiró de forma natural, limpiar de memoria
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }

    public void cleanExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }

    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    public void clear() {
        blacklistedTokens.clear();
    }
}
