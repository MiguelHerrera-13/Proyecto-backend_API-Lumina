package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.HistorialDTO;
import com.miguel.api_lumina.dto.PacienteRequestDTO;
import com.miguel.api_lumina.dto.PacienteResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    private PacienteResponseDTO mapearResponse(PacienteEntity p) {
        return new PacienteResponseDTO(
                p.getIdPaciente(),
                p.getNombreCompleto(),
                p.getFechaNacimiento(),
                p.getDiagnosticoFase(),
                p.getActivo(),
                p.getIdEmpleadoAsignado(),
                p.getNombreEmpleadoAsignado(),
                p.getRolEmpleadoAsignado(),
                p.getIdFamiliarAsignado(),
                p.getNombreFamiliarAsignado(),
                p.getEmailFamiliarAsignado(),
                p.getParentescoFamiliar()
        );
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> obtenerTodos() {
        List<PacienteResponseDTO> lista = pacienteService.obtenerTodos().stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<HistorialDTO> obtenerHistorial(@PathVariable Long id) {
        HistorialDTO historial = pacienteService.obtenerHistorial(id);
        return ResponseEntity.ok(historial);
    }

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@Valid @RequestBody PacienteRequestDTO requestDTO) {
        PacienteEntity entity = new PacienteEntity();
        entity.setNombreCompleto(requestDTO.getNombreCompleto() != null && !requestDTO.getNombreCompleto().isBlank()
                ? requestDTO.getNombreCompleto().trim() : "Paciente Lumina");
        entity.setFechaNacimiento(requestDTO.getFechaNacimiento() != null
                ? requestDTO.getFechaNacimiento() : java.time.LocalDate.of(1960, 1, 1));
        entity.setDiagnosticoFase(requestDTO.getDiagnosticoFase() != null && !requestDTO.getDiagnosticoFase().isBlank()
                ? requestDTO.getDiagnosticoFase().trim() : "Control clínico general");
        entity.setActivo(true);

        // Asignación de profesional / médico o empleado
        entity.setIdEmpleadoAsignado(requestDTO.getIdEmpleadoAsignado());
        entity.setNombreEmpleadoAsignado(requestDTO.getNombreEmpleadoAsignado());
        entity.setRolEmpleadoAsignado(requestDTO.getRolEmpleadoAsignado());

        // Asignación de familiar
        entity.setIdFamiliarAsignado(requestDTO.getIdFamiliarAsignado());
        entity.setNombreFamiliarAsignado(requestDTO.getNombreFamiliarAsignado());
        entity.setEmailFamiliarAsignado(requestDTO.getEmailFamiliarAsignado());
        entity.setParentescoFamiliar(requestDTO.getParentescoFamiliar());

        PacienteEntity pacienteGuardado = pacienteService.registrarPaciente(entity);
        return new ResponseEntity<>(mapearResponse(pacienteGuardado), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obtenerPaciente(@PathVariable Long id) {
        Optional<PacienteEntity> pacienteOpt = pacienteService.obtenerPacientePorId(id);
        return pacienteOpt.map(pacienteEntity -> ResponseEntity.ok(mapearResponse(pacienteEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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

            if (requestDTO.getIdEmpleadoAsignado() != null) {
                entity.setIdEmpleadoAsignado(requestDTO.getIdEmpleadoAsignado());
            }
            if (requestDTO.getNombreEmpleadoAsignado() != null) {
                entity.setNombreEmpleadoAsignado(requestDTO.getNombreEmpleadoAsignado().trim());
            }
            if (requestDTO.getRolEmpleadoAsignado() != null) {
                entity.setRolEmpleadoAsignado(requestDTO.getRolEmpleadoAsignado().trim());
            }

            if (requestDTO.getIdFamiliarAsignado() != null) {
                entity.setIdFamiliarAsignado(requestDTO.getIdFamiliarAsignado());
            }
            if (requestDTO.getNombreFamiliarAsignado() != null) {
                entity.setNombreFamiliarAsignado(requestDTO.getNombreFamiliarAsignado().trim());
            }
            if (requestDTO.getEmailFamiliarAsignado() != null) {
                entity.setEmailFamiliarAsignado(requestDTO.getEmailFamiliarAsignado().trim());
            }
            if (requestDTO.getParentescoFamiliar() != null) {
                entity.setParentescoFamiliar(requestDTO.getParentescoFamiliar().trim());
            }

            PacienteEntity actualizado = pacienteService.registrarPaciente(entity);
            return ResponseEntity.ok(mapearResponse(actualizado));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/asignar-familiar")
    public ResponseEntity<PacienteResponseDTO> asignarFamiliar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Optional<PacienteEntity> pacienteOpt = pacienteService.obtenerPacientePorId(id);
        if (pacienteOpt.isPresent()) {
            PacienteEntity entity = pacienteOpt.get();
            if (body.containsKey("nombreFamiliar")) {
                entity.setNombreFamiliarAsignado(body.get("nombreFamiliar"));
            }
            if (body.containsKey("emailFamiliar")) {
                entity.setEmailFamiliarAsignado(body.get("emailFamiliar"));
            }
            if (body.containsKey("parentesco")) {
                entity.setParentescoFamiliar(body.get("parentesco"));
            }
            if (body.containsKey("idFamiliar") && body.get("idFamiliar") != null) {
                try {
                    entity.setIdFamiliarAsignado(Long.parseLong(body.get("idFamiliar")));
                } catch (Exception ignored) {}
            }
            PacienteEntity actualizado = pacienteService.registrarPaciente(entity);
            return ResponseEntity.ok(mapearResponse(actualizado));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        pacienteService.darDeBajaPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
