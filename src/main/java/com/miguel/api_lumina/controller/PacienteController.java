package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.PacienteRequestDTO;
import com.miguel.api_lumina.dto.PacienteResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.miguel.api_lumina.dto.HistorialDTO;

import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public ResponseEntity<java.util.List<PacienteResponseDTO>> obtenerTodos() {
        java.util.List<PacienteResponseDTO> lista = pacienteService.obtenerTodos().stream()
                .map(p -> new PacienteResponseDTO(
                        p.getIdPaciente(),
                        p.getNombreCompleto(),
                        p.getFechaNacimiento(),
                        p.getDiagnosticoFase(),
                        p.getActivo()
                ))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(lista);
    }
    @GetMapping("/{id}/historial")
    public ResponseEntity<HistorialDTO> obtenerHistorial(@PathVariable Long id) {

        HistorialDTO historial = pacienteService.obtenerHistorial(id);

        return ResponseEntity.ok(historial);
    }
    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@RequestBody PacienteRequestDTO requestDTO) {
        // Mapeo manual RequestDTO -> Entity
        PacienteEntity entity = new PacienteEntity();
        entity.setNombreCompleto(requestDTO.getNombreCompleto() != null && !requestDTO.getNombreCompleto().isBlank() ? requestDTO.getNombreCompleto().trim() : "Paciente Lumina");
        entity.setFechaNacimiento(requestDTO.getFechaNacimiento() != null ? requestDTO.getFechaNacimiento() : java.time.LocalDate.of(1960, 1, 1));
        entity.setDiagnosticoFase(requestDTO.getDiagnosticoFase() != null && !requestDTO.getDiagnosticoFase().isBlank() ? requestDTO.getDiagnosticoFase().trim() : "Control clínico general");
        entity.setActivo(true);
        
        PacienteEntity pacienteGuardado = pacienteService.registrarPaciente(entity);
        
        // Mapeo manual Entity -> ResponseDTO
        PacienteResponseDTO responseDTO = new PacienteResponseDTO(
                pacienteGuardado.getIdPaciente(),
                pacienteGuardado.getNombreCompleto(),
                pacienteGuardado.getFechaNacimiento(),
                pacienteGuardado.getDiagnosticoFase(),
                pacienteGuardado.getActivo()
        );
        
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obtenerPaciente(@PathVariable Long id) {
        Optional<PacienteEntity> pacienteOpt = pacienteService.obtenerPacientePorId(id);
        
        if (pacienteOpt.isPresent()) {
            PacienteEntity entity = pacienteOpt.get();
            // Mapeo manual Entity -> ResponseDTO
            PacienteResponseDTO responseDTO = new PacienteResponseDTO(
                    entity.getIdPaciente(),
                    entity.getNombreCompleto(),
                    entity.getFechaNacimiento(),
                    entity.getDiagnosticoFase(),
                    entity.getActivo()
            );
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> actualizarPaciente(@PathVariable Long id, @RequestBody PacienteRequestDTO requestDTO) {
        Optional<PacienteEntity> pacienteOpt = pacienteService.obtenerPacientePorId(id);
        if (pacienteOpt.isPresent()) {
            PacienteEntity entity = pacienteOpt.get();
            if (requestDTO.getNombreCompleto() != null && !requestDTO.getNombreCompleto().isBlank()) {
                entity.setNombreCompleto(requestDTO.getNombreCompleto().trim());
            }
            if (requestDTO.getFechaNacimiento() != null) {
                entity.setFechaNacimiento(requestDTO.getFechaNacimiento());
            }
            if (requestDTO.getDiagnosticoFase() != null) {
                entity.setDiagnosticoFase(requestDTO.getDiagnosticoFase().trim());
            }
            PacienteEntity actualizado = pacienteService.registrarPaciente(entity);
            return ResponseEntity.ok(new PacienteResponseDTO(
                    actualizado.getIdPaciente(),
                    actualizado.getNombreCompleto(),
                    actualizado.getFechaNacimiento(),
                    actualizado.getDiagnosticoFase(),
                    actualizado.getActivo()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        pacienteService.darDeBajaPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
