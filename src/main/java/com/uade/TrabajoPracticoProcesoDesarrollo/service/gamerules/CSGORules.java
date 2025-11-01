package com.uade.TrabajoPracticoProcesoDesarrollo.service.gamerules;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearScrimRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.PostulacionRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.exceptions.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CSGORules implements GameRules {
    @Override
    public String supports() { return "csgo"; }

    @Override
    public void validateCreate(CrearScrimRequest req, Juego juego) {
        // CSGO often plays 5v5 too; enforce a minimum cupo of 10 for big matches as example
        if (req.cuposTotal != null && req.cuposTotal < 5) {
            throw new BusinessException("CSGO: cupo total demasiado pequeño (mínimo 5)");
        }
    }

    @Override
    public void validatePostular(PostulacionRequest req, Scrim scrim, Usuario usuario) {
        // For CSGO, enforce a stricter latency requirement: if scrim has latenciaMax, user's latency must be <= latenciaMax - 10 (simulated stricter rule)
        if (scrim.getLatenciaMax() != null && usuario.getLatencia() != null) {
            if (usuario.getLatencia() > scrim.getLatenciaMax() - 10) {
                throw new BusinessException("CSGO: la latencia del usuario es demasiado alta para este scrim (umbral estricto)");
            }
        }
    }

    @Override
    public void validateFormTeam(Scrim scrim, List<Usuario> candidatos) {
        // Example: require at least one usuario with mmr >= 2000 in candidates for competitive CSGO
        long high = candidatos.stream().filter(u -> u.getMmr() != null && u.getMmr() >= 2000).count();
        if (scrim.getRangoMin() != null && scrim.getRangoMin() >= 2000 && high == 0) {
            throw new BusinessException("CSGO: no hay candidatos con MMR suficiente para este scrim competitivo");
        }
    }
}
