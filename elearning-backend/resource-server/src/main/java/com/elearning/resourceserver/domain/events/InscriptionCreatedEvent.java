package com.elearning.resourceserver.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class InscriptionCreatedEvent extends ApplicationEvent {
    private final UUID apprenantId;
    private final UUID formationId;

    public InscriptionCreatedEvent(Object source, UUID apprenantId, UUID formationId) {
        super(source);
        this.apprenantId = apprenantId;
        this.formationId = formationId;
    }
}
