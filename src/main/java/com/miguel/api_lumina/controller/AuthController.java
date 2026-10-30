package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y obtención del token JWT para acceder a los endpoints protegidos")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica al usuario con email y contraseña. " +
                    "Devuelve un token JWT Bearer que debe incluirse en el header " +
                    "'Authorization: Bearer <token>' para acceder a los endpoints protegidos. " +
                    "**Este endpoint es público: no requiere token JWT.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso. Se devuelve el token JWT junto con el email y rol del usuario."),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas: email o contraseña incorrectos.")
    })
    @SecurityRequirements(value = {})
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
