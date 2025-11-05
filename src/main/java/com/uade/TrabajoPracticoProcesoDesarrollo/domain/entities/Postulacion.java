package com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.Rol;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Postulacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JsonBackReference("scrim-postulaciones")
    private Scrim scrim;

    @Enumerated(EnumType.STRING)
    private Rol rolDeseado;
    private String comentario;

    @Enumerated(EnumType.STRING)
    private PostulacionEstado estado = PostulacionEstado.PENDIENTE;

    private LocalDateTime fechaPostulacion;

    @PrePersist
    protected void onCreate(){
        if (fechaPostulacion == null) fechaPostulacion = LocalDateTime.now();
        if (estado == null) estado = PostulacionEstado.PENDIENTE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Scrim getScrim() { return scrim; }
    public void setScrim(Scrim scrim) { this.scrim = scrim; }
    public Rol getRolDeseado() { return rolDeseado; }
    public void setRolDeseado(Rol rolDeseado) { this.rolDeseado = rolDeseado; }
    // Alias de compatibilidad con TPO: valor derivado del enum
    @Transient
    public String getRolSolicitado() { return rolDeseado != null ? rolDeseado.name() : null; }
    public PostulacionEstado getEstado() { return estado; }
    public void setEstado(PostulacionEstado estado) { this.estado = estado; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDateTime fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }
}
