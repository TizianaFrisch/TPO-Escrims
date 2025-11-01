package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WaitlistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Scrim scrim;

    @ManyToOne(optional = false)
    private Usuario usuario;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Scrim getScrim() { return scrim; }
    public void setScrim(Scrim scrim) { this.scrim = scrim; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
