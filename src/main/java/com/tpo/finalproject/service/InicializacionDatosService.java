package com.tpo.finalproject.service;

import com.tpo.finalproject.domain.entities.Juego;
import com.tpo.finalproject.repository.JuegoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InicializacionDatosService implements CommandLineRunner {
    
    private final JuegoRepository juegoRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        inicializarJuegos();
    }
    
    private void inicializarJuegos() {
        // Verificar si ya existe League of Legends
        if (juegoRepository.findByNombre("League of Legends").isEmpty()) {
            Juego lol = Juego.builder()
                    .nombre("League of Legends")
                    .version("14.20")
                    .descripcion("Juego MOBA 5v5 desarrollado por Riot Games")
                    .desarrollador("Riot Games")
                    .genero("MOBA")
                    .maxJugadores(10)
                    .fechaLanzamiento(LocalDateTime.of(2009, 10, 27, 0, 0))
                    .rolesDisponibles("[\"TOP\",\"JUNGLE\",\"MID\",\"ADC\",\"SUPPORT\"]")
                    .regionesSoportadas("[\"LAS\",\"LAN\",\"NA\",\"EUW\",\"EUNE\",\"KR\",\"JP\",\"OCE\",\"BR\",\"TR\",\"RU\"]")
                    .mmrMinimo(0)
                    .mmrMaximo(5000)
                    .activo(true)
                    .build();
            
            juegoRepository.save(lol);
        }
        
        // Agregar más juegos si es necesario
        if (juegoRepository.findByNombre("Valorant").isEmpty()) {
            Juego valorant = Juego.builder()
                    .nombre("Valorant")
                    .version("8.08")
                    .descripcion("Shooter táctico 5v5 desarrollado por Riot Games")
                    .desarrollador("Riot Games")
                    .genero("FPS")
                    .maxJugadores(10)
                    .fechaLanzamiento(LocalDateTime.of(2020, 6, 2, 0, 0))
                    .rolesDisponibles("[\"DUELIST\",\"INITIATOR\",\"CONTROLLER\",\"SENTINEL\"]")
                    .regionesSoportadas("[\"LAS\",\"LAN\",\"NA\",\"EU\",\"ASIA\",\"KR\",\"JP\",\"OCE\",\"BR\"]")
                    .mmrMinimo(0)
                    .mmrMaximo(3000)
                    .activo(true)
                    .build();
            
            juegoRepository.save(valorant);
        }
    }
}