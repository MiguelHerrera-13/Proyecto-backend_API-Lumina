package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AsignacionProfesionalResponseDTO;
import com.miguel.api_lumina.dto.HistorialDTO;
import com.miguel.api_lumina.dto.PacienteInactivoResponseDTO;
import com.miguel.api_lumina.dto.PacienteResponseDTO;
import com.miguel.api_lumina.dto.TratamientoResponseDTO;
import com.miguel.api_lumina.dto.TurnoResponseDTO;
import com.miguel.api_lumina.dto.UsuarioResponseDTO;
import com.miguel.api_lumina.entity.AsignacionProfesionalPacienteEntity;
import com.miguel.api_lumina.entity.PacienteEntity;
import com.miguel.api_lumina.entity.TipoAsignacionProfesional;
import com.miguel.api_lumina.entity.TratamientoEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.AsignacionProfesionalPacienteRepository;
import com.miguel.api_lumina.repository.PacienteRepository;
import com.miguel.api_lumina.repository.TratamientoRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionProfesionalPacienteRepository asignacionProfesionalPacienteRepository;

    public PacienteService(
            PacienteRepository pacienteRepository,
            TurnoRepository turnoRepository,
            TratamientoRepository tratamientoRepository,
            UsuarioRepository usuarioRepository,
            AsignacionProfesionalPacienteRepository asignacionProfesionalPacienteRepository) {

        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.asignacionProfesionalPacienteRepository = asignacionProfesionalPacienteRepository;
    }

    @Transactional
    public PacienteEntity registrarPaciente(PacienteEntity paciente) {
        return pacienteRepository.save(paciente);
    }

    public Optional<PacienteEntity> obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id);
    }

    @Transactional
    public void darDeBajaPaciente(Long id) {
        darDeBajaPaciente(id, "Baja administrativa", null);
    }

    @Transactional
    public void darDeBajaPaciente(Long id, String motivo, String usuario) {
        PacienteEntity paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El paciente con ID " + id + " no existe"));

        String motivoFinal = (motivo != null && !motivo.isBlank()) ? motivo.trim() : "Baja administrativa";
        String usuarioFinal = (usuario != null && !usuario.isBlank()) ? usuario.trim() : "ADMIN";

        pacienteRepository.darDeBajaConAuditoria(id, motivoFinal, LocalDateTime.now(), usuarioFinal);
    }

    @Transactional
    public PacienteResponseDTO reactivarPaciente(Long id) {
        PacienteEntity paciente = pacienteRepository.findByIdAnyStatus(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El paciente con ID " + id + " no existe"));

        if (Boolean.TRUE.equals(paciente.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El paciente con ID " + id + " ya se encuentra activo");
        }

        pacienteRepository.reactivarPacienteById(id);
        paciente.setActivo(true);
        paciente.setMotivoBaja(null);
        paciente.setFechaBaja(null);
        paciente.setUsuarioBaja(null);

        return mapearPaciente(paciente);
    }

    @Transactional(readOnly = true)
    public List<PacienteInactivoResponseDTO> obtenerPacientesInactivos() {
        return pacienteRepository.findPacientesInactivos()
                .stream()
                .map(p -> new PacienteInactivoResponseDTO(
                        p.getIdPaciente(),
                        p.getNombreCompleto(),
                        p.getFechaNacimiento(),
                        p.getDiagnosticoFase(),
                        p.getActivo(),
                        p.getMotivoBaja(),
                        p.getFechaBaja(),
                        p.getUsuarioBaja()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HistorialDTO obtenerHistorial(Long idPaciente) {

        PacienteEntity paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El paciente con ID " + idPaciente + " no existe"
                ));

        List<TurnoEntity> turnos = turnoRepository
                .findByPaciente_IdPaciente(idPaciente)
                .stream()
                .filter(turno ->
                        turno.getFechaHoraFin() != null
                                && turno.getFechaHoraFin().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        List<TratamientoEntity> tratamientos =
                tratamientoRepository.findByPaciente_IdPaciente(idPaciente);

        PacienteResponseDTO pacienteDTO = mapearPaciente(paciente);

        List<TurnoResponseDTO> turnosDTO = turnos.stream()
                .map(this::mapearTurno)
                .collect(Collectors.toList());

        List<TratamientoResponseDTO> tratamientosDTO = tratamientos.stream()
                .map(this::mapearTratamiento)
                .collect(Collectors.toList());

        Set<Long> idsUsuarios = new HashSet<>();

        for (TurnoEntity turno : turnos) {
            if (turno.getCuidador() != null) {
                idsUsuarios.add(turno.getCuidador().getIdUsuario());
            }
        }

        for (TratamientoEntity tratamiento : tratamientos) {
            if (tratamiento.getMedico() != null) {
                idsUsuarios.add(tratamiento.getMedico().getIdUsuario());
            }
        }

        List<UsuarioResponseDTO> usuariosDTO = usuarioRepository
                .findAllById(idsUsuarios)
                .stream()
                .map(this::mapearUsuario)
                .collect(Collectors.toList());

        return new HistorialDTO(
                pacienteDTO,
                usuariosDTO,
                turnosDTO,
                tratamientosDTO
        );
    }

    @Transactional
    public AsignacionProfesionalResponseDTO asignarMedicoCabecera(
            Long idPaciente,
            Long idUsuario) {

        PacienteEntity paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new IllegalArgumentException("El paciente no existe"));

        UsuarioEntity medico = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (medico.getRol() == null || !"MEDICO".equals(medico.getRol())) {
            throw new IllegalArgumentException("El usuario indicado no tiene rol MEDICO");
        }

        Optional<AsignacionProfesionalPacienteEntity> asignacionActual =
                asignacionProfesionalPacienteRepository
                        .findFirstByPaciente_IdPacienteAndTipoAsignacionAndActivaTrue(
                                idPaciente,
                                TipoAsignacionProfesional.MEDICO_CABECERA
                        );

        if (asignacionActual.isPresent()) {
            AsignacionProfesionalPacienteEntity actual = asignacionActual.get();

            if (actual.getUsuario().getIdUsuario().equals(idUsuario)) {
                return mapearAsignacion(actual);
            }

            actual.setActiva(false);
            actual.setFechaHasta(LocalDateTime.now());
            asignacionProfesionalPacienteRepository.save(actual);
        }

        AsignacionProfesionalPacienteEntity nuevaAsignacion =
                new AsignacionProfesionalPacienteEntity();

        nuevaAsignacion.setPaciente(paciente);
        nuevaAsignacion.setUsuario(medico);
        nuevaAsignacion.setTipoAsignacion(TipoAsignacionProfesional.MEDICO_CABECERA);
        nuevaAsignacion.setFechaDesde(LocalDateTime.now());
        nuevaAsignacion.setActiva(true);

        AsignacionProfesionalPacienteEntity guardada =
                asignacionProfesionalPacienteRepository.save(nuevaAsignacion);

        return mapearAsignacion(guardada);
    }

    @Transactional
    public AsignacionProfesionalResponseDTO agregarCuidador(
            Long idPaciente,
            Long idUsuario) {

        PacienteEntity paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new IllegalArgumentException("El paciente no existe"));

        UsuarioEntity cuidador = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (cuidador.getRol() == null || !"CUIDADOR".equals(cuidador.getRol())) {
            throw new IllegalArgumentException("El usuario indicado no tiene rol CUIDADOR");
        }

        boolean yaAsignado =
                asignacionProfesionalPacienteRepository
                        .existsByPaciente_IdPacienteAndUsuario_IdUsuarioAndTipoAsignacionAndActivaTrue(
                                idPaciente,
                                idUsuario,
                                TipoAsignacionProfesional.CUIDADOR
                        );

        if (yaAsignado) {
            throw new IllegalArgumentException("El cuidador ya se encuentra asignado al paciente");
        }

        AsignacionProfesionalPacienteEntity nuevaAsignacion =
                new AsignacionProfesionalPacienteEntity();

        nuevaAsignacion.setPaciente(paciente);
        nuevaAsignacion.setUsuario(cuidador);
        nuevaAsignacion.setTipoAsignacion(TipoAsignacionProfesional.CUIDADOR);
        nuevaAsignacion.setFechaDesde(LocalDateTime.now());
        nuevaAsignacion.setActiva(true);

        AsignacionProfesionalPacienteEntity guardada =
                asignacionProfesionalPacienteRepository.save(nuevaAsignacion);

        return mapearAsignacion(guardada);
    }

    private AsignacionProfesionalResponseDTO mapearAsignacion(
            AsignacionProfesionalPacienteEntity asignacion) {

        return new AsignacionProfesionalResponseDTO(
                asignacion.getIdAsignacion(),
                asignacion.getPaciente().getIdPaciente(),
                asignacion.getUsuario().getIdUsuario(),
                asignacion.getUsuario().getNombre(),
                asignacion.getTipoAsignacion(),
                asignacion.getFechaDesde(),
                asignacion.getFechaHasta(),
                asignacion.getActiva()
        );
    }

    private PacienteResponseDTO mapearPaciente(PacienteEntity paciente) {

        return new PacienteResponseDTO(
                paciente.getIdPaciente(),
                paciente.getNombreCompleto(),
                paciente.getFechaNacimiento(),
                paciente.getDiagnosticoFase(),
                paciente.getActivo(),
                paciente.getMotivoBaja(),
                paciente.getFechaBaja(),
                paciente.getUsuarioBaja()
        );
    }

    private TurnoResponseDTO mapearTurno(TurnoEntity turno) {

        TurnoResponseDTO dto = new TurnoResponseDTO();

        dto.setIdTurno(turno.getIdTurno());

        if (turno.getPaciente() != null) {
            dto.setNombrePaciente(turno.getPaciente().getNombreCompleto());
        }

        if (turno.getCuidador() != null) {
            dto.setNombreCuidador(turno.getCuidador().getNombre());
        }

        dto.setFechaHoraInicio(turno.getFechaHoraInicio());
        dto.setFechaHoraFin(turno.getFechaHoraFin());
        dto.setEstado(turno.getEstado());

        return dto;
    }

    private TratamientoResponseDTO mapearTratamiento(
            TratamientoEntity tratamiento) {

        TratamientoResponseDTO dto = new TratamientoResponseDTO();

        dto.setIdTratamiento(tratamiento.getIdTratamiento());

        if (tratamiento.getPaciente() != null) {
            dto.setIdPaciente(
                    tratamiento.getPaciente().getIdPaciente()
            );
            dto.setNombrePaciente(
                    tratamiento.getPaciente().getNombreCompleto()
            );
        }

        if (tratamiento.getMedico() != null) {
            dto.setIdMedico(
                    tratamiento.getMedico().getIdUsuario()
            );
            dto.setNombreMedico(
                    tratamiento.getMedico().getNombre()
            );
        }

        dto.setDescripcion(tratamiento.getDescripcion());
        dto.setFechaInicio(tratamiento.getFechaInicio());
        dto.setFechaFin(tratamiento.getFechaFin());
        dto.setActivo(tratamiento.getActivo());

        return dto;
    }

    private UsuarioResponseDTO mapearUsuario(UsuarioEntity usuario) {

        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.getActivo());

        return dto;
    }
}