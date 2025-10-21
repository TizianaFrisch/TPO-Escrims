// model/BusquedaFavorita.java
@Entity
public class BusquedaFavorita {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) Usuario usuario;
    String juego; String region;
    Integer rangoMin; Integer rangoMax; Integer latenciaMax;
    Boolean alertasActivas = true;
}

// repository/BusquedaFavoritaRepository.java
public interface BusquedaFavoritaRepository extends JpaRepository<BusquedaFavorita, Long> {
    List<BusquedaFavorita> findByJuegoAndRegionAndAlertasActivasTrue(String juego, String region);
}

// controller/BusquedaFavoritaController.java
@RestController @RequestMapping("/api/busquedas")
public class BusquedaFavoritaController {
    private final BusquedaFavoritaRepository repo;
    @PostMapping public BusquedaFavorita create(@RequestBody @Valid BusquedaFavorita b){ return repo.save(b); }
    @GetMapping  public List<BusquedaFavorita> mine(@RequestParam Long userId){ return repo.findAll(/* filtrar por usuario */); }
    @DeleteMapping("{id}") public void del(@PathVariable Long id){ repo.deleteById(id); }
}

// service/ScrimService.java (al final de create)
var matches = busquedaRepo.findByJuegoAndRegionAndAlertasActivasTrue(s.getJuego(), s.getRegion());
for (var bf : matches) {
boolean okRango = (bf.getRangoMin()==null || s.getRangoMax()>=bf.getRangoMin()) &&
        (bf.getRangoMax()==null || s.getRangoMin()<=bf.getRangoMax());
boolean okPing  = (bf.getLatenciaMax()==null || s.getLatenciaMax()<=bf.getLatenciaMax());
  if (okRango && okPing) bus.publish(new ScrimCoincidenteEvent(bf.getUsuario().getId(), s.getId()));
        }
