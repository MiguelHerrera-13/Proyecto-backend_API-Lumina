package com.miguel.api_lumina.config;

import com.miguel.api_lumina.entity.TurnoEntity;
import com.miguel.api_lumina.repository.TurnoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TurnoRepository turnoRepository;

    public DataInitializer(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Override
    public void run(String... args) {
        if (turnoRepository.count() == 0) {
            TurnoEntity turno = new TurnoEntity();
            turno.setEstado("ACTIVO");
            turno.setFechaHoraInicio(LocalDateTime.now().minusHours(2));
            turno.setFechaHoraFin(LocalDateTime.now().plusHours(6));
            turnoRepository.save(turno);
            System.out.println("==================================================");
            System.out.println(">>> [DATOS DE PRUEBA] Turno creado con éxito con ID: " + turno.getIdTurno());
            System.out.println("==================================================");
        }
    }
}
