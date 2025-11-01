package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.BusquedaGuardada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusquedaGuardadaRepository extends JpaRepository<BusquedaGuardada, Long> {
    List<BusquedaGuardada> findByUsuarioId(Long usuarioId);
}
