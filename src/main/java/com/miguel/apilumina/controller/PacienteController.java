package com.miguel.apilumina.controller;

import com.miguel.apilumina.dto.PacienteRequestDTO;
import com.miguel.apilumina.dto.PacienteResponseDTO;
import com.miguel.apilumina.entity.PacienteEntity;
import com.miguel.apilumina.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@Valid @RequestBody PacienteRequestDTO requestDTO) {
        // Mapeo manual RequestDTO -> Entity
        PacienteEntity entity = new PacienteEntity();
        entity.setNombreCompleto(requestDTO.getNombreCompleto());
        entity.setFechaNacimiento(requestDTO.getFechaNacimiento());
        entity.setDiagnosticoFase(requestDTO.getDiagnosticoFase());
        
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        pacienteService.darDeBajaPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
