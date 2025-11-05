package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.CanalNotificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;
import jakarta.persistence.*;

@Entity
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    @Enumerated(EnumType.STRING)
    private CanalNotificacion canal;

    @Column(length = 2000)
    private String payload;

    private String destinatario;

    private boolean leida = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }
    public CanalNotificacion getCanal() { return canal; }
    public void setCanal(CanalNotificacion canal) { this.canal = canal; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}
