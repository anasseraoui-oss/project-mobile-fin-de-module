package com.elearning.resourceserver.application.scheduler;

import com.elearning.resourceserver.application.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    // private final SeanceRepository seanceRepository;

    // S'exécute toutes les minutes
    @Scheduled(fixedRate = 60000)
    public void notifyUpcomingLiveSessions() {
        log.info("Checking for upcoming live sessions...");
        
        // Pseudo code pour la logique de DB:
        // LocalDateTime targetTime = LocalDateTime.now().plusMinutes(30);
        // List<Seance> upcomingSeances = seanceRepository.findLiveSessionsStartingAt(targetTime);
        
        // for (Seance seance : upcomingSeances) {
        //     String topic = "course_" + seance.getCourse().getId();
        //     notificationService.sendToTopic(topic, 
        //             "Séance Live Imminente", 
        //             "La séance " + seance.getTitle() + " commence dans 30 minutes.", 
        //             Map.of("seanceId", seance.getId().toString(), "type", "LIVE_REMINDER"));
        // }
    }
}
