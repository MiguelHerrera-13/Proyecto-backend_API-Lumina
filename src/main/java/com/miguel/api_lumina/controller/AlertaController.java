package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AlertaResponseDTO;
import com.miguel.api_lumina.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Consulta de alertas clínicas automáticas generadas por el sistema para los médicos")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @Operation(summary = "Obtener alertas no leídas de un médico",
            description = "Devuelve la lista de alertas clínicas activas y no leídas asignadas al médico con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de alertas obtenida correctamente")
    })
    @GetMapping("/medico/{id}")
    public ResponseEntity<List<AlertaResponseDTO>> obtenerAlertasPorMedico(
            @PathVariable("id") Long idMedico) {

        List<AlertaResponseDTO> alertas =
                alertaService.obtenerAlertasNoLeidasPorMedico(idMedico);

        return ResponseEntity.ok(alertas);
    }
}