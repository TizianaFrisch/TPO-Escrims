package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusquedaFavoritaRepository extends JpaRepository<BusquedaFavorita, Long> {
    List<BusquedaFavorita> findByJuegoAndRegionAndAlertasActivasTrue(String juego, String region);
    List<BusquedaFavorita> findByUsuarioId(Long usuarioId);
}
