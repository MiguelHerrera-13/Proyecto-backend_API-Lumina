package com.miguel.api_lumina.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenBlacklistServiceTest {

    @Mock
    private JwtService jwtService;

    private TokenBlacklistService blacklistService;

    @BeforeEach
    public void setUp() {
        blacklistService = new TokenBlacklistService(jwtService);
    }

    @Test
    @DisplayName("blacklistToken agrega un token válido a la lista negra y es detectado por isBlacklisted")
    public void testBlacklistToken_Success() {
        String token = "jwt.token.de.prueba";
        Date futureDate = new Date(System.currentTimeMillis() + 3600000); // 1 hora futura

        when(jwtService.extractExpiration(token)).thenReturn(futureDate);

        assertFalse(blacklistService.isBlacklisted(token));

        blacklistService.blacklistToken(token);

        assertTrue(blacklistService.isBlacklisted(token));
        assertEquals(1, blacklistService.getBlacklistSize());
    }

    @Test
    @DisplayName("isBlacklisted retorna false si el token nunca fue agregado")
    public void testIsBlacklisted_NotFound() {
        assertFalse(blacklistService.isBlacklisted("token-no-agregado"));
    }

    @Test
    @DisplayName("isBlacklisted retorna false para valores nulos o en blanco")
    public void testIsBlacklisted_NullOrBlank() {
        assertFalse(blacklistService.isBlacklisted(null));
        assertFalse(blacklistService.isBlacklisted("   "));
    }

    @Test
    @DisplayName("blacklistToken maneja tokens cuya fecha de expiración ya pasó de forma natural")
    public void testBlacklistToken_AlreadyExpiredNatural() {
        String token = "jwt.expirado.natural";
        Date pastDate = new Date(System.currentTimeMillis() - 10000); // 10 segundos en el pasado

        when(jwtService.extractExpiration(token)).thenReturn(pastDate);

        blacklistService.blacklistToken(token);

        // Al consultar isBlacklisted, detecta que expiró naturalmente y lo purga retornando false
        assertFalse(blacklistService.isBlacklisted(token));
        assertEquals(0, blacklistService.getBlacklistSize());
    }

    @Test
    @DisplayName("cleanExpiredTokens purga correctamente los tokens vencidos en memoria")
    public void testCleanExpiredTokens() {
        String tokenVencido = "token.vencido";
        String tokenActivo = "token.activo";

        when(jwtService.extractExpiration(tokenVencido)).thenReturn(new Date(System.currentTimeMillis() - 5000));
        when(jwtService.extractExpiration(tokenActivo)).thenReturn(new Date(System.currentTimeMillis() + 60000));

        blacklistService.blacklistToken(tokenVencido);
        blacklistService.blacklistToken(tokenActivo);

        assertEquals(2, blacklistService.getBlacklistSize());

        blacklistService.cleanExpiredTokens();

        assertEquals(1, blacklistService.getBlacklistSize());
        assertTrue(blacklistService.isBlacklisted(tokenActivo));
        assertFalse(blacklistService.isBlacklisted(tokenVencido));
    }
}
