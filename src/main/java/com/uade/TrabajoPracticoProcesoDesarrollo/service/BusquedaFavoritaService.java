package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.model.BusquedaFavorita;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.BusquedaFavoritaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BusquedaFavoritaService {
    private final BusquedaFavoritaRepository repo;
    private final NotificacionService notificacionService;

    public BusquedaFavoritaService(BusquedaFavoritaRepository repo, NotificacionService notificacionService) {
        this.repo = repo;
        this.notificacionService = notificacionService;
    }

    public List<BusquedaFavorita> buscarCoincidentes(String juego, String region, Integer rango, Integer latencia) {
        return repo.findByJuegoAndRegionAndAlertasActivasTrue(juego, region).stream()
            .filter(b -> (b.getRangoMin() == null || b.getRangoMin() <= rango)
                     && (b.getRangoMax() == null || b.getRangoMax() >= rango)
                     && (b.getLatenciaMax() == null || b.getLatenciaMax() >= latencia))
            .toList();
    }

    public void notificarCoincidentes(String juego, String region, Integer rango, Integer latencia, Long scrimId) {
        var coincidencias = buscarCoincidentes(juego, region, rango, latencia);
        for (BusquedaFavorita b : coincidencias) {
            Usuario usuario = b.getUsuario();
            String titulo = "Scrim favorito disponible";
            String mensaje = "Se creó un scrim que coincide con tu búsqueda favorita: " + juego + " - " + region + " (Scrim #" + scrimId + ")";
            notificacionService.crearYEnviarATodosCanales(usuario.getId(), titulo, mensaje, null);
        }
    }
}
