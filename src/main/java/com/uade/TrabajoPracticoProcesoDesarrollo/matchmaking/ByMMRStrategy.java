package com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Selecciona los mejores candidatos por cercanía al rango/ventana MMR del scrim.
 * Criterio simple: ordena por distancia al centro del rango permitido (o MMR mínimo si no hay max),
 * filtra los fuera de rango si rangoMin/rangoMax existen, y limita a cuposTotal.
 */
@Component
public class ByMMRStrategy implements MatchmakingStrategy {
    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        int cap = scrim.getCuposTotal() != null ? scrim.getCuposTotal() : candidatos.size();
    Integer min = scrim.getRangoMin();
    Integer max = scrim.getRangoMax();
    double centro = (min != null && max != null) ? (min + max) / 2.0 : (min != null ? min : 0);

    // Primer intento: respetar rango si existe (tratando null MMR como 0)
    var dentroDeRango = candidatos.stream()
        .sorted(Comparator.comparingDouble(u -> Math.abs(((u.getMmr() != null ? u.getMmr() : 0)) - centro)))
        .filter(u -> (min == null || (u.getMmr() != null ? u.getMmr() : 0) >= min)
              && (max == null || (u.getMmr() != null ? u.getMmr() : 0) <= max))
        .limit(cap)
        .collect(Collectors.toList());

    if (!dentroDeRango.isEmpty()) return dentroDeRango;

    // Fallback: si nadie entra en rango, elegir por cercanía ignorando rango (incluye MMR null como 0)
    return candidatos.stream()
        .sorted(Comparator.comparingDouble(u -> Math.abs(((u.getMmr() != null ? u.getMmr() : 0)) - centro)))
        .limit(cap)
        .collect(Collectors.toList());
    }
}
