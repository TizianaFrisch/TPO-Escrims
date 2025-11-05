package com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ByHistoryStrategy implements MatchmakingStrategy {
    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        int cap = scrim.getCuposTotal() != null ? scrim.getCuposTotal() : candidatos.size();
        // Dummy: seleccionar usuarios con mayor id (simula historial de actividad)
        return candidatos.stream()
                .sorted(Comparator.comparingLong(u -> -u.getId()))
                .limit(cap)
                .collect(Collectors.toList());
    }
}
