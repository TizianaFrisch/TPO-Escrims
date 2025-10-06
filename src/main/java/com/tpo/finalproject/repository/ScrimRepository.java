package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Scrim;
import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScrimRepository extends JpaRepository<Scrim, Long> {
    
    List<Scrim> findByActivoTrue();
    
    List<Scrim> findByCreador(Usuario creador);
    
    List<Scrim> findByRegion(String region);
    
    List<Scrim> findByEstado(Scrim.EstadoScrim estado);
    
    @Query("SELECT s FROM Scrim s WHERE s.activo = true AND s.estado = :estado")
    List<Scrim> findScrimsActivosPorEstado(@Param("estado") Scrim.EstadoScrim estado);
    
    @Query("SELECT s FROM Scrim s WHERE s.region = :region AND s.activo = true AND s.estado = 'BUSCANDO_JUGADORES'")
    List<Scrim> findScrimsDisponiblesEnRegion(@Param("region") String region);
    
    @Query("SELECT s FROM Scrim s WHERE s.mmrMinimo <= :mmr AND s.mmrMaximo >= :mmr AND s.activo = true AND s.estado = 'BUSCANDO_JUGADORES'")
    List<Scrim> findScrimsCompatiblesConMMR(@Param("mmr") Integer mmr);
    
    @Query("SELECT s FROM Scrim s WHERE s.fechaHora BETWEEN :inicio AND :fin")
    List<Scrim> findScrimsEnRangoFecha(@Param("inicio") LocalDateTime inicio, 
                                       @Param("fin") LocalDateTime fin);
    
    @Query("SELECT s FROM Scrim s WHERE s.region = :region AND s.mmrMinimo <= :mmr AND s.mmrMaximo >= :mmr AND s.activo = true AND s.estado = 'BUSCANDO_JUGADORES'")
    List<Scrim> findScrimsRecomendadosParaUsuario(@Param("region") String region, 
                                                  @Param("mmr") Integer mmr);
}