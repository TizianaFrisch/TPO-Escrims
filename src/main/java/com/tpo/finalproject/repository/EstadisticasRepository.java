package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Estadisticas;
import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstadisticasRepository extends JpaRepository<Estadisticas, Long> {
    
    Optional<Estadisticas> findByUsuario(Usuario usuario);
    
    Optional<Estadisticas> findByUsuarioId(Long usuarioId);
    
    @Query("SELECT e FROM Estadisticas e WHERE e.partidasJugadas >= :minPartidas ORDER BY e.winrate DESC")
    List<Estadisticas> findTopJugadoresPorWinrate(@Param("minPartidas") Integer minPartidas);
    
    @Query("SELECT e FROM Estadisticas e WHERE e.partidasJugadas >= :minPartidas ORDER BY e.performanceScorePromedio DESC")
    List<Estadisticas> findTopJugadoresPorPerformance(@Param("minPartidas") Integer minPartidas);
    
    @Query("SELECT e FROM Estadisticas e WHERE e.partidasJugadas >= :minPartidas ORDER BY e.mmrActual DESC")
    List<Estadisticas> findTopJugadoresPorMMR(@Param("minPartidas") Integer minPartidas);
    
    @Query("SELECT e FROM Estadisticas e WHERE e.rachaActual >= :minRacha ORDER BY e.rachaActual DESC")
    List<Estadisticas> findJugadoresEnRachaVictoriosa(@Param("minRacha") Integer minRacha);
    
    @Query("SELECT e FROM Estadisticas e WHERE e.ultimaActualizacion >= :fecha")
    List<Estadisticas> findJugadoresActivosDesde(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT AVG(e.winrate) FROM Estadisticas e WHERE e.partidasJugadas >= 10")
    Double findWinratePromedio();
    
    @Query("SELECT AVG(e.performanceScorePromedio) FROM Estadisticas e WHERE e.partidasJugadas >= 10")
    Double findPerformanceScorePromedio();
    
    @Query("SELECT COUNT(e) FROM Estadisticas e WHERE e.partidasJugadas >= :minPartidas")
    Long countJugadoresActivos(@Param("minPartidas") Integer minPartidas);
    
    @Query("SELECT e FROM Estadisticas e WHERE e.mmrActual BETWEEN :mmrMin AND :mmrMax AND e.partidasJugadas >= 5")
    List<Estadisticas> findJugadoresEnRangoMMR(@Param("mmrMin") Integer mmrMin, 
                                              @Param("mmrMax") Integer mmrMax);
}