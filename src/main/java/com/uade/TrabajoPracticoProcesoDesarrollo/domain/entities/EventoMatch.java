package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_match")
public class EventoMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipo = TipoEvento.OTRO;

    private LocalDateTime momento;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    public enum TipoEvento { KILL, TORRE, DRAGON, BARON, INHIBIDOR, OBJETIVO, PAUSA, REANUDACION, RENDICION, OTRO }

    @PrePersist
    protected void onCreate(){ if (momento == null) momento = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }
    public LocalDateTime getMomento() { return momento; }
    public void setMomento(LocalDateTime momento) { this.momento = momento; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
