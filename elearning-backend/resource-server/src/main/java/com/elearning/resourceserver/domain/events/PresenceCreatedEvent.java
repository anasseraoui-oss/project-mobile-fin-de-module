package com.elearning.resourceserver.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PresenceCreatedEvent extends ApplicationEvent {
    private final UUID apprenantId;
    private final UUID seanceId;
    private final UUID coursId;
    private final UUID formationId;

    public PresenceCreatedEvent(Object source, UUID apprenantId, UUID seanceId, UUID coursId, UUID formationId) {
        super(source);
        this.apprenantId = apprenantId;
        this.seanceId = seanceId;
        this.coursId = coursId;
        this.formationId = formationId;
    }
}
