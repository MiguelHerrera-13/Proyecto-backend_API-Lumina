package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.ReporteDiarioRequestDTO;
import com.miguel.api_lumina.dto.ReporteDiarioResponseDTO;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.service.ReporteDiarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes-diarios")
public class ReporteDiarioController {

    private final ReporteDiarioService reporteDiarioService;

    public ReporteDiarioController(ReporteDiarioService reporteDiarioService) {
        this.reporteDiarioService = reporteDiarioService;
    }

    @PostMapping
    public ResponseEntity<ReporteDiarioResponseDTO> registrarReporte(@Valid @RequestBody ReporteDiarioRequestDTO requestDTO) {
        ReporteDiarioEntity guardado = reporteDiarioService.registrarReporte(requestDTO);
        return new ResponseEntity<>(toResponseDTO(guardado), HttpStatus.CREATED);
    }

    @GetMapping("/turno/{idTurno}")
    public ResponseEntity<List<ReporteDiarioResponseDTO>> obtenerReportesPorTurno(@PathVariable Long idTurno) {
        List<ReporteDiarioEntity> reportes = reporteDiarioService.obtenerReportesPorTurno(idTurno);
        List<ReporteDiarioResponseDTO> dtos = reportes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

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
