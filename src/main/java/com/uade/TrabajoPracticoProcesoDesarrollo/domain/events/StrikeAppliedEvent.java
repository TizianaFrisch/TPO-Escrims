package com.uade.TrabajoPracticoProcesoDesarrollo.domain.events;

public class StrikeAppliedEvent implements DomainEvent {
    private final Long userId;
    private final Integer strikes;

    public StrikeAppliedEvent(Long userId, Integer strikes) {
        this.userId = userId;
        this.strikes = strikes;
    }

    public Long getUserId() { return userId; }
    public Integer getStrikes() { return strikes; }
}
