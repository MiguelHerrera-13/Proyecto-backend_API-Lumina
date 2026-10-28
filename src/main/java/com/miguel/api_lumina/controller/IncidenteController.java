package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.IncidenteRequestDTO;
import com.miguel.api_lumina.dto.IncidenteResponseDTO;
import com.miguel.api_lumina.service.IncidenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes-diarios")
public class IncidenteController {

    private final IncidenteService incidenteService;

    public IncidenteController(IncidenteService incidenteService) {
        this.incidenteService = incidenteService;
    }

    @PostMapping("/{idReporte}/incidentes")
    public ResponseEntity<IncidenteResponseDTO> registrarIncidente(
            @PathVariable Long idReporte,
            @Valid @RequestBody IncidenteRequestDTO requestDTO) {
        IncidenteResponseDTO creado = incidenteService.registrarIncidente(idReporte, requestDTO);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/{idReporte}/incidentes")
    public ResponseEntity<List<IncidenteResponseDTO>> obtenerIncidentesPorReporte(
            @PathVariable Long idReporte) {
        List<IncidenteResponseDTO> lista = incidenteService.obtenerIncidentesPorReporte(idReporte);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/incidentes/{idIncidente}")
    public ResponseEntity<IncidenteResponseDTO> obtenerIncidentePorId(
            @PathVariable Long idIncidente) {
        IncidenteResponseDTO incidente = incidenteService.obtenerIncidentePorId(idIncidente);
        return ResponseEntity.ok(incidente);
    }
}

