package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
	long countByScrimIdAndEstado(Long scrimId, PostulacionEstado estado);
	java.util.List<Postulacion> findByScrimId(Long scrimId);
	java.util.List<Postulacion> findByScrimIdAndEstado(Long scrimId, PostulacionEstado estado);

	// Encontrar postulacion por scrim y usuario para actualizaciones directas
	Postulacion findByScrimIdAndUsuarioId(Long scrimId, Long usuarioId);

	// Prefer entity-based variants to avoid nested property resolution issues
	long countByScrimAndEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim scrim, PostulacionEstado estado);
	java.util.List<Postulacion> findByScrimAndEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim scrim, PostulacionEstado estado);

	// Allow explicit flush in services to ensure visibility within same transaction
	void flush();
}
