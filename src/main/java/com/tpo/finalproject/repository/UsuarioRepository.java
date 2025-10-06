package com.tpo.finalproject.repository;

import com.tpo.finalproject.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByDiscordId(String discordId);
    
    List<Usuario> findByActivoTrue();
    
    List<Usuario> findByRegion(String region);
    
    @Query("SELECT u FROM Usuario u WHERE u.mmr BETWEEN :mmrMin AND :mmrMax")
    List<Usuario> findByMmrBetween(@Param("mmrMin") Integer mmrMin, @Param("mmrMax") Integer mmrMax);
    
    @Query("SELECT u FROM Usuario u WHERE u.region = :region AND u.mmr BETWEEN :mmrMin AND :mmrMax AND u.activo = true")
    List<Usuario> findUsuariosDisponiblesParaScrim(@Param("region") String region, 
                                                  @Param("mmrMin") Integer mmrMin, 
                                                  @Param("mmrMax") Integer mmrMax);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByDiscordId(String discordId);
}