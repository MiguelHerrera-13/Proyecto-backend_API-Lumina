package com.miguel.api_lumina.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long testExpiration = 3600000; // 1 hora

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService(testSecret, testExpiration);
    }

    @Test
    @DisplayName("Generar token y extraer claims correctamente")
    public void testGenerateAndExtractClaims() {
        String email = "medico.ficticio@lumina.com";
        Long idUsuario = 10L;
        String rol = "MEDICO";

        String token = jwtService.generateToken(email, idUsuario, rol);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(rol, jwtService.extractRol(token));
        assertEquals(idUsuario, jwtService.extractIdUsuario(token));
        assertTrue(jwtService.validateToken(token));
        assertTrue(jwtService.isTokenValid(token, email));
    }

    @Test
    @DisplayName("Token inválido para un correo distinto")
    public void testTokenInvalidForDifferentEmail() {
        String token = jwtService.generateToken("user1@lumina.com", 1L, "CUIDADOR");

        assertFalse(jwtService.isTokenValid(token, "otro@lumina.com"));
    }

    @Test
    @DisplayName("Token alterado o manipulado debe ser rechazado")
    public void testTamperedTokenIsRejected() {
        String token = jwtService.generateToken("admin@lumina.com", 2L, "ADMIN");
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(jwtService.validateToken(tamperedToken));
        assertFalse(jwtService.isTokenValid(tamperedToken, "admin@lumina.com"));
    }

    @Test
    @DisplayName("Token expirado debe ser rechazado")
    public void testExpiredTokenIsRejected() {
        JwtService expiredJwtService = new JwtService(testSecret, -1000); // Expirado en el pasado
        String token = expiredJwtService.generateToken("expirado@lumina.com", 3L, "MEDICO");

        assertFalse(expiredJwtService.validateToken(token));
        assertFalse(expiredJwtService.isTokenValid(token, "expirado@lumina.com"));
    }
}
