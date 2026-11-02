package com.miguel.api_lumina.service;

import com.miguel.api_lumina.entity.RegistroEnvioMailEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.RegistroEnvioMailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    public static final String REMITENTE_OFICIAL = "cuidadores.ies@gmail.com";

    private final RegistroEnvioMailRepository registroEnvioMailRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.server.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(RegistroEnvioMailRepository registroEnvioMailRepository) {
        this.registroEnvioMailRepository = registroEnvioMailRepository;
    }

    public void enviarCorreoVerificacion(UsuarioEntity usuario, String token) {
        String asunto = "Verificación y Activación de Cuenta - Lumina Healthcare";
        String linkActivacion = baseUrl + "/api/auth/activar?token=" + token;

        String cuerpo = String.format(
                "Hola %s,\n\n" +
                "¡Gracias por registrarte en Lumina Healthcare!\n\n" +
                "Para activar tu cuenta y poder iniciar sesión en la aplicación, por favor ingresa al siguiente enlace:\n" +
                "%s\n\n" +
                "O introduce este código de verificación en la app:\n" +
                "%s\n\n" +
                "Este enlace expirará en 24 horas.\n\n" +
                "Atentamente,\n" +
                "Equipo de Cuidadores Lumina\n" +
                "%s",
                usuario.getNombre() != null ? usuario.getNombre() : "Usuario",
                linkActivacion,
                token,
                REMITENTE_OFICIAL
        );

        // Guardar siempre el registro en la base de datos MySQL
        RegistroEnvioMailEntity registro = new RegistroEnvioMailEntity();
        registro.setRemitente(REMITENTE_OFICIAL);
        registro.setDestinatario(usuario.getEmail());
        registro.setAsunto(asunto);
        registro.setCuerpo(cuerpo);
        registro.setToken(token);
        registro.setFechaEnvio(LocalDateTime.now());
        registro.setEstado("REGISTRADO");

        // Enviar vía SMTP si JavaMailSender está disponible
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(REMITENTE_OFICIAL);
                message.setTo(usuario.getEmail());
                message.setSubject(asunto);
                message.setText(cuerpo);
                mailSender.send(message);
                registro.setEstado("ENVIADO");
                log.info("Correo de verificación enviado exitosamente desde [{}] a [{}]", REMITENTE_OFICIAL, usuario.getEmail());
            } catch (Exception e) {
                log.warn("No se pudo conectar al servidor SMTP de Gmail (se guarda en BD y log): {}", e.getMessage());
                registro.setEstado("REGISTRADO_PENDIENTE_SMTP");
            }
        } else {
            log.info("JavaMailSender no activo. Correo persistido en base de datos desde [{}] a [{}]", REMITENTE_OFICIAL, usuario.getEmail());
            registro.setEstado("REGISTRADO_EN_BD");
        }

        if (registroEnvioMailRepository != null) {
            registroEnvioMailRepository.save(registro);
        }

        log.info("==================================================");
        log.info(" CORREO DE VERIFICACIÓN REGISTRADO EN BD LUMINA ");
        log.info(" De: {}", REMITENTE_OFICIAL);
        log.info(" Para: {}", usuario.getEmail());
        log.info(" Token: {}", token);
        log.info(" Link: {}", linkActivacion);
        log.info("==================================================");
    }
}
