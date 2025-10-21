package com.uade.escrims.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusquedaFavoritaDTO {
    @NotNull private Long usuarioId;
    private String juego;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMax;
    private Boolean alertasActivas = true;
}
