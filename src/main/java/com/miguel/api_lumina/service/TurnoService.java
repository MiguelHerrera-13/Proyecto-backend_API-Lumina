package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AgendaDisponibilidadResponseDTO;
import com.miguel.api_lumina.dto.IntervaloAgendaDTO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        if (dto.getIdMedico() == null) {
            throw new IllegalArgumentException("El médico es obligatorio");
        }

        UsuarioEntity medico = usuarioRepository.findById(dto.getIdMedico())
                .orElseThrow(() -> new IllegalArgumentException("El médico no existe"));

        if (medico.getRol() != null && !medico.getRol().equalsIgnoreCase("MEDICO")) {
            throw new IllegalArgumentException("El usuario indicado no tiene rol de MEDICO");
        }

        TurnoEntity turno = new TurnoEntity();
        turno.setPaciente(paciente);
        turno.setCuidador(cuidador);
        turno.setMedico(medico);
        turno.setFechaHoraInicio(dto.getFechaHoraInicio());
        turno.setFechaHoraFin(dto.getFechaHoraFin());
        turno.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");

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
        List<TurnoEntity> turnos = turnoRepository.findByCuidador_IdUsuario(idCuidador);
        return turnos.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgendaDisponibilidadResponseDTO obtenerAgendaDisponibilidad(Long profesionalId, LocalDateTime desde, LocalDateTime hasta) {
        // Criterio de Aceptación: Parámetros desde y hasta obligatorios
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parámetros 'desde' y 'hasta' son obligatorios");
        }

        // Criterio de Aceptación: Validación de que desde sea anterior a hasta
        if (!desde.isBefore(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' debe ser estrictamente anterior a la fecha 'hasta'");
        }

        UsuarioEntity profesional = null;
        if (profesionalId != null) {
            profesional = usuarioRepository.findById(profesionalId)
                    .orElseThrow(() -> new IllegalArgumentException("El profesional indicado no existe"));
        }

        // Obtener turnos ordenados cronológicamente
        List<TurnoEntity> turnos = turnoRepository.findAgendaProfesional(profesionalId, desde, hasta);
        List<TurnoResponseDTO> turnosDTO = turnos.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Calcular huecos libres de agenda cronológicamente
        List<IntervaloAgendaDTO> huecosLibres = new ArrayList<>();
        LocalDateTime cursor = desde;

        for (TurnoEntity t : turnos) {
            LocalDateTime inicioTurno = t.getFechaHoraInicio();
            LocalDateTime finTurno = t.getFechaHoraFin();

            if (inicioTurno.isAfter(cursor)) {
                LocalDateTime finHueco = inicioTurno.isBefore(hasta) ? inicioTurno : hasta;
                if (cursor.isBefore(finHueco)) {
                    huecosLibres.add(new IntervaloAgendaDTO(cursor, finHueco, "LIBRE", null, "Disponibilidad libre"));
                }
            }

            if (finTurno.isAfter(cursor)) {
                cursor = finTurno;
            }
        }

        if (cursor.isBefore(hasta)) {
            huecosLibres.add(new IntervaloAgendaDTO(cursor, hasta, "LIBRE", null, "Disponibilidad libre"));
        }

        AgendaDisponibilidadResponseDTO response = new AgendaDisponibilidadResponseDTO();
        response.setProfesionalId(profesionalId);
        if (profesional != null) {
            response.setNombreProfesional(profesional.getNombre());
            response.setRolProfesional(profesional.getRol());
        }
        response.setDesde(desde);
        response.setHasta(hasta);
        response.setTurnosOcupados(turnosDTO);
        response.setHuecosLibres(huecosLibres);

        return response;
    }

    private TurnoResponseDTO mapToDTO(TurnoEntity turno) {
        TurnoResponseDTO dto = new TurnoResponseDTO();

        dto.setIdTurno(turno.getIdTurno());
        dto.setNombrePaciente(turno.getPaciente().getNombreCompleto());
        dto.setNombreCuidador(turno.getCuidador().getNombre());

        if (turno.getMedico() != null) {
            dto.setIdMedico(turno.getMedico().getIdUsuario());
            dto.setNombreMedico(turno.getMedico().getNombre());
        }

        dto.setFechaHoraInicio(turno.getFechaHoraInicio());
        dto.setFechaHoraFin(turno.getFechaHoraFin());
        dto.setEstado(turno.getEstado());

        return dto;
    }
}
