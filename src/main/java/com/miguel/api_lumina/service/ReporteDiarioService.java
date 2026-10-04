package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.ReporteDiarioRequestDTO;
import com.miguel.api_lumina.entity.ReporteDiarioEntity;
import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.repository.ReporteDiarioRepository;
import com.miguel.api_lumina.repository.TurnoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ReporteDiarioService {

    private final ReporteDiarioRepository reporteDiarioRepository;
    private final TurnoRepository turnoRepository;

    public ReporteDiarioService(ReporteDiarioRepository reporteDiarioRepository, TurnoRepository turnoRepository) {
        this.reporteDiarioRepository = reporteDiarioRepository;
        this.turnoRepository = turnoRepository;
    }

    @Transactional
    public ReporteDiarioEntity registrarReporte(ReporteDiarioRequestDTO requestDTO) {
        // Validar que el turno exista (Punto 9)
        TurnoEntity turno = turnoRepository.findById(requestDTO.getIdTurno())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El turno con ID " + requestDTO.getIdTurno() + " no existe"));

        ReporteDiarioEntity entity = new ReporteDiarioEntity();
        entity.setTurno(turno);
        entity.setTemperatura(requestDTO.getTemperatura());
        entity.setPresionArterial(requestDTO.getPresionArterial());
        entity.setObservacion(requestDTO.getObservacion());

        return reporteDiarioRepository.save(entity);
    }

    public List<ReporteDiarioEntity> obtenerReportesPorTurno(Long idTurno) {
        // Validar que el turno exista (Punto 9)
        if (!turnoRepository.existsById(idTurno)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El turno con ID " + idTurno + " no existe");
        }
        return reporteDiarioRepository.findByTurno_IdTurno(idTurno);
    }

    public Optional<ReporteDiarioEntity> obtenerReportePorId(Long id) {
        return reporteDiarioRepository.findById(id);
    }
}
