package com.miguel.api_lumina.service;

import com.miguel.api_lumina.dto.AuthResponseDTO;
import com.miguel.api_lumina.dto.LoginRequestDTO;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.UsuarioRepository;
import com.miguel.api_lumina.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        UsuarioEntity usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String storedHash = usuario.getPasswordHash();
        boolean passwordMatches = false;

        // Comprobar si la contraseña almacenada ya tiene formato BCrypt estándar ($2a$, $2b$, $2y$)
        if (isBcryptHash(storedHash)) {
            passwordMatches = passwordEncoder.matches(request.getPassword(), storedHash);
        } else if (storedHash != null && storedHash.equals(request.getPassword())) {
            // Estrategia de transición segura (Lazy Migration):
            // Si la cuenta existía con contraseña previa y coincide exactamente,
            // se actualiza de forma transparente e inmediata a hash seguro BCrypt.
            passwordMatches = true;
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            usuarioRepository.save(usuario);
        }

        if (!passwordMatches) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getIdUsuario(), usuario.getRol());
        return new AuthResponseDTO(token, "Bearer", usuario.getEmail(), usuario.getRol());
    }

    private boolean isBcryptHash(String hash) {
        if (hash == null || hash.length() != 60) {
            return false;
        }
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
