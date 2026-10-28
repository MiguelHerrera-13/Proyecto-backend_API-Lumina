package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AlergiaRequestDTO;
import com.miguel.api_lumina.dto.AlergiaResponseDTO;
import com.miguel.api_lumina.entity.AlergiaEntity;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.repository.AlergiaRepository;
import com.miguel.api_lumina.repository.PacienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlergiaService {

    private static final Set<String> SEVERIDADES_VALIDAS = Set.of("LEVE", "MODERADA", "GRAVE");

    private final AlergiaRepository alergiaRepository;
    private final PacienteRepository pacienteRepository;

    public AlergiaService(AlergiaRepository alergiaRepository, PacienteRepository pacienteRepository) {
        this.alergiaRepository = alergiaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public AlergiaResponseDTO crearAlergia(Long idPaciente, AlergiaRequestDTO dto) {
        PacienteEntity paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente));

        String severidadNormalizada = validarSeveridad(dto.getSeveridad());
        String tipoLimpio = dto.getTipo().trim();

        if (alergiaRepository.existsByPacienteIdPacienteAndTipoIgnoreCase(idPaciente, tipoLimpio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una alergia activa registrada con el tipo: " + tipoLimpio);
        }

        AlergiaEntity entity = new AlergiaEntity();
        entity.setPaciente(paciente);
        entity.setTipo(tipoLimpio);
        entity.setSeveridad(severidadNormalizada);
        entity.setDescripcion(dto.getDescripcion().trim());

        AlergiaEntity guardada = alergiaRepository.save(entity);
        return mapToResponseDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<AlergiaResponseDTO> listarAlergiasPorPaciente(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        return alergiaRepository.findByPacienteIdPaciente(idPaciente)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlergiaResponseDTO obtenerAlergiaPorId(Long idPaciente, Long idAlergia) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        AlergiaEntity entity = alergiaRepository.findByIdAlergiaAndPacienteIdPaciente(idAlergia, idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia no encontrada con ID: " + idAlergia));

        return mapToResponseDTO(entity);
    }

    @Transactional
    public AlergiaResponseDTO actualizarAlergia(Long idPaciente, Long idAlergia, AlergiaRequestDTO dto) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        AlergiaEntity entity = alergiaRepository.findByIdAlergiaAndPacienteIdPaciente(idAlergia, idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia no encontrada con ID: " + idAlergia));

        String severidadNormalizada = validarSeveridad(dto.getSeveridad());
        String tipoLimpio = dto.getTipo().trim();

        if (alergiaRepository.existsByPacienteIdPacienteAndTipoIgnoreCaseAndIdAlergiaNot(idPaciente, tipoLimpio, idAlergia)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otra alergia activa registrada con el tipo: " + tipoLimpio);
        }

        entity.setTipo(tipoLimpio);
        entity.setSeveridad(severidadNormalizada);
        entity.setDescripcion(dto.getDescripcion().trim());

        AlergiaEntity actualizada = alergiaRepository.save(entity);
        return mapToResponseDTO(actualizada);
    }

    @Transactional
    public void eliminarAlergia(Long idPaciente, Long idAlergia) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        AlergiaEntity entity = alergiaRepository.findByIdAlergiaAndPacienteIdPaciente(idAlergia, idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia no encontrada con ID: " + idAlergia));

        alergiaRepository.delete(entity);
    }

    private String validarSeveridad(String severidad) {
        if (severidad == null || !SEVERIDADES_VALIDAS.contains(severidad.trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Severidad inválida. Debe ser LEVE, MODERADA o GRAVE");
        }
        return severidad.trim().toUpperCase();
    }

    private AlergiaResponseDTO mapToResponseDTO(AlergiaEntity entity) {
        return new AlergiaResponseDTO(
                entity.getIdAlergia(),
                entity.getPaciente() != null ? entity.getPaciente().getIdPaciente() : null,
                entity.getTipo(),
                entity.getSeveridad(),
                entity.getDescripcion(),
                entity.getActivo()
        );
    }
}
