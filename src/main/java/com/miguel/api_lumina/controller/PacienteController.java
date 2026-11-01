package com.miguel.api_lumina.controller;

import com.miguel.api_lumina.dto.AsignacionProfesionalRequestDTO;
import com.miguel.api_lumina.dto.AsignacionProfesionalResponseDTO;
import com.miguel.api_lumina.dto.HistorialDTO;
import com.miguel.api_lumina.dto.PacienteInactivoResponseDTO;
import com.miguel.api_lumina.dto.PacienteRequestDTO;
import com.miguel.api_lumina.dto.PacienteResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<PacienteInactivoResponseDTO>> obtenerPacientesInactivos(
            Authentication authentication) {
        validarRolAdmin(authentication);
        List<PacienteInactivoResponseDTO> inactivos = pacienteService.obtenerPacientesInactivos();
        return ResponseEntity.ok(inactivos);
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<PacienteResponseDTO> reactivarPaciente(
            @PathVariable("id") Long id,
            Authentication authentication) {
        validarRolAdmin(authentication);
        PacienteResponseDTO responseDTO = pacienteService.reactivarPaciente(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<HistorialDTO> obtenerHistorial(@PathVariable("id") Long id) {
        HistorialDTO historial = pacienteService.obtenerHistorial(id);
        return ResponseEntity.ok(historial);
    }

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@Valid @RequestBody PacienteRequestDTO requestDTO) {
        PacienteEntity entity = new PacienteEntity();
        entity.setNombreCompleto(requestDTO.getNombreCompleto());
        entity.setFechaNacimiento(requestDTO.getFechaNacimiento());
        entity.setDiagnosticoFase(requestDTO.getDiagnosticoFase());

        PacienteEntity pacienteGuardado = pacienteService.registrarPaciente(entity);

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
    public ResponseEntity<PacienteResponseDTO> obtenerPaciente(@PathVariable("id") Long id) {
        Optional<PacienteEntity> pacienteOpt = pacienteService.obtenerPacientePorId(id);

        if (pacienteOpt.isPresent()) {
            PacienteEntity entity = pacienteOpt.get();
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
    public ResponseEntity<Void> eliminarPaciente(
            @PathVariable("id") Long id,
            @RequestParam(value = "motivo", required = false) String motivo,
            Authentication authentication) {
        String usuario = (authentication != null) ? authentication.getName() : null;
        pacienteService.darDeBajaPaciente(id, motivo, usuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/medico-cabecera")
    public ResponseEntity<AsignacionProfesionalResponseDTO> asignarMedicoCabecera(
            @PathVariable("id") Long idPaciente,
            @Valid @RequestBody AsignacionProfesionalRequestDTO requestDTO) {
        try {
            AsignacionProfesionalResponseDTO response =
                    pacienteService.asignarMedicoCabecera(idPaciente, requestDTO.getIdUsuario());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/cuidadores")
    public ResponseEntity<AsignacionProfesionalResponseDTO> agregarCuidador(
            @PathVariable("id") Long idPaciente,
            @Valid @RequestBody AsignacionProfesionalRequestDTO requestDTO) {
        try {
            AsignacionProfesionalResponseDTO response =
                    pacienteService.agregarCuidador(idPaciente, requestDTO.getIdUsuario());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void validarRolAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acceso no autorizado");
        }

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        if (!esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo usuarios con rol ADMIN pueden reactivar pacientes");
        }
    }
}