package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id")
    private Scrim scrim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_ganador_id")
    private Equipo equipoGanador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_perdedor_id")
    private Equipo equipoPerdedor;

    private Integer duracionMinutos;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatch estado = EstadoMatch.EN_PROGRESO;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private Long mvpId;

    private Integer killsEquipoGanador;
    private Integer killsEquipoPerdedor;
    private Integer torresDestruidasGanador;
    private Integer torresDestruidasPerdedor;
    private Integer goldDiferencia;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadisticaJugadorMatch> estadisticasJugadores = new ArrayList<>();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventoMatch> eventos = new ArrayList<>();

    public enum EstadoMatch { EN_PROGRESO, FINALIZADO, ABANDONADO, CANCELADO_POR_MODERADOR }

    @PrePersist
    protected void onCreate(){ if (fechaInicio == null) fechaInicio = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Scrim getScrim() { return scrim; }
    public void setScrim(Scrim scrim) { this.scrim = scrim; }
    public Equipo getEquipoGanador() { return equipoGanador; }
    public void setEquipoGanador(Equipo equipoGanador) { this.equipoGanador = equipoGanador; }
    public Equipo getEquipoPerdedor() { return equipoPerdedor; }
    public void setEquipoPerdedor(Equipo equipoPerdedor) { this.equipoPerdedor = equipoPerdedor; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public EstadoMatch getEstado() { return estado; }
    public void setEstado(EstadoMatch estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Long getMvpId() { return mvpId; }
    public void setMvpId(Long mvpId) { this.mvpId = mvpId; }
    public Integer getKillsEquipoGanador() { return killsEquipoGanador; }
    public void setKillsEquipoGanador(Integer killsEquipoGanador) { this.killsEquipoGanador = killsEquipoGanador; }
    public Integer getKillsEquipoPerdedor() { return killsEquipoPerdedor; }
    public void setKillsEquipoPerdedor(Integer killsEquipoPerdedor) { this.killsEquipoPerdedor = killsEquipoPerdedor; }
    public Integer getTorresDestruidasGanador() { return torresDestruidasGanador; }
    public void setTorresDestruidasGanador(Integer torresDestruidasGanador) { this.torresDestruidasGanador = torresDestruidasGanador; }
    public Integer getTorresDestruidasPerdedor() { return torresDestruidasPerdedor; }
    public void setTorresDestruidasPerdedor(Integer torresDestruidasPerdedor) { this.torresDestruidasPerdedor = torresDestruidasPerdedor; }
    public Integer getGoldDiferencia() { return goldDiferencia; }
    public void setGoldDiferencia(Integer goldDiferencia) { this.goldDiferencia = goldDiferencia; }
    public List<EstadisticaJugadorMatch> getEstadisticasJugadores() { return estadisticasJugadores; }
    public List<EventoMatch> getEventos() { return eventos; }
}
