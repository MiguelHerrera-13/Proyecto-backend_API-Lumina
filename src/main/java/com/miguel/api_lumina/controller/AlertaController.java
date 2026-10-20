package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AlertaResponseDTO;
import com.miguel.api_lumina.service.AlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping("/medico/{id}")
    public ResponseEntity<List<AlertaResponseDTO>> obtenerAlertasPorMedico(
            @PathVariable("id") Long idMedico,
            @RequestParam(value = "leida", required = false) Boolean leida) {

        List<AlertaResponseDTO> alertas =
                alertaService.obtenerAlertasPorMedicoYEstado(idMedico, leida);

        return ResponseEntity.ok(alertas);
    }

    @PatchMapping("/medico/{idMedico}/{idAlerta}/leida")
    public ResponseEntity<AlertaResponseDTO> marcarAlertaComoLeida(
            @PathVariable("idMedico") Long idMedico,
            @PathVariable("idAlerta") Long idAlerta) {

        AlertaResponseDTO response = alertaService.marcarAlertaComoLeida(idMedico, idAlerta);
        return ResponseEntity.ok(response);
    }
}