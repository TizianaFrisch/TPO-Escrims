package com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Estrategia "híbrida" que combina estado/tiempo del scrim con afinidad MMR y latencia.
 *
 * Reglas duras (descartan):
 * - Misma región (si ambas existen).
 * - Si el scrim define latenciaMax, el candidato debe cumplirla.
 * - Si el scrim define rangoMin/Max, el candidato debe estar dentro.
 *
 * Ranking (score 0..1):
 * score = 0.35*estado + 0.15*tiempo + 0.25*mmr + 0.15*latencia + 0.10*extra(rol/hist)
 * Donde extra por ahora queda neutro (=1).
 */
@Component
public class HybridStrategy implements MatchmakingStrategy {

    private static final double W_ESTADO = 0.35;
    private static final double W_TIEMPO = 0.15;
    private static final double W_MMR    = 0.25;
    private static final double W_LAT    = 0.15;
    private static final double W_EXTRA  = 0.10; // reservado para rol/historial futurible

    @Override
    public List<Usuario> seleccionar(List<Usuario> candidatos, Scrim scrim) {
        int cap = scrim.getCuposTotal() != null ? scrim.getCuposTotal() : candidatos.size();

        // Filtros duros por región, rango y latencia
        var filtrados = candidatos.stream()
                .filter(u -> regionOk(u, scrim))
                .filter(u -> latOk(u, scrim))
                .filter(u -> rangoOk(u, scrim))
                .collect(Collectors.toList());

        if (filtrados.isEmpty()) {
            // si nadie pasa los filtros duros, degradar a "sin filtros" pero con ranking (mejor que nada)
            filtrados = candidatos;
        }

        final LocalDateTime now = LocalDateTime.now();
        final double estadoScore = estadoScore(scrim);

        // Ordenar por score híbrido descendente
        return filtrados.stream()
                .sorted(Comparator.comparingDouble((Usuario u) -> {
                    double t = tiempoScore(scrim, now);
                    double m = mmrScore(u, scrim);
                    double l = latScore(u, scrim);
                    double extra = 1.0; // placeholder (rol/historial)
                    double score = W_ESTADO*estadoScore + W_TIEMPO*t + W_MMR*m + W_LAT*l + W_EXTRA*extra;
                    return score;
                }).reversed())
                .limit(cap)
                .collect(Collectors.toList());
    }

    private boolean regionOk(Usuario u, Scrim s){
        if (isBlank(u.getRegion()) || isBlank(s.getRegion())) return true; // sin datos: no bloquear
        return Objects.equals(norm(u.getRegion()), norm(s.getRegion()));
    }

    private boolean latOk(Usuario u, Scrim s){
        Integer max = s.getLatenciaMax();
        if (max == null) return true;
        Integer lat = u.getLatencia();
        return lat != null && lat <= max;
    }

    private boolean rangoOk(Usuario u, Scrim s){
        Integer mmr = u.getMmr();
        Integer min = s.getRangoMin();
        Integer max = s.getRangoMax();
        if (mmr == null) return false; // si hay rango definido, pedimos mmr del usuario
        if (min != null && mmr < min) return false;
        if (max != null && mmr > max) return false;
        return true;
    }

    private double estadoScore(Scrim s){
        if (s.getEstado() == null) return 0.7; // tratar null como BUSCANDO
        return switch (s.getEstado()){
            case LOBBY_ARMADO -> 1.0;
            case BUSCANDO    -> 0.7;
            case CONFIRMADO  -> 0.4;
            case EN_JUEGO    -> 0.2;
            default          -> 0.0;
        };
    }

    private double tiempoScore(Scrim s, LocalDateTime now){
        LocalDateTime fh = s.getFechaHora();
        if (fh == null) return 0.8; // sin fecha: neutral-alto
        long minutes = Math.abs(Duration.between(now, fh).toMinutes());
        long window = 48L * 60L; // 48 horas
        double v = 1.0 - (minutes / (double) window);
        return clamp(v, 0.0, 1.0);
    }

    private double mmrScore(Usuario u, Scrim s){
        Integer mmr = u.getMmr();
        if (mmr == null) return 0.4; // sin dato, penalizar
        Integer min = s.getRangoMin();
        Integer max = s.getRangoMax();
        if (min != null && max != null) {
            double center = (min + max) / 2.0;
            double half = Math.max(1.0, (max - min) / 2.0);
            return clamp(1.0 - Math.abs(mmr - center) / half, 0.0, 1.0);
        }
        if (min != null) {
            double half = Math.max(1.0, Math.abs(mmr - min));
            return clamp(1.0 - Math.abs(mmr - min) / half, 0.0, 1.0);
        }
        if (max != null) {
            double half = Math.max(1.0, Math.abs(max - mmr));
            return clamp(1.0 - Math.abs(max - mmr) / half, 0.0, 1.0);
        }
        return 0.8; // sin rango: casi neutral
    }

    private double latScore(Usuario u, Scrim s){
        Integer max = s.getLatenciaMax();
        Integer lat = u.getLatencia();
        if (max == null && lat == null) return 0.8;
        if (max == null) return lat != null ? clamp(1.0 - (lat / 200.0), 0.0, 1.0) : 0.6; // 200ms como referencia suave
        if (lat == null) return 0.4;
        return clamp(1.0 - (lat / (double) Math.max(1, max)), 0.0, 1.0);
    }

    private static String norm(String s){ return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }
    private static boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
    private static double clamp(double v, double a, double b){ return Math.max(a, Math.min(b, v)); }
}
