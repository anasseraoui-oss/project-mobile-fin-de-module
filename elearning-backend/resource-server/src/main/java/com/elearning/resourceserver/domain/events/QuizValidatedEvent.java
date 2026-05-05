package com.elearning.resourceserver.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class QuizValidatedEvent extends ApplicationEvent {
    private final UUID apprenantId;
    private final UUID coursId;
    private final UUID formationId;
    private final BigDecimal score;

    public QuizValidatedEvent(Object source, UUID apprenantId, UUID coursId,
                              UUID formationId, BigDecimal score) {
        super(source);
        this.apprenantId = apprenantId;
        this.coursId = coursId;
        this.formationId = formationId;
        this.score = score;
    }
}
