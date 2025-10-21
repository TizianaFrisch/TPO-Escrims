package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScrimRepository extends JpaRepository<Scrim, Long>, JpaSpecificationExecutor<Scrim> {
	java.util.List<Scrim> findByEstado(ScrimEstado estado);
}
