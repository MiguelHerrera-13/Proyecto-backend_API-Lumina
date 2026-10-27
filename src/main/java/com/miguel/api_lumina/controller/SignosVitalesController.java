package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.SignosVitalesRequestDTO;
import com.miguel.api_lumina.dto.SignosVitalesResponseDTO;
import com.miguel.api_lumina.service.SignosVitalesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes/{id}/signos-vitales")
public class SignosVitalesController {

    private final SignosVitalesService signosVitalesService;

    public SignosVitalesController(SignosVitalesService signosVitalesService) {
        this.signosVitalesService = signosVitalesService;
    }

    @PostMapping
    public ResponseEntity<SignosVitalesResponseDTO> registrarSignosVitales(
            @PathVariable("id") Long id,
            @Valid @RequestBody SignosVitalesRequestDTO requestDTO,
            Authentication authentication) {

        String usuarioAutenticado = (authentication != null) ? authentication.getName() : null;
        SignosVitalesResponseDTO response = signosVitalesService.registrarSignosVitales(id, requestDTO, usuarioAutenticado);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SignosVitalesResponseDTO>> obtenerHistorialSignosVitales(
            @PathVariable("id") Long id) {

        List<SignosVitalesResponseDTO> historial = signosVitalesService.obtenerHistorialSignosVitales(id);
        return ResponseEntity.ok(historial);
    }
}
