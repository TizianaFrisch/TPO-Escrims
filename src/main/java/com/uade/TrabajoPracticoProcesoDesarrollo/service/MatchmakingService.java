package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.*;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.EquipoRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.PostulacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchmakingService {
    private final ScrimRepository scrimRepository;
    private final PostulacionRepository postulacionRepository;
    private final EquipoRepository equipoRepository;

    public MatchmakingService(ScrimRepository scrimRepository, PostulacionRepository postulacionRepository,
                              EquipoRepository equipoRepository, UsuarioRepository usuarioRepository) {
        this.scrimRepository = scrimRepository;
        this.postulacionRepository = postulacionRepository;
        this.equipoRepository = equipoRepository;
    }

    @Transactional
    public List<Equipo> formarEquiposPorMMR(Long scrimId) {
        Scrim s = scrimRepository.findById(scrimId).orElseThrow();
        var aceptadas = postulacionRepository.findByScrimIdAndEstado(scrimId, PostulacionEstado.ACEPTADA);

        // Si no hay suficientes aceptadas para al menos 2 jugadores, promover desde pendientes
        if (aceptadas.size() < 2) {
            var pendientes = postulacionRepository.findByScrimIdAndEstado(scrimId, PostulacionEstado.PENDIENTE);
            int faltan = 2 - aceptadas.size();
            for (var p : pendientes) {
                if (faltan <= 0) break;
                p.setEstado(PostulacionEstado.ACEPTADA);
                postulacionRepository.save(p);
                aceptadas.add(p);
                faltan--;
            }
        }

        int required = s.getCuposTotal() != null ? s.getCuposTotal() : aceptadas.size();
        if (required < 2) throw new IllegalStateException("Se requieren al menos 2 jugadores aceptados");

        // Ordenar por MMR desc (null como 0)
        var jugadoresOrdenados = aceptadas.stream()
                .map(Postulacion::getUsuario)
                .sorted(Comparator.comparingInt(u -> -1 * (u.getMmr() != null ? u.getMmr() : 0)))
                .toList();

        // Tomar solo la cantidad requerida (si hay más aceptados)
        var jugadores = jugadoresOrdenados.subList(0, Math.min(required, jugadoresOrdenados.size()));

        List<Usuario> equipo1 = new ArrayList<>();
        List<Usuario> equipo2 = new ArrayList<>();
        for (int i = 0; i < jugadores.size(); i++) {
            if (i % 2 == 0) equipo1.add(jugadores.get(i)); else equipo2.add(jugadores.get(i));
        }
        if (equipo1.isEmpty() || equipo2.isEmpty()) {
            // Intento adicional: mover último del equipo mayor al menor
            if (jugadores.size() >= 2) {
                equipo1.clear(); equipo2.clear();
                for (int i = 0; i < jugadores.size(); i++) {
                    if (i < jugadores.size()/2) equipo1.add(jugadores.get(i)); else equipo2.add(jugadores.get(i));
                }
            }
            if (equipo1.isEmpty() || equipo2.isEmpty()) throw new IllegalStateException("No se pudo formar dos equipos válidos");
        }

        Equipo e1 = crearEquipo(s, "Equipo Azul", "AZUL", equipo1);
        Equipo e2 = crearEquipo(s, "Equipo Rojo", "ROJO", equipo2);
        return java.util.List.of(e1, e2);
    }

    private Equipo crearEquipo(Scrim s, String nombre, String lado, List<Usuario> miembros){
        Equipo e = new Equipo();
        e.setScrim(s); e.setNombre(nombre); e.setLado(lado);
        e.setCapitan(miembros.get(0));
        var saved = equipoRepository.save(e);
        for (int i = 0; i < miembros.size(); i++){
            var me = new MiembroEquipo();
            me.setEquipo(saved);
            me.setUsuario(miembros.get(i));
            me.setRol(rolPorPosicion(i));
            saved.getMiembros().add(me);
        }
        // promedio simple
        saved.setPromedioMMR(miembros.stream().map(Usuario::getMmr).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0));
        return equipoRepository.save(saved);
    }

    private String rolPorPosicion(int i){
        String[] roles = {"TOP","JUNGLE","MID","ADC","SUPPORT"};
        return roles[i % roles.length];
    }
}
