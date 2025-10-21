package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;

@Entity
public class Confirmacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Scrim scrim;

    @ManyToOne(optional = false)
    private Usuario usuario;

    private boolean confirmado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Scrim getScrim() { return scrim; }
    public void setScrim(Scrim scrim) { this.scrim = scrim; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }
}
