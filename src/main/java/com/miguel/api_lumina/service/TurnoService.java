package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.CancelarTurnoDTO;
import com.miguel.api_lumina.dto.ReprogramarTurnoDTO;
import com.miguel.api_lumina.dto.TurnoFiltroDTO;
import com.miguel.api_lumina.dto.TurnoRequestDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import com.miguel.api_lumina.specification.TurnoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public Page<TurnoResponseDTO> buscarTurnos(
            Long pacienteId,
            Long profesionalId,
            Long medicoId,
            Long cuidadorId,
            String estado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable
    ) {
        Specification<TurnoEntity> spec = TurnoSpecification.conFiltros(
                pacienteId, profesionalId, medicoId, cuidadorId, estado, fechaDesde, fechaHasta
        );
        Page<TurnoEntity> turnosPage = turnoRepository.findAll(spec, pageable);
        return turnosPage.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TurnoResponseDTO> buscarTurnos(TurnoFiltroDTO filtro, Pageable pageable) {
        if (filtro == null) {
            filtro = new TurnoFiltroDTO();
        }
        return buscarTurnos(
                filtro.getPacienteId(),
                filtro.getProfesionalId(),
                filtro.getMedicoId(),
                filtro.getCuidadorId(),
                filtro.getEstado(),
                filtro.getFechaDesde(),
                filtro.getFechaHasta(),
                pageable
        );
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
        dto.setMotivoCancelacion(turno.getMotivoCancelacion());
        dto.setMotivoReprogramacion(turno.getMotivoReprogramacion());

        return dto;
    }
}
