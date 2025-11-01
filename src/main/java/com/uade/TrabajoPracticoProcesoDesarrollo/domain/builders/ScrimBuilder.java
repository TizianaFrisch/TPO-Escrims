package com.uade.TrabajoPracticoProcesoDesarrollo.domain.builders;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import java.time.LocalDateTime;

public class ScrimBuilder {
    private final Scrim scrim = new Scrim();

    public ScrimBuilder(){}

    public ScrimBuilder juego(Juego juego){ scrim.setJuego(juego); return this; }
    public ScrimBuilder region(String r){ scrim.setRegion(r); return this; }
    public ScrimBuilder rango(Integer min, Integer max){ scrim.setRangoMin(min); scrim.setRangoMax(max); return this; }
    public ScrimBuilder latencia(Integer ms){ scrim.setLatenciaMax(ms); return this; }
    public ScrimBuilder fechaHora(LocalDateTime dt){ scrim.setFechaHora(dt); return this; }
    public ScrimBuilder duracion(Integer minutos){ scrim.setDuracionMinutos(minutos); return this; }
    public ScrimBuilder formato(String f){ scrim.setFormato(f); return this; }
    public ScrimBuilder cupos(Integer cupos){ scrim.setCuposTotal(cupos); return this; }

    public ScrimBuilder estado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado estado){ scrim.setEstado(estado); return this; }

    public Scrim build(){ return scrim; }
}
