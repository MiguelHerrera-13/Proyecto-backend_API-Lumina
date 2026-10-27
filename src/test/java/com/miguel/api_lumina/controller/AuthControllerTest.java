package com.miguel.api_lumina.controller;

import tools.jackson.databind.ObjectMapper;
import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.ForgotPasswordRequestDTO;
import com.miguel.api_lumina.dto.ForgotPasswordResponseDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordResponseDTO;
import com.miguel.api_lumina.exception.GlobalExceptionHandler;
import com.miguel.api_lumina.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login con credenciales válidas retorna 200 y token JWT")
    public void testLogin_Success() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario@lumina.com", "clave123");
        AuthResponseDTO response = new AuthResponseDTO("fake.jwt.token", "Bearer", "usuario@lumina.com", "MEDICO");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value("usuario@lumina.com"))
                .andExpect(jsonPath("$.rol").value("MEDICO"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login con credenciales inválidas retorna 401 Unauthorized")
    public void testLogin_InvalidCredentials() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario@lumina.com", "claveErronea");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("POST /api/auth/login con email inválido retorna 400 Bad Request")
    public void testLogin_InvalidEmail() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("formato-invalido", "clave123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login con contraseña vacía retorna 400 Bad Request")
    public void testLogin_EmptyPassword() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario@lumina.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password retorna 200 con mensaje genérico de anti-enumeración")
    public void testForgotPassword_Success() throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("usuario@lumina.com");
        ForgotPasswordResponseDTO response = new ForgotPasswordResponseDTO(
                "Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña.");

        when(authService.forgotPassword(any(ForgotPasswordRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Si el correo electrónico está registrado, recibirás un enlace o token para restablecer tu contraseña."));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password con formato de email inválido retorna 400 Bad Request")
    public void testForgotPassword_InvalidEmail() throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("email-invalido");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password con email vacío retorna 400 Bad Request")
    public void testForgotPassword_EmptyEmail() throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password con datos válidos retorna 200 y mensaje de confirmación")
    public void testResetPassword_Success() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token-12345", "nuevaClaveSegura123");
        ResetPasswordResponseDTO response = new ResetPasswordResponseDTO("Contraseña restablecida exitosamente");

        when(authService.resetPassword(any(ResetPasswordRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña restablecida exitosamente"));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password con token inválido o expirado retorna 400 Bad Request")
    public void testResetPassword_InvalidToken() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token-invalido", "nuevaClaveSegura123");

        when(authService.resetPassword(any(ResetPasswordRequestDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de recuperación es inválido o no existe"));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El token de recuperación es inválido o no existe"));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password con campos vacíos retorna 400 Bad Request")
    public void testResetPassword_EmptyFields() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("", "");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.nuevaPassword").exists());
    }
}
