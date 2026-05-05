package com.elearning.resourceserver.domain.events;

import com.elearning.resourceserver.domain.enums.TentativeQuizStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class QuizSubmittedEvent extends ApplicationEvent {
    private final UUID apprenantId;
    private final UUID quizId;
    private final UUID coursId;
    private final UUID formationId;
    private final BigDecimal score;
    private final TentativeQuizStatus status;

    public QuizSubmittedEvent(Object source, UUID apprenantId, UUID quizId, UUID coursId,
                              UUID formationId, BigDecimal score, TentativeQuizStatus status) {
        super(source);
        this.apprenantId = apprenantId;
        this.quizId = quizId;
        this.coursId = coursId;
        this.formationId = formationId;
        this.score = score;
        this.status = status;
    }
}
