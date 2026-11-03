package com.miguel.api_lumina.service;

import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final int MAX_INTENTOS = 5;
    private static final long MINUTOS_BLOQUEO = 15;

    private final UsuarioRepository usuarioRepository;

    public LoginAttemptService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra un intento fallido en su propia transacción independiente.
     * Al usar REQUIRES_NEW, el guardado persiste aunque la transacción del login haga rollback.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntentoFallido(UsuarioEntity usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAX_INTENTOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Resetea el contador al loguearse correctamente.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetearIntentos(UsuarioEntity usuario) {
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);
    }

    public int getMaxIntentos() {
        return MAX_INTENTOS;
    }
}
