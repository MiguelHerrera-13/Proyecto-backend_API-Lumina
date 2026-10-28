package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AgendaDisponibilidadResponseDTO;
import com.miguel.api_lumina.dto.CancelarTurnoDTO;
import com.miguel.api_lumina.dto.IntervaloAgendaDTO;
import com.miguel.api_lumina.dto.ReprogramarTurnoDTO;
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
        turno.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");

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

    @Transactional
    public TurnoResponseDTO cancelarTurno(Long idTurno, CancelarTurnoDTO dto) {
        if (dto == null || dto.getMotivo() == null || dto.getMotivo().trim().length() < 10) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio y debe tener al menos 10 caracteres");
        }

        TurnoEntity turno = turnoRepository.findById(idTurno)
                .orElseThrow(() -> new IllegalArgumentException("El turno no existe"));

        validarTurnoModificable(turno, "cancelar");

        turno.setEstado("CANCELADO");
        turno.setMotivoCancelacion(dto.getMotivo().trim());

        TurnoEntity actualizado = turnoRepository.save(turno);
        return mapToDTO(actualizado);
    }

    @Transactional
    public TurnoResponseDTO reprogramarTurno(Long idTurno, ReprogramarTurnoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de reprogramación son obligatorios");
        }

        if (dto.getNuevaFechaHoraInicio() == null || dto.getNuevaFechaHoraFin() == null) {
            throw new IllegalArgumentException("Las nuevas fechas de inicio y fin son obligatorias");
        }

        if (!dto.getNuevaFechaHoraInicio().isBefore(dto.getNuevaFechaHoraFin())) {
            throw new IllegalArgumentException("La nueva fecha de inicio debe ser anterior a la de fin");
        }

        if (dto.getMotivo() == null || dto.getMotivo().trim().length() < 10) {
            throw new IllegalArgumentException("El motivo de reprogramación es obligatorio y debe tener al menos 10 caracteres");
        }

        TurnoEntity turno = turnoRepository.findById(idTurno)
                .orElseThrow(() -> new IllegalArgumentException("El turno no existe"));

        validarTurnoModificable(turno, "reprogramar");

        // Validar disponibilidad horaria de médico si tiene médico asignado
        if (turno.getMedico() != null) {
            boolean solapaMedico = turnoRepository.existsSuperposicionMedico(
                    idTurno,
                    turno.getMedico().getIdUsuario(),
                    dto.getNuevaFechaHoraInicio(),
                    dto.getNuevaFechaHoraFin()
            );
            if (solapaMedico) {
                throw new IllegalStateException("El médico ya tiene un turno asignado en el horario seleccionado");
            }
        }

        // Validar disponibilidad horaria de cuidador
        if (turno.getCuidador() != null) {
            boolean solapaCuidador = turnoRepository.existsSuperposicionCuidador(
                    idTurno,
                    turno.getCuidador().getIdUsuario(),
                    dto.getNuevaFechaHoraInicio(),
                    dto.getNuevaFechaHoraFin()
            );
            if (solapaCuidador) {
                throw new IllegalStateException("El cuidador ya tiene un turno asignado en el horario seleccionado");
            }
        }

        turno.setFechaHoraInicio(dto.getNuevaFechaHoraInicio());
        turno.setFechaHoraFin(dto.getNuevaFechaHoraFin());
        turno.setMotivoReprogramacion(dto.getMotivo().trim());
        turno.setEstado("REPROGRAMADO");

        TurnoEntity actualizado = turnoRepository.save(turno);
        return mapToDTO(actualizado);
    }

    private void validarTurnoModificable(TurnoEntity turno, String accion) {
        String estado = turno.getEstado() != null ? turno.getEstado().toUpperCase() : "";

        if (estado.equals("FINALIZADO") || estado.equals("COMPLETADO")) {
            throw new IllegalStateException("No se puede " + accion + " un turno que ya ha sido finalizado");
        }

        if (estado.equals("CANCELADO")) {
            throw new IllegalStateException("No se puede " + accion + " un turno que ya se encuentra cancelado");
        }
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
        if (turno.getPaciente() != null) {
            dto.setNombrePaciente(turno.getPaciente().getNombreCompleto());
        }
        if (turno.getCuidador() != null) {
            dto.setNombreCuidador(turno.getCuidador().getNombre());
        }

        if (turno.getMedico() != null) {
            dto.setIdMedico(turno.getMedico().getIdUsuario());
            dto.setNombreMedico(turno.getMedico().getNombre());
        }

        dto.setFechaHoraInicio(turno.getFechaHoraInicio());
        dto.setFechaHoraFin(turno.getFechaHoraFin());
        dto.setEstado(turno.getEstado());
        dto.setIdRecurrencia(turno.getIdRecurrencia());
        dto.setMotivoCancelacion(turno.getMotivoCancelacion());
        dto.setMotivoReprogramacion(turno.getMotivoReprogramacion());

        return dto;
    }
}
