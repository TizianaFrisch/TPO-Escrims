package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import jakarta.persistence.*;

@Entity
public class ReporteConducta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Scrim scrim;

    @ManyToOne(optional = false)
    private Usuario reportado;

    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    private String sancion; // opcional

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Scrim getScrim() { return scrim; }
    public void setScrim(Scrim scrim) { this.scrim = scrim; }
    public Usuario getReportado() { return reportado; }
    public void setReportado(Usuario reportado) { this.reportado = reportado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }
    public String getSancion() { return sancion; }
    public void setSancion(String sancion) { this.sancion = sancion; }
}
