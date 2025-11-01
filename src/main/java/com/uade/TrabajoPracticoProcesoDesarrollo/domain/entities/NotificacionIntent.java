package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.CanalNotificacion;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class NotificacionIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificacionId;

    @Enumerated(EnumType.STRING)
    private CanalNotificacion canal;

    @Column(length = 2000)
    private String payload;

    private String destinatario;

    private boolean success;

    private String errorMessage;

    private LocalDateTime intentoAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNotificacionId() { return notificacionId; }
    public void setNotificacionId(Long notificacionId) { this.notificacionId = notificacionId; }
    public CanalNotificacion getCanal() { return canal; }
    public void setCanal(CanalNotificacion canal) { this.canal = canal; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getIntentoAt() { return intentoAt; }
    public void setIntentoAt(LocalDateTime intentoAt) { this.intentoAt = intentoAt; }
}
