package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.NotificacionIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionIntentRepository extends JpaRepository<NotificacionIntent, Long> {
}
