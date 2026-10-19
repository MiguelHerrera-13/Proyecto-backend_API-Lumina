package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.ReporteDiarioRequestDTO;
import com.miguel.api_lumina.dto.ReporteDiarioResponseDTO;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.service.ReporteDiarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes-diarios")
@Tag(name = "Reportes Diarios", description = "Registro de reportes clínicos diarios vinculados a los turnos de atención")
public class ReporteDiarioController {

    private final ReporteDiarioService reporteDiarioService;

    public ReporteDiarioController(ReporteDiarioService reporteDiarioService) {
        this.reporteDiarioService = reporteDiarioService;
    }

    @Operation(summary = "Registrar un reporte diario",
            description = "Crea un nuevo reporte clínico asociado a un turno existente. " +
                    "Incluye temperatura (35.0–42.0), presión arterial (formato sistólica/diastólica) y observaciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reporte creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: temperatura fuera de rango, formato de presión incorrecto o campos nulos"),
            @ApiResponse(responseCode = "404", description = "El turno indicado no existe en el sistema")
    })
    @PostMapping
    public ResponseEntity<ReporteDiarioResponseDTO> registrarReporte(@Valid @RequestBody ReporteDiarioRequestDTO requestDTO) {
        ReporteDiarioEntity guardado = reporteDiarioService.registrarReporte(requestDTO);
        return new ResponseEntity<>(toResponseDTO(guardado), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener reportes de un turno",
            description = "Lista todos los reportes diarios registrados para el turno con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reportes obtenida correctamente")
    })
    @GetMapping("/turno/{idTurno}")
    public ResponseEntity<List<ReporteDiarioResponseDTO>> obtenerReportesPorTurno(@PathVariable Long idTurno) {
        List<ReporteDiarioEntity> reportes = reporteDiarioService.obtenerReportesPorTurno(idTurno);
        List<ReporteDiarioResponseDTO> dtos = reportes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Obtener un reporte por ID",
            description = "Busca y devuelve un reporte diario activo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte encontrado"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReporteDiarioResponseDTO> obtenerReportePorId(@PathVariable Long id) {
        return reporteDiarioService.obtenerReportePorId(id)
                .map(entity -> ResponseEntity.ok(toResponseDTO(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ReporteDiarioResponseDTO toResponseDTO(ReporteDiarioEntity entity) {
        return new ReporteDiarioResponseDTO(
                entity.getIdReporte(),
                entity.getTurno() != null ? entity.getTurno().getIdTurno() : null,
                entity.getTemperatura(),
                entity.getPresionArterial(),
                entity.getObservacion(),
                entity.getFechaRegistro(),
                entity.getActivo()
        );
    }
}
