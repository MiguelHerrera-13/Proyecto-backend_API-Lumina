package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.TratamientoRequestDTO;
import com.miguel.api_lumina.dto.TratamientoResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TratamientoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TratamientoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public TratamientoService(TratamientoRepository tratamientoRepository,
                              PacienteRepository pacienteRepository,
                              UsuarioRepository usuarioRepository) {
        this.tratamientoRepository = tratamientoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public TratamientoResponseDTO registrarTratamiento(TratamientoRequestDTO requestDTO) {
        // Validación de fechas en capa de Servicio (Regla de negocio crítica)
        if (requestDTO.getFechaInicio() == null || requestDTO.getFechaFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias");
        }

        if (!requestDTO.getFechaFin().isAfter(requestDTO.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // Validación de existencia de paciente
        PacienteEntity paciente = pacienteRepository.findById(requestDTO.getIdPaciente())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El paciente con ID " + requestDTO.getIdPaciente() + " no existe"));

        // Validación de existencia de médico
        UsuarioEntity medico = usuarioRepository.findById(requestDTO.getIdMedico())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El médico con ID " + requestDTO.getIdMedico() + " no existe"));

        // Validación de rol médico (si rol está especificado)
        if (medico.getRol() != null && !medico.getRol().equalsIgnoreCase("MEDICO")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario con ID " + requestDTO.getIdMedico() + " no tiene el rol de MEDICO");
        }

        TratamientoEntity entity = new TratamientoEntity();
        entity.setPaciente(paciente);
        entity.setMedico(medico);
        entity.setDescripcion(requestDTO.getDescripcion());
        entity.setFechaInicio(requestDTO.getFechaInicio());
        entity.setFechaFin(requestDTO.getFechaFin());
        entity.setActivo(true);

        TratamientoEntity guardado = tratamientoRepository.save(entity);
        return mapearAResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<TratamientoResponseDTO> obtenerTratamientosPorPaciente(Long idPaciente) {
        // Validar que el paciente exista
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El paciente con ID " + idPaciente + " no existe");
        }

        return tratamientoRepository.findByPaciente_IdPaciente(idPaciente).stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TratamientoResponseDTO> obtenerTratamientoPorId(Long id) {
        return tratamientoRepository.findById(id).map(this::mapearAResponseDTO);
    }

    @Transactional
    public void darDeBajaTratamiento(Long id) {
        TratamientoEntity tratamiento = tratamientoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El tratamiento con ID " + id + " no existe"));
        tratamientoRepository.delete(tratamiento);
    }

    private TratamientoResponseDTO mapearAResponseDTO(TratamientoEntity entity) {
        TratamientoResponseDTO dto = new TratamientoResponseDTO();
        dto.setIdTratamiento(entity.getIdTratamiento());
        if (entity.getPaciente() != null) {
            dto.setIdPaciente(entity.getPaciente().getIdPaciente());
            dto.setNombrePaciente(entity.getPaciente().getNombreCompleto());
        }
        if (entity.getMedico() != null) {
            dto.setIdMedico(entity.getMedico().getIdUsuario());
            dto.setNombreMedico(entity.getMedico().getNombre());
        }
        dto.setDescripcion(entity.getDescripcion());
        dto.setFechaInicio(entity.getFechaInicio());
        dto.setFechaFin(entity.getFechaFin());
        dto.setActivo(entity.getActivo());
        return dto;
    }
}
