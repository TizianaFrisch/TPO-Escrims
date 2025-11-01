package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Estadistica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadisticaRepository extends JpaRepository<Estadistica, Long> {
	List<Estadistica> findByScrimId(Long scrimId);
	List<Estadistica> findByUsuarioId(Long usuarioId);
}
