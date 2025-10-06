package com.tpo.finalproject.service;

import com.tpo.finalproject.domain.entities.*;
import com.tpo.finalproject.repository.EquipoRepository;
import com.tpo.finalproject.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MatchmakingService {
    
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Patrón Strategy - Diferentes estrategias de matchmaking
    public enum EstrategiaMatchmaking {
        POR_MMR,
        POR_ROLES,
        POR_HISTORIAL,
        BALANCEADO
    }
    
    // Patrón Strategy - Estrategia principal de formación de equipos
    @Transactional
    public void formarEquiposBalanceados(Scrim scrim, List<Postulacion> postulaciones) {
        
        if (postulaciones.size() < 10) {
            throw new IllegalArgumentException("Se necesitan al menos 10 jugadores");
        }
        
        // Tomar solo los primeros 10 jugadores
        List<Usuario> jugadores = postulaciones.stream()
                .limit(10)
                .map(Postulacion::getUsuario)
                .toList();
        
        // Aplicar estrategia de balanceo por MMR
        List<List<Usuario>> equiposBalanceados = aplicarEstrategiaMMR(jugadores);
        
        // Crear entidades de equipos
        crearEquiposEnBaseDatos(scrim, equiposBalanceados);
    }
    
    // Patrón Strategy - Estrategia de balanceo por MMR
    private List<List<Usuario>> aplicarEstrategiaMMR(List<Usuario> jugadores) {
        
        // Ordenar jugadores por MMR de mayor a menor
        List<Usuario> jugadoresOrdenados = jugadores.stream()
                .sorted(Comparator.comparing(Usuario::getMmr, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        
        List<Usuario> equipo1 = new ArrayList<>();
        List<Usuario> equipo2 = new ArrayList<>();
        
        // Algoritmo de distribución alternada (draft pick)
        for (int i = 0; i < jugadoresOrdenados.size(); i++) {
            if (i % 2 == 0) {
                equipo1.add(jugadoresOrdenados.get(i));
            } else {
                equipo2.add(jugadoresOrdenados.get(i));
            }
        }
        
        // Balancear si la diferencia de MMR promedio es muy grande
        balancearEquiposPorMMR(equipo1, equipo2);
        
        List<List<Usuario>> equipos = new ArrayList<>();
        equipos.add(equipo1);
        equipos.add(equipo2);
        
        return equipos;
    }
    
    // Patrón Strategy - Estrategia de balanceo por roles
    private List<List<Usuario>> aplicarEstrategiaRoles(List<Usuario> jugadores) {
        
        // Agrupar jugadores por rol preferido
        List<Usuario> tops = filtrarPorRol(jugadores, "TOP");
        List<Usuario> jungles = filtrarPorRol(jugadores, "JUNGLE");
        List<Usuario> mids = filtrarPorRol(jugadores, "MID");
        List<Usuario> adcs = filtrarPorRol(jugadores, "ADC");
        List<Usuario> supports = filtrarPorRol(jugadores, "SUPPORT");
        List<Usuario> flexibles = filtrarPorRol(jugadores, "FLEX");
        
        List<Usuario> equipo1 = new ArrayList<>();
        List<Usuario> equipo2 = new ArrayList<>();
        
        // Distribuir un jugador de cada rol por equipo
        distribuirJugadoresPorRol(equipo1, equipo2, tops, "TOP");
        distribuirJugadoresPorRol(equipo1, equipo2, jungles, "JUNGLE");
        distribuirJugadoresPorRol(equipo1, equipo2, mids, "MID");
        distribuirJugadoresPorRol(equipo1, equipo2, adcs, "ADC");
        distribuirJugadoresPorRol(equipo1, equipo2, supports, "SUPPORT");
        
        // Completar con jugadores flexibles si faltan
        completarConFlexibles(equipo1, equipo2, flexibles);
        
        List<List<Usuario>> equipos = new ArrayList<>();
        equipos.add(equipo1);
        equipos.add(equipo2);
        
        return equipos;
    }
    
    // Patrón Strategy - Estrategia híbrida (MMR + Roles)
    public List<List<Usuario>> aplicarEstrategiaHibrida(List<Usuario> jugadores) {
        
        // Primero intentar balancear por roles
        List<List<Usuario>> equiposPorRoles = aplicarEstrategiaRoles(jugadores);
        
        // Luego ajustar por MMR si es necesario
        Usuario mejorJugadorEquipo1 = obtenerMejorJugador(equiposPorRoles.get(0));
        Usuario mejorJugadorEquipo2 = obtenerMejorJugador(equiposPorRoles.get(1));
        
        // Si la diferencia de MMR del mejor jugador es muy grande, intercambiar
        if (Math.abs(mejorJugadorEquipo1.getMmr() - mejorJugadorEquipo2.getMmr()) > 200) {
            // Lógica de intercambio para balancear
            ajustarEquiposPorMMR(equiposPorRoles.get(0), equiposPorRoles.get(1));
        }
        
        return equiposPorRoles;
    }
    
    // Métodos auxiliares
    
    @Transactional
    private void crearEquiposEnBaseDatos(Scrim scrim, List<List<Usuario>> equiposBalanceados) {
        
        for (int i = 0; i < equiposBalanceados.size(); i++) {
            List<Usuario> miembrosEquipo = equiposBalanceados.get(i);
            
            if (miembrosEquipo.isEmpty()) continue;
            
            // El primer jugador será el capitán
            Usuario capitan = miembrosEquipo.get(0);
            
            // Crear equipo
            Equipo equipo = Equipo.builder()
                    .nombre("Equipo " + (i + 1))
                    .scrim(scrim)
                    .capitan(capitan)
                    .lado(i == 0 ? "AZUL" : "ROJO")
                    .build();
            
            Equipo equipoGuardado = equipoRepository.save(equipo);
            
            // Crear miembros del equipo
            for (Usuario usuario : miembrosEquipo) {
                MiembroEquipo miembro = MiembroEquipo.builder()
                        .equipo(equipoGuardado)
                        .usuario(usuario)
                        .rol(determinarRol(usuario, miembrosEquipo.indexOf(usuario)))
                        .build();
                
                equipoGuardado.getMiembros().add(miembro);
            }
            
            // Calcular MMR promedio
            equipoGuardado.calcularPromedioMMR();
            equipoRepository.save(equipoGuardado);
        }
    }
    
    private void balancearEquiposPorMMR(List<Usuario> equipo1, List<Usuario> equipo2) {
        
        double promedioEquipo1 = calcularMMRPromedio(equipo1);
        double promedioEquipo2 = calcularMMRPromedio(equipo2);
        
        // Si la diferencia es mayor a 100 MMR, intentar intercambiar jugadores
        double diferencia = Math.abs(promedioEquipo1 - promedioEquipo2);
        
        if (diferencia > 100) {
            // Encontrar el mejor intercambio posible
            realizarMejorIntercambio(equipo1, equipo2);
        }
    }
    
    private void realizarMejorIntercambio(List<Usuario> equipo1, List<Usuario> equipo2) {
        
        double mejorDiferencia = Double.MAX_VALUE;
        Usuario mejorJugador1 = null;
        Usuario mejorJugador2 = null;
        
        // Probar todos los intercambios posibles
        for (Usuario jugador1 : equipo1) {
            for (Usuario jugador2 : equipo2) {
                
                // Simular intercambio
                List<Usuario> tempEquipo1 = new ArrayList<>(equipo1);
                List<Usuario> tempEquipo2 = new ArrayList<>(equipo2);
                
                tempEquipo1.remove(jugador1);
                tempEquipo1.add(jugador2);
                tempEquipo2.remove(jugador2);
                tempEquipo2.add(jugador1);
                
                double diferencia = Math.abs(calcularMMRPromedio(tempEquipo1) - calcularMMRPromedio(tempEquipo2));
                
                if (diferencia < mejorDiferencia) {
                    mejorDiferencia = diferencia;
                    mejorJugador1 = jugador1;
                    mejorJugador2 = jugador2;
                }
            }
        }
        
        // Realizar el mejor intercambio encontrado
        if (mejorJugador1 != null && mejorJugador2 != null) {
            equipo1.remove(mejorJugador1);
            equipo1.add(mejorJugador2);
            equipo2.remove(mejorJugador2);
            equipo2.add(mejorJugador1);
        }
    }
    
    private double calcularMMRPromedio(List<Usuario> equipo) {
        return equipo.stream()
                .mapToInt(u -> u.getMmr() != null ? u.getMmr() : 0)
                .average()
                .orElse(0.0);
    }
    
    private List<Usuario> filtrarPorRol(List<Usuario> jugadores, String rol) {
        return jugadores.stream()
                .filter(j -> rol.equals(j.getRolPreferido()))
                .toList();
    }
    
    private void distribuirJugadoresPorRol(List<Usuario> equipo1, List<Usuario> equipo2, 
                                          List<Usuario> jugadoresRol, String rol) {
        
        Collections.shuffle(jugadoresRol); // Aleatorizar
        
        for (int i = 0; i < jugadoresRol.size() && (equipo1.size() < 5 || equipo2.size() < 5); i++) {
            if (equipo1.size() <= equipo2.size() && equipo1.size() < 5) {
                equipo1.add(jugadoresRol.get(i));
            } else if (equipo2.size() < 5) {
                equipo2.add(jugadoresRol.get(i));
            }
        }
    }
    
    private void completarConFlexibles(List<Usuario> equipo1, List<Usuario> equipo2, 
                                      List<Usuario> flexibles) {
        
        Collections.shuffle(flexibles);
        
        for (Usuario flexible : flexibles) {
            if (equipo1.size() < 5) {
                equipo1.add(flexible);
            } else if (equipo2.size() < 5) {
                equipo2.add(flexible);
            }
        }
    }
    
    private Usuario obtenerMejorJugador(List<Usuario> equipo) {
        return equipo.stream()
                .max(Comparator.comparing(Usuario::getMmr, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }
    
    private void ajustarEquiposPorMMR(List<Usuario> equipo1, List<Usuario> equipo2) {
        // Lógica de ajuste fino basada en MMR
        realizarMejorIntercambio(equipo1, equipo2);
    }
    
    private String determinarRol(Usuario usuario, int posicion) {
        
        if (usuario.getRolPreferido() != null) {
            return usuario.getRolPreferido();
        }
        
        // Asignar rol basado en posición si no tiene preferencia
        String[] roles = {"TOP", "JUNGLE", "MID", "ADC", "SUPPORT"};
        return roles[posicion % roles.length];
    }
}