// Al postular
if (user.getCooldownHasta()!=null && user.getCooldownHasta().isAfter(LocalDateTime.now()))
        throw new BusinessException("Cooldown activo hasta " + user.getCooldownHasta());

// Moderación: registrar no-show
@PostMapping("/api/mod/no-show/{scrimId}/{userId}") // @PreAuthorize("hasRole('MOD') or hasRole('ADMIN')")
public void registrarNoShow(...) {
    user.setStrikes( (user.getStrikes()==null?0:user.getStrikes()) + 1 );
    if (user.getStrikes() >= 3) user.setCooldownHasta(LocalDateTime.now().plusDays(7));
    usuarioRepo.save(user);
    audit.log("Usuario", user.getId(), "no_show", adminUser, Map.of("scrimId", scrimId, "strikes", user.getStrikes()));
    bus.publish(new StrikeAppliedEvent(user.getId(), user.getStrikes()));
}
