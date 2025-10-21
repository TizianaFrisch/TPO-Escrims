package com.uade.TrabajoPracticoProcesoDesarrollo.web.dto;

import java.util.List;

public class FinalizarMatchRequest {
    public Long equipoGanadorId;
    public Integer duracionMinutos;
    public String observaciones;
    public Integer killsGanador;
    public Integer killsPerdedor;
    public Integer torresGanador;
    public Integer torresPerdedor;
    public Integer goldDiff;
    public Integer deltaWin; // default +15
    public Integer deltaLose; // default -12
    public List<EstadisticaJugador> jugadores;

    public static class EstadisticaJugador {
        public Long usuarioId;
        public Long equipoId; // opcional, para mapear al equipo correspondiente
        public Integer kills;
        public Integer muertes;
        public Integer asistencias;
        public Integer minions;
        public Integer oro;
        public Integer danoCausado;
        public Integer danoRecibido;
        public Integer torres;
        public Integer objetivos;
    }
}
