package com.elearning.resourceserver.application.scheduler;

import com.elearning.resourceserver.application.services.NotificationService;
import com.elearning.resourceserver.domain.Seance;
import com.elearning.resourceserver.domain.enums.SeanceStatus;
import com.elearning.resourceserver.domain.enums.SeanceType;
import com.elearning.resourceserver.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final SeanceRepository seanceRepository;

    @Scheduled(fixedDelayString = "${notifications.live-scheduler-delay-ms:5000}")
    public void notifyUpcomingLiveSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<Seance> dueLiveSessions = seanceRepository.findUpcomingLiveSessions(
                SeanceType.LIVE,
                SeanceStatus.PLANIFIEE,
                now.minusSeconds(5),
                now.plusSeconds(30)
        );

        for (Seance seance : dueLiveSessions) {
            UUID formationId = seance.getCourse() != null && seance.getCourse().getFormation() != null
                    ? seance.getCourse().getFormation().getId()
                    : null;
            if (formationId == null) {
                log.warn("Live session {} has no formation; notification skipped", seance.getId());
                continue;
            }

            Map<String, String> data = new HashMap<>();
            data.put("type", "LIVE_REMINDER");
            data.put("seanceId", seance.getId().toString());
            data.put("formationId", formationId.toString());
            if (seance.getMeetingLink() != null && !seance.getMeetingLink().isBlank()) {
                data.put("meetingLink", seance.getMeetingLink());
            }

            int recipients = notificationService.sendToFormationSubscribers(
                    formationId,
                    "Seance live maintenant",
                    "La seance '" + seance.getTitle() + "' commence maintenant.",
                    data,
                    "live-reminder:" + seance.getId()
            );
            log.info("Live reminder sent for seance {} to {} recipients", seance.getId(), recipients);
        }
    }
}
