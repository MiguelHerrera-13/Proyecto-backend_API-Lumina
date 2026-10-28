package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.TurnoConflictoDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteRequestDTO;
import com.miguel.api_lumina.dto.TurnoRecurrenteResponseDTO;
import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${turnos.recurrencia.max-meses:3}")
    private int maxMesesRecurrencia = 3;

    public TurnoService(TurnoRepository turnoRepository, PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository) {
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void setMaxMesesRecurrencia(int maxMesesRecurrencia) {
        this.maxMesesRecurrencia = maxMesesRecurrencia;
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
        turno.setEstado(dto.getEstado());

        TurnoEntity turnoGuardado = turnoRepository.save(turno);

        return mapToDTO(turnoGuardado);
    }

    @Transactional
    public TurnoRecurrenteResponseDTO generarTurnosRecurrentes(TurnoRecurrenteRequestDTO dto) {
        if (dto.getFechaHoraInicio() == null || dto.getFechaHoraFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }

        if (!dto.getFechaHoraInicio().isBefore(dto.getFechaHoraFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la de fin");
        }

        if (dto.getFechaLimite() == null) {
            throw new IllegalArgumentException("La fecha límite de recurrencia es obligatoria");
        }

        LocalDate fechaInicioDate = dto.getFechaHoraInicio().toLocalDate();
        if (dto.getFechaLimite().isBefore(fechaInicioDate)) {
            throw new IllegalArgumentException("La fecha límite no puede ser anterior a la fecha de inicio");
        }

        LocalDate fechaMaxima = fechaInicioDate.plusMonths(maxMesesRecurrencia);
        if (dto.getFechaLimite().isAfter(fechaMaxima)) {
            throw new IllegalArgumentException("La fecha límite no puede superar el límite máximo de " + maxMesesRecurrencia + " meses");
        }

        String patron = dto.getPatronRecurrencia() != null ? dto.getPatronRecurrencia().trim().toUpperCase() : "";
        if (!"DIARIO".equals(patron) && !"SEMANAL".equals(patron)) {
            throw new IllegalArgumentException("Patrón de recurrencia inválido: " + dto.getPatronRecurrencia() + ". Valores permitidos: DIARIO, SEMANAL");
        }

        PacienteEntity paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new IllegalArgumentException("El paciente no existe"));

        UsuarioEntity cuidador = usuarioRepository.findById(dto.getIdCuidador())
                .orElseThrow(() -> new IllegalArgumentException("El cuidador no existe"));

        UsuarioEntity medico = usuarioRepository.findById(dto.getIdMedico())
                .orElseThrow(() -> new IllegalArgumentException("El médico no existe"));

        if (medico.getRol() != null && !medico.getRol().equalsIgnoreCase("MEDICO")) {
            throw new IllegalArgumentException("El usuario indicado no tiene rol de MEDICO");
        }

        Duration duracion = Duration.between(dto.getFechaHoraInicio(), dto.getFechaHoraFin());
        String idRecurrencia = UUID.randomUUID().toString();
        String estado = (dto.getEstado() != null && !dto.getEstado().isBlank()) ? dto.getEstado().trim() : "ASIGNADO";

        List<TurnoResponseDTO> turnosCreados = new ArrayList<>();
        List<TurnoConflictoDTO> turnosOmitidos = new ArrayList<>();

        LocalDateTime currentInicio = dto.getFechaHoraInicio();

        while (!currentInicio.toLocalDate().isAfter(dto.getFechaLimite())) {
            LocalDateTime currentFin = currentInicio.plus(duracion);

            if (turnoRepository.existeSolapamientoCuidador(dto.getIdCuidador(), currentInicio, currentFin)) {
                turnosOmitidos.add(new TurnoConflictoDTO(
                        currentInicio,
                        currentFin,
                        "Solapamiento con turno existente del cuidador"
                ));
            } else if (turnoRepository.existeSolapamientoPaciente(dto.getIdPaciente(), currentInicio, currentFin)) {
                turnosOmitidos.add(new TurnoConflictoDTO(
                        currentInicio,
                        currentFin,
                        "Solapamiento con turno existente del paciente"
                ));
            } else {
                TurnoEntity turno = new TurnoEntity();
                turno.setPaciente(paciente);
                turno.setCuidador(cuidador);
                turno.setMedico(medico);
                turno.setFechaHoraInicio(currentInicio);
                turno.setFechaHoraFin(currentFin);
                turno.setEstado(estado);
                turno.setIdRecurrencia(idRecurrencia);
                turno.setActivo(true);

                TurnoEntity guardado = turnoRepository.save(turno);
                turnosCreados.add(mapToDTO(guardado));
            }

            if ("DIARIO".equals(patron)) {
                currentInicio = currentInicio.plusDays(1);
            } else {
                currentInicio = currentInicio.plusWeeks(1);
            }
        }

        return new TurnoRecurrenteResponseDTO(
                idRecurrencia,
                patron,
                turnosCreados.size(),
                turnosOmitidos.size(),
                turnosCreados,
                turnosOmitidos
        );
    }

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> obtenerTurnosPorRecurrencia(String idRecurrencia) {
        return turnoRepository.findByIdRecurrencia(idRecurrencia)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
        dto.setIdRecurrencia(turno.getIdRecurrencia());

        return dto;
    }
}
