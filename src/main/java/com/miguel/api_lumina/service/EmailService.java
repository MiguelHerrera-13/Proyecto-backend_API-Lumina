package com.miguel.api_lumina.service;

import com.miguel.api_lumina.entity.RegistroEnvioMailEntity;
import com.miguel.api_lumina.entity.UsuarioEntity;
import com.miguel.api_lumina.repository.RegistroEnvioMailRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
        String nombre = usuario.getNombre() != null ? usuario.getNombre() : "Usuario";

        String htmlCuerpo = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; margin: 0; padding: 24px;'>"
                + "<div style='max-width: 520px; margin: 0 auto; background: #FFFFFF; border-radius: 16px; padding: 32px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); border: 1px solid #E2E8F0;'>"
                + "  <div style='text-align: center; margin-bottom: 24px;'>"
                + "    <h1 style='color: #0D9488; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: -0.5px;'>Lúmina</h1>"
                + "    <p style='color: #64748B; font-size: 13px; margin: 4px 0 0 0;'>Plataforma de Cuidados y Gestión Médica</p>"
                + "  </div>"
                + "  <h2 style='color: #0F172A; font-size: 18px; margin-top: 0;'>¡Hola, " + nombre + "!</h2>"
                + "  <p style='color: #334155; font-size: 14px; line-height: 1.6;'>"
                + "    Gracias por registrarte en el sistema clínico <b>Lúmina</b>. Para activar tu cuenta de inmediato, ingresa el siguiente código de verificación en la aplicación móvil:"
                + "  </p>"
                + "  <div style='background-color: #F0FDFA; border: 2px dashed #0D9488; border-radius: 14px; padding: 20px; text-align: center; margin: 20px 0;'>"
                + "    <span style='color: #0D9488; font-size: 12px; font-weight: 800; text-transform: uppercase; letter-spacing: 1px;'>Código de Activación</span>"
                + "    <p style='margin: 8px 0; font-size: 32px; font-weight: 900; letter-spacing: 8px; color: #0F766E; font-family: monospace;'>" + token + "</p>"
                + "    <p style='margin: 0; color: #475569; font-size: 12px;'>📱 Abre la app Lúmina en tu celular e introduce este código para activar tu cuenta al instante.</p>"
                + "  </div>"
                + "  <p style='color: #64748B; font-size: 13px; text-align: center; margin-top: 24px; margin-bottom: 8px;'>O si estás conectado en la misma red Wi-Fi médica:</p>"
                + "  <div style='text-align: center; margin-bottom: 24px;'>"
                + "    <a href='" + linkActivacion + "' style='background-color: #0D9488; color: #FFFFFF; padding: 12px 24px; text-decoration: none; border-radius: 10px; font-weight: bold; font-size: 14px; display: inline-block; box-shadow: 0 4px 12px rgba(13,148,136,0.25);'>Activar desde Navegador</a>"
                + "  </div>"
                + "  <hr style='border: none; border-top: 1px solid #E2E8F0; margin: 24px 0;'/>"
                + "  <p style='color: #94A3B8; font-size: 11px; margin: 0; text-align: center;'>"
                + "    Este correo de seguridad fue enviado desde <b>" + REMITENTE_OFICIAL + "</b>.<br/>"
                + "    Si no solicitaste esta cuenta, puedes desestimar este mensaje."
                + "  </p>"
                + "</div>"
                + "</body></html>";

        // Guardar siempre el registro en la base de datos MySQL
        RegistroEnvioMailEntity registro = new RegistroEnvioMailEntity();
        registro.setRemitente(REMITENTE_OFICIAL);
        registro.setDestinatario(usuario.getEmail());
        registro.setAsunto(asunto);
        registro.setCuerpo(htmlCuerpo);
        registro.setToken(token);
        registro.setFechaEnvio(LocalDateTime.now());
        registro.setEstado("REGISTRADO");

        // Enviar vía SMTP si JavaMailSender está disponible
        if (mailSender != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(REMITENTE_OFICIAL);
                helper.setTo(usuario.getEmail());
                helper.setSubject(asunto);
                helper.setText(htmlCuerpo, true); // true = HTML
                mailSender.send(mimeMessage);
                registro.setEstado("ENVIADO");
                log.info("Correo de verificación HTML con botón enviado exitosamente desde [{}] a [{}]", REMITENTE_OFICIAL, usuario.getEmail());
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
