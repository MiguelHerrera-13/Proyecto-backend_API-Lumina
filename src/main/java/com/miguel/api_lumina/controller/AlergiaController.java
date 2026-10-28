package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AlergiaRequestDTO;
import com.miguel.api_lumina.dto.AlergiaResponseDTO;
import com.miguel.api_lumina.service.AlergiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes/{id}/alergias")
public class AlergiaController {

    private final AlergiaService alergiaService;

    public AlergiaController(AlergiaService alergiaService) {
        this.alergiaService = alergiaService;
    }

    @PostMapping
    public ResponseEntity<AlergiaResponseDTO> crearAlergia(
            @PathVariable("id") Long idPaciente,
            @Valid @RequestBody AlergiaRequestDTO dto) {
        AlergiaResponseDTO creada = alergiaService.crearAlergia(idPaciente, dto);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AlergiaResponseDTO>> listarAlergiasPorPaciente(
            @PathVariable("id") Long idPaciente) {
        List<AlergiaResponseDTO> alergias = alergiaService.listarAlergiasPorPaciente(idPaciente);
        return ResponseEntity.ok(alergias);
    }

    @GetMapping("/{idAlergia}")
    public ResponseEntity<AlergiaResponseDTO> obtenerAlergiaPorId(
            @PathVariable("id") Long idPaciente,
            @PathVariable("idAlergia") Long idAlergia) {
        AlergiaResponseDTO alergia = alergiaService.obtenerAlergiaPorId(idPaciente, idAlergia);
        return ResponseEntity.ok(alergia);
    }

    @PutMapping("/{idAlergia}")
    public ResponseEntity<AlergiaResponseDTO> actualizarAlergia(
            @PathVariable("id") Long idPaciente,
            @PathVariable("idAlergia") Long idAlergia,
            @Valid @RequestBody AlergiaRequestDTO dto) {
        AlergiaResponseDTO actualizada = alergiaService.actualizarAlergia(idPaciente, idAlergia, dto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{idAlergia}")
    public ResponseEntity<Void> eliminarAlergia(
            @PathVariable("id") Long idPaciente,
            @PathVariable("idAlergia") Long idAlergia) {
        alergiaService.eliminarAlergia(idPaciente, idAlergia);
        return ResponseEntity.noContent().build();
    }
}
