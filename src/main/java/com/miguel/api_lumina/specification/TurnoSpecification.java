package com.miguel.api_lumina.specification;

import com.miguel.api_lumina.entity.TurnoEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TurnoSpecification {

    public static Specification<TurnoEntity> conFiltros(
            Long pacienteId,
            Long profesionalId,
            Long medicoId,
            Long cuidadorId,
            String estado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (pacienteId != null) {
                predicates.add(cb.equal(root.get("paciente").get("idPaciente"), pacienteId));
            }

            if (profesionalId != null) {
                Predicate esMedico = cb.equal(root.get("medico").get("idUsuario"), profesionalId);
                Predicate esCuidador = cb.equal(root.get("cuidador").get("idUsuario"), profesionalId);
                predicates.add(cb.or(esMedico, esCuidador));
            }

            if (medicoId != null) {
                predicates.add(cb.equal(root.get("medico").get("idUsuario"), medicoId));
            }

            if (cuidadorId != null) {
                predicates.add(cb.equal(root.get("cuidador").get("idUsuario"), cuidadorId));
            }

            if (estado != null && !estado.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("estado")), estado.trim().toUpperCase()));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHoraInicio"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaHoraInicio"), fechaHasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

