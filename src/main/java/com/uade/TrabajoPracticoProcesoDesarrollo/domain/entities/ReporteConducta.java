package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.MotivoReporte;
import jakarta.persistence.*;

@Entity
public class ReporteConducta {
    // Compatibilidad con lógica de moderación
    public boolean getResuelto() { return estado == EstadoReporte.APROBADO || estado == EstadoReporte.RECHAZADO; }
    public void setResuelto(boolean resuelto) { this.estado = resuelto ? EstadoReporte.APROBADO : EstadoReporte.PENDIENTE; }
    public String getResolucion() { return sancion; }
    public void setResolucion(String resolucion) { this.sancion = resolucion; }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Scrim scrim;

    @ManyToOne
    private Usuario reportante;

    @ManyToOne(optional = false)
    private Usuario reportado;

    @Enumerated(EnumType.STRING)
    private MotivoReporte motivo;

    private String descripcion; // descripción adicional del reporte

    @Enumerated(EnumType.STRING)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    private String sancion; // opcional

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Scrim getScrim() { return scrim; }
    public void setScrim(Scrim scrim) { this.scrim = scrim; }
    public Usuario getReportante() { return reportante; }
    public void setReportante(Usuario reportante) { this.reportante = reportante; }
    public Usuario getReportado() { return reportado; }
    public void setReportado(Usuario reportado) { this.reportado = reportado; }
    public MotivoReporte getMotivo() { return motivo; }
    public void setMotivo(MotivoReporte motivo) { this.motivo = motivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }
    public String getSancion() { return sancion; }
    public void setSancion(String sancion) { this.sancion = sancion; }
}
