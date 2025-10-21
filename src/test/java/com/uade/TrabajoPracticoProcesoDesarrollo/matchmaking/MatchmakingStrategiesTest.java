package com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchmakingStrategiesTest {
    @Test
    void byLatencySelectsLowestLatency(){
        var s = new Scrim(); s.setCuposTotal(2); s.setLatenciaMax(100);
        List<Usuario> c = new ArrayList<>();
        var u1 = new Usuario(); u1.setId(1L); u1.setLatencia(150);
        var u2 = new Usuario(); u2.setId(2L); u2.setLatencia(50);
        var u3 = new Usuario(); u3.setId(3L); u3.setLatencia(80);
        c.add(u1); c.add(u2); c.add(u3);
        var strat = new ByLatencyStrategy();
        var res = strat.seleccionar(c, s);
        assertEquals(2, res.size());
        assertTrue(res.stream().allMatch(u -> u.getLatencia() <= 100));
    }

    @Test
    void byHistorySelectsByRecent(){
        var s = new Scrim(); s.setCuposTotal(2);
        List<Usuario> c = new ArrayList<>();
        var u1 = new Usuario(); u1.setId(1L);
        var u2 = new Usuario(); u2.setId(5L);
        var u3 = new Usuario(); u3.setId(3L);
        c.add(u1); c.add(u2); c.add(u3);
        var strat = new ByHistoryStrategy();
        var res = strat.seleccionar(c, s);
        assertEquals(2, res.size());
        assertTrue(res.get(0).getId() >= res.get(1).getId());
    }
}
