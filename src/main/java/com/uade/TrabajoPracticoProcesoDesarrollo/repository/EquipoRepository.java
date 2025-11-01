package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByScrimId(Long scrimId);
}
