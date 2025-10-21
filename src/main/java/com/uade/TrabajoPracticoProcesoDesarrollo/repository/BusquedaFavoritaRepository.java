package com.uade.escrims.repository;

import com.uade.escrims.model.BusquedaFavorita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusquedaFavoritaRepository extends JpaRepository<BusquedaFavorita, Long> {
    List<BusquedaFavorita> findByUsuarioId(Long usuarioId);
    List<BusquedaFavorita> findByJuegoAndRegionAndAlertasActivasTrue(String juego, String region);
}
