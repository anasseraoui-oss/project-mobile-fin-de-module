package com.elearning.resourceserver.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class OrganisationValidatedEvent extends ApplicationEvent {
    private final UUID organisationId;
    private final UUID validatedBy;

    public OrganisationValidatedEvent(Object source, UUID organisationId, UUID validatedBy) {
        super(source);
        this.organisationId = organisationId;
        this.validatedBy = validatedBy;
    }
}
