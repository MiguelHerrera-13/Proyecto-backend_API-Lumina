package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.ChangePasswordRequestDTO;
import com.miguel.api_lumina.dto.ChangePasswordResponseDTO;
import com.miguel.api_lumina.dto.ForgotPasswordRequestDTO;
import com.miguel.api_lumina.dto.ForgotPasswordResponseDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.dto.LogoutRequestDTO;
import com.miguel.api_lumina.dto.LogoutResponseDTO;
import com.miguel.api_lumina.dto.RefreshTokenRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordRequestDTO;
import com.miguel.api_lumina.dto.ResetPasswordResponseDTO;
import com.miguel.api_lumina.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequestDTO request
    ) {
        LogoutResponseDTO response = authService.logout(authHeader, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ChangePasswordResponseDTO> changePassword(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequestDTO request
    ) {
        ChangePasswordResponseDTO response = authService.changePassword(authentication, authHeader, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        ForgotPasswordResponseDTO response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        ResetPasswordResponseDTO response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.GetMapping(value = "/activar", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> activarCuentaHtml(@org.springframework.web.bind.annotation.RequestParam("token") String token) {
        try {
            String mensaje = authService.activarCuenta(token);
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Lumina - Cuenta Activada</title>"
                    + "<style>body{font-family:system-ui,-apple-system,sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;background:#F1F5F9;}"
                    + ".card{background:white;padding:40px;border-radius:16px;box-shadow:0 10px 25px rgba(0,0,0,0.05);text-align:center;max-width:440px;}"
                    + "h2{color:#0D9488;margin-bottom:12px;}.btn{display:inline-block;margin-top:20px;padding:12px 24px;background:#0D9488;color:white;text-decoration:none;border-radius:8px;font-weight:bold;}</style></head>"
                    + "<body><div class='card'>"
                    + "<svg width='64' height='64' viewBox='0 0 24 24' fill='none' stroke='#0D9488' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M22 11.08V12a10 10 0 1 1-5.93-9.14'></path><polyline points='22 4 12 14.01 9 11.01'></polyline></svg>"
                    + "<h2>¡Cuenta Activada!</h2>"
                    + "<p>" + mensaje + "</p>"
                    + "<p style='color:#64748B;font-size:14px;'>Correo enviado desde <b>cuidadores.ies@gmail.com</b></p>"
                    + "<p>Ya puedes volver a la aplicación Lumina e iniciar sesión con tu cuenta.</p>"
                    + "</div></body></html>";
            return ResponseEntity.ok(html);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            String errorHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Lumina - Error de Activación</title>"
                    + "<style>body{font-family:system-ui,-apple-system,sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;background:#F1F5F9;}"
                    + ".card{background:white;padding:40px;border-radius:16px;box-shadow:0 10px 25px rgba(0,0,0,0.05);text-align:center;max-width:440px;}"
                    + "h2{color:#E11D48;margin-bottom:12px;}</style></head>"
                    + "<body><div class='card'>"
                    + "<svg width='64' height='64' viewBox='0 0 24 24' fill='none' stroke='#E11D48' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><circle cx='12' cy='12' r='10'></circle><line x1='15' y1='9' x2='9' y2='15'></line><line x1='9' y1='9' x2='15' y2='15'></line></svg>"
                    + "<h2>Error de Activación</h2>"
                    + "<p>" + e.getReason() + "</p>"
                    + "</div></body></html>";
            return ResponseEntity.status(e.getStatusCode()).body(errorHtml);
        }
    }

    @PostMapping("/activar")
    public ResponseEntity<?> activarCuentaApi(@org.springframework.web.bind.annotation.RequestParam("token") String token) {
        String mensaje = authService.activarCuenta(token);
        return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", mensaje));
    }

    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigoApi(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        String mensaje = authService.activarCuenta(token);
        return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", mensaje));
    }
}
