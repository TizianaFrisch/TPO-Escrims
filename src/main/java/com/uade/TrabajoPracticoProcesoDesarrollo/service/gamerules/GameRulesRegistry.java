package com.uade.TrabajoPracticoProcesoDesarrollo.service.gamerules;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GameRulesRegistry {
    private final Map<String, GameRules> map = new HashMap<>();

    public GameRulesRegistry(List<GameRules> rules) {
        if (rules != null) {
            for (GameRules r : rules) {
                if (r != null && r.supports() != null) map.put(r.supports().toLowerCase(), r);
            }
        }
    }

    public GameRules forGame(Juego j) {
        if (j == null || j.getNombre() == null) return new DefaultGameRules();
        return map.getOrDefault(j.getNombre().toLowerCase(), new DefaultGameRules());
    }

    // Simple default no-op rules
    static class DefaultGameRules implements GameRules {
        @Override public String supports() { return "_default"; }
    }
}
