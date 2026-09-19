package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public TurnoService(TurnoRepository turnoRepository, PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository) {
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public TurnoResponseDTO crearTurno(TurnoRequestDTO dto) {
        if (dto.getFechaHoraInicio() == null || dto.getFechaHoraFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        
        if (!dto.getFechaHoraInicio().isBefore(dto.getFechaHoraFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la de fin");
        }

        PacienteEntity paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new IllegalArgumentException("El paciente no existe"));

        UsuarioEntity cuidador = usuarioRepository.findById(dto.getIdCuidador())
                .orElseThrow(() -> new IllegalArgumentException("El cuidador no existe"));

        TurnoEntity turno = new TurnoEntity();
        turno.setPaciente(paciente);
        turno.setCuidador(cuidador);
        turno.setFechaHoraInicio(dto.getFechaHoraInicio());
        turno.setFechaHoraFin(dto.getFechaHoraFin());
        turno.setEstado(dto.getEstado());

        TurnoEntity turnoGuardado = turnoRepository.save(turno);

        return mapToDTO(turnoGuardado);
    }

    @Transactional(readOnly = true)
    public TurnoResponseDTO obtenerTurnoPorId(Long id) {
        TurnoEntity turno = turnoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El turno no existe"));
        return mapToDTO(turno);
    }

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> obtenerTurnosPorCuidador(Long idCuidador) {
        // As we check active only by using the SQLRestriction, we just query by cuidador
        List<TurnoEntity> turnos = turnoRepository.findByCuidador_IdUsuario(idCuidador);
        return turnos.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TurnoResponseDTO mapToDTO(TurnoEntity turno) {
        TurnoResponseDTO dto = new TurnoResponseDTO();
        dto.setIdTurno(turno.getIdTurno());
        dto.setNombrePaciente(turno.getPaciente().getNombreCompleto());
        dto.setNombreCuidador(turno.getCuidador().getNombre());
        dto.setFechaHoraInicio(turno.getFechaHoraInicio());
        dto.setFechaHoraFin(turno.getFechaHoraFin());
        dto.setEstado(turno.getEstado());
        return dto;
    }
}
