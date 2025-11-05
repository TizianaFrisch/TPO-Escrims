package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_usuario")
public class HistorialUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Resultado resultado = Resultado.PENDIENTE;

    private Integer mmrAntes;
    private Integer mmrDespues;
    private LocalDateTime fechaRegistro;

    public enum Resultado { VICTORIA, DERROTA, EMPATE, ABANDONO, PENDIENTE }

    @PrePersist
    protected void onCreate(){ if (fechaRegistro == null) fechaRegistro = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public Resultado getResultado() { return resultado; }
    public void setResultado(Resultado resultado) { this.resultado = resultado; }
    public Integer getMmrAntes() { return mmrAntes; }
    public void setMmrAntes(Integer mmrAntes) { this.mmrAntes = mmrAntes; }
    public Integer getMmrDespues() { return mmrDespues; }
    public void setMmrDespues(Integer mmrDespues) { this.mmrDespues = mmrDespues; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
