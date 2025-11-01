package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import java.util.List;
import jakarta.validation.constraints.*;

public class FinalizarMatchRequest {
    @NotNull(message = "equipoGanadorId es obligatorio")
    public Long equipoGanadorId;
    @Min(value = 1, message = "duracionMinutos debe ser >= 1")
    public Integer duracionMinutos;
    public String observaciones;
    @Min(value = 0, message = "killsGanador debe ser >= 0")
    public Integer killsGanador;
    @Min(value = 0, message = "killsPerdedor debe ser >= 0")
    public Integer killsPerdedor;
    @Min(value = 0, message = "torresGanador debe ser >= 0")
    public Integer torresGanador;
    @Min(value = 0, message = "torresPerdedor debe ser >= 0")
    public Integer torresPerdedor;
    public Integer goldDiff;
    public Integer deltaWin; // default +15
    public Integer deltaLose; // default -12
    public List<EstadisticaJugador> jugadores;

    public static class EstadisticaJugador {
        @NotNull(message = "usuarioId es obligatorio")
        public Long usuarioId;
        public Long equipoId; // opcional, para mapear al equipo correspondiente
        @Min(value = 0) public Integer kills;
        @Min(value = 0) public Integer muertes;
        @Min(value = 0) public Integer asistencias;
        @Min(value = 0) public Integer minions;
        @Min(value = 0) public Integer oro;
        @Min(value = 0) public Integer danoCausado;
        @Min(value = 0) public Integer danoRecibido;
        @Min(value = 0) public Integer torres;
        @Min(value = 0) public Integer objetivos;
    }
}
