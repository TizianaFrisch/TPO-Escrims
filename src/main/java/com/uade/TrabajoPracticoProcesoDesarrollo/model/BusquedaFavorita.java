package com.uade.TrabajoPracticoProcesoDesarrollo.model;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BusquedaFavorita {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String juego;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMax;
    private Boolean alertasActivas = true;

    public Integer getRangoMin() { return rangoMin; }
    public Integer getRangoMax() { return rangoMax; }
    public Integer getLatenciaMax() { return latenciaMax; }
    public Boolean getAlertasActivas() { return alertasActivas; }
    public Usuario getUsuario() { return usuario; }
}
