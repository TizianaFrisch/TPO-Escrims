package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "estadisticas_jugador_match")
public class EstadisticaJugadorMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    private Integer kills;
    private Integer muertes;
    private Integer asistencias;
    private Integer minions;
    private Integer oro;
    private Integer danoCausado;
    private Integer danoRecibido;
    private Integer torres;
    private Integer objetivos;

    public Long getId() { return id; }
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Integer getKills() { return kills; }
    public void setKills(Integer kills) { this.kills = kills; }
    public Integer getMuertes() { return muertes; }
    public void setMuertes(Integer muertes) { this.muertes = muertes; }
    public Integer getAsistencias() { return asistencias; }
    public void setAsistencias(Integer asistencias) { this.asistencias = asistencias; }
    public Integer getMinions() { return minions; }
    public void setMinions(Integer minions) { this.minions = minions; }
    public Integer getOro() { return oro; }
    public void setOro(Integer oro) { this.oro = oro; }
    public Integer getDanoCausado() { return danoCausado; }
    public void setDanoCausado(Integer danoCausado) { this.danoCausado = danoCausado; }
    public Integer getDanoRecibido() { return danoRecibido; }
    public void setDanoRecibido(Integer danoRecibido) { this.danoRecibido = danoRecibido; }
    public Integer getTorres() { return torres; }
    public void setTorres(Integer torres) { this.torres = torres; }
    public Integer getObjetivos() { return objetivos; }
    public void setObjetivos(Integer objetivos) { this.objetivos = objetivos; }
}
