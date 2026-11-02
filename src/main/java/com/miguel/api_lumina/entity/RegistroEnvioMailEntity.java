package com.miguel.api_lumina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_envio_mail")
public class RegistroEnvioMailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Long idEnvio;

    @Column(name = "remitente", nullable = false, length = 100)
    private String remitente = "cuidadores.ies@gmail.com";

    @Column(name = "destinatario", nullable = false, length = 100)
    private String destinatario;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @Column(name = "cuerpo", columnDefinition = "TEXT", nullable = false)
    private String cuerpo;

    @Column(name = "token", length = 100)
    private String token;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "REGISTRADO";

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    public RegistroEnvioMailEntity() {
    }

    public Long getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(Long idEnvio) {
        this.idEnvio = idEnvio;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}
