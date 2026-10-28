package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.ControlIngestaRequestDTO;
import com.miguel.api_lumina.dto.ControlIngestaResponseDTO;
import com.miguel.api_lumina.dto.ResumenIngestaResponseDTO;
import com.miguel.api_lumina.service.ControlIngestaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes-diarios")
public class ControlIngestaController {

    private final ControlIngestaService controlIngestaService;

    public ControlIngestaController(ControlIngestaService controlIngestaService) {
        this.controlIngestaService = controlIngestaService;
    }

    @PostMapping("/{idReporte}/ingestas")
    public ResponseEntity<ControlIngestaResponseDTO> registrarIngesta(
            @PathVariable Long idReporte,
            @Valid @RequestBody ControlIngestaRequestDTO requestDTO) {
        ControlIngestaResponseDTO creado = controlIngestaService.registrarIngesta(idReporte, requestDTO);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/{idReporte}/ingestas")
    public ResponseEntity<List<ControlIngestaResponseDTO>> obtenerIngestasPorReporte(
            @PathVariable Long idReporte) {
        List<ControlIngestaResponseDTO> lista = controlIngestaService.obtenerIngestasPorReporte(idReporte);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idReporte}/ingestas/resumen")
    public ResponseEntity<ResumenIngestaResponseDTO> obtenerResumenIngestaPorReporte(
            @PathVariable Long idReporte) {
        ResumenIngestaResponseDTO resumen = controlIngestaService.obtenerResumenIngestaPorReporte(idReporte);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/ingestas/{idControlIngesta}")
    public ResponseEntity<ControlIngestaResponseDTO> obtenerIngestaPorId(
            @PathVariable Long idControlIngesta) {
        ControlIngestaResponseDTO dto = controlIngestaService.obtenerIngestaPorId(idControlIngesta);
        return ResponseEntity.ok(dto);
    }
}

