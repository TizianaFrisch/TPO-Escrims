package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Juego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {
    
    List<Juego> findByActivoTrue();
    
    Optional<Juego> findByNombre(String nombre);
    
    Optional<Juego> findByNombreAndActivoTrue(String nombre);
    
    @Query("SELECT j FROM Juego j WHERE j.activo = true AND j.regionesSoportadas LIKE %:region%")
    List<Juego> findJuegosDisponiblesEnRegion(@Param("region") String region);
    
    @Query("SELECT j FROM Juego j WHERE j.activo = true AND j.mmrMinimo <= :mmr AND j.mmrMaximo >= :mmr")
    List<Juego> findJuegosCompatiblesConMMR(@Param("mmr") Integer mmr);
    
    @Query("SELECT j FROM Juego j WHERE j.activo = true AND j.rolesDisponibles LIKE %:rol%")
    List<Juego> findJuegosConRol(@Param("rol") String rol);
}