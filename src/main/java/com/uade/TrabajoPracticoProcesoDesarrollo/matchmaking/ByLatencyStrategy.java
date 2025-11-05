package com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ByLatencyStrategy implements MatchmakingStrategy {
    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        int cap = scrim.getCuposTotal() != null ? scrim.getCuposTotal() : candidatos.size();
        int latMax = scrim.getLatenciaMax() != null ? scrim.getLatenciaMax() : Integer.MAX_VALUE;
        return candidatos.stream()
                .filter(u -> u.getLatencia() != null && u.getLatencia() <= latMax)
                .sorted(Comparator.comparingInt(Usuario::getLatencia))
                .limit(cap)
                .collect(Collectors.toList());
    }
}
