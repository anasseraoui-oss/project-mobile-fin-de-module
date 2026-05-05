package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.Presence;
import com.elearning.resourceserver.domain.Seance;
import com.elearning.resourceserver.domain.enums.PresenceStatus;
import com.elearning.resourceserver.domain.enums.SeanceStatus;
import com.elearning.resourceserver.domain.enums.SeanceType;
import com.elearning.resourceserver.domain.enums.ValidationMethod;
import com.elearning.resourceserver.domain.events.PresenceCreatedEvent;
import com.elearning.resourceserver.exceptions.AccessDeniedException;
import com.elearning.resourceserver.exceptions.ResourceNotFoundException;
import com.elearning.resourceserver.exceptions.ValidationException;
import com.elearning.resourceserver.repository.InscriptionRepository;
import com.elearning.resourceserver.repository.PresenceRepository;
import com.elearning.resourceserver.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redisTemplate;
    private final PresenceRepository presenceRepository;
    private final SeanceRepository seanceRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * UC-02: Generate QR code for a live session (FORMATEUR)
     */
    @Transactional
    public Map<String, Object> generateQrCodeToken(UUID seanceId, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        // Verify ownership
        if (!seance.getFormateurId().equals(formateurId)) {
            throw new AccessDeniedException("Vous n'êtes pas le formateur de cette séance");
        }

        // Verify LIVE type
        if (seance.getType() != SeanceType.LIVE) {
            throw new ValidationException("Seul un type LIVE peut générer un QR code de présence");
        }

        // Verify session EN_COURS
        if (seance.getStatus() != SeanceStatus.EN_COURS) {
            throw new ValidationException("La séance doit être en cours pour générer un QR code");
        }

        String token = UUID.randomUUID().toString();
        seance.setQrCodeToken(token);

        // RB-04: qrCodeExpiresAt = scheduledAt + duration + 15min
        int durationMinutes = seance.getDuration() != null ? seance.getDuration() : 60;
        LocalDateTime expiresAt = seance.getScheduledAt()
                .plusMinutes(durationMinutes)
                .plusMinutes(15);
        seance.setQrCodeExpiresAt(expiresAt);
        seanceRepository.save(seance);

        // Store in Redis: qr:{token} = seanceId, TTL = duration*60 + 900
        long ttlSeconds = (long) durationMinutes * 60 + 900;
        redisTemplate.opsForValue().set("qr:" + token, seanceId.toString(), ttlSeconds, TimeUnit.SECONDS);

        return Map.of(
                "qrToken", token,
                "expiresAt", expiresAt.toString(),
                "qrCodeExpiresAt", expiresAt.toString()
        );
    }

    /**
     * UC-02: Scan QR code for presence (APPRENANT) — complete with RB-04
     */
    @Transactional
    public Map<String, String> scanQrCode(UUID apprenantId, String token, String ipAddress) {
        // Step 1: Verify token in Redis
        String redisKey = "qr:" + token;
        String seanceIdStr = redisTemplate.opsForValue().get(redisKey);
        if (seanceIdStr == null) {
            throw new ResourceNotFoundException("QR Code invalide ou expiré");
        }

        UUID seanceId = UUID.fromString(seanceIdStr);
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        // Step 3: RB-04 — Verify temporal window
        LocalDateTime now = LocalDateTime.now();
        if (seance.getScheduledAt() != null && now.isBefore(seance.getScheduledAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "La séance n'a pas encore commencé");
        }
        if (seance.getQrCodeExpiresAt() != null && now.isAfter(seance.getQrCodeExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Hors délai — le QR code a expiré");
        }

        // Step 4: Verify inscription to formation
        UUID coursId = seance.getCoursId();
        UUID formationId = seance.getCourse().getFormation().getId();
        if (!inscriptionRepository.existsByApprenantIdAndFormationId(apprenantId, formationId)) {
            throw new AccessDeniedException("Vous n'êtes pas inscrit à cette formation");
        }

        // Step 5: Verify no duplicate presence
        if (presenceRepository.existsByApprenantIdAndSeanceId(apprenantId, seanceId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Présence déjà enregistrée pour cette séance");
        }

        // Step 6: RB-04 — Determine status (RETARD if > 15min after scheduledAt)
        PresenceStatus status = PresenceStatus.PRESENT;
        if (seance.getScheduledAt() != null && now.isAfter(seance.getScheduledAt().plusMinutes(15))) {
            status = PresenceStatus.RETARD;
        }

        // Step 7: Save Presence
        Presence presence = new Presence();
        presence.setApprenantId(apprenantId);
        presence.setSeanceId(seanceId);
        presence.setStatus(status);
        presence.setMarkedAt(now);
        presence.setValidationMethod(ValidationMethod.QR_CODE);
        presence.setIpAddress(ipAddress);
        presenceRepository.save(presence);

        // Step 8: Delete Redis key (usage unique)
        redisTemplate.delete(redisKey);

        // Step 9: Publish PresenceCreatedEvent → recalculate presenceRate async
        eventPublisher.publishEvent(new PresenceCreatedEvent(this, apprenantId, seanceId, coursId, formationId));

        return Map.of(
                "message", "Présence validée avec succès",
                "status", status.name()
        );
    }

    /**
     * Manual code presence (alternative to QR)
     */
    @Transactional
    public Map<String, String> scanManualCode(UUID apprenantId, String code, String ipAddress) {
        String redisKey = "presence:manual:" + code;
        String seanceIdStr = redisTemplate.opsForValue().get(redisKey);
        if (seanceIdStr == null) {
            throw new ResourceNotFoundException("Code invalide ou expiré");
        }

        UUID seanceId = UUID.fromString(seanceIdStr);
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        UUID formationId = seance.getCourse().getFormation().getId();

        if (!inscriptionRepository.existsByApprenantIdAndFormationId(apprenantId, formationId)) {
            throw new AccessDeniedException("Vous n'êtes pas inscrit à cette formation");
        }

        if (presenceRepository.existsByApprenantIdAndSeanceId(apprenantId, seanceId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Présence déjà enregistrée");
        }

        LocalDateTime now = LocalDateTime.now();
        PresenceStatus status = PresenceStatus.PRESENT;
        if (seance.getScheduledAt() != null && now.isAfter(seance.getScheduledAt().plusMinutes(15))) {
            status = PresenceStatus.RETARD;
        }

        Presence presence = new Presence();
        presence.setApprenantId(apprenantId);
        presence.setSeanceId(seanceId);
        presence.setStatus(status);
        presence.setMarkedAt(now);
        presence.setValidationMethod(ValidationMethod.CODE_MANUEL);
        presence.setIpAddress(ipAddress);
        presenceRepository.save(presence);

        redisTemplate.delete(redisKey);

        eventPublisher.publishEvent(new PresenceCreatedEvent(
                this, apprenantId, seanceId, seance.getCoursId(), formationId));

        return Map.of("message", "Présence validée", "status", status.name());
    }

    /**
     * List attendees for a session (FORMATEUR)
     */
    @Transactional(readOnly = true)
    public List<Presence> listPresences(UUID seanceId) {
        return presenceRepository.findBySeanceId(seanceId);
    }

    /**
     * Generate manual 6-digit code
     */
    public Map<String, String> generateManualCode(UUID seanceId, UUID formateurId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        if (!seance.getFormateurId().equals(formateurId)) {
            throw new AccessDeniedException("Vous n'êtes pas le formateur de cette séance");
        }

        String code = String.format("%06d", (int) (Math.random() * 999999));
        redisTemplate.opsForValue().set("presence:manual:" + code, seanceId.toString(), 300, TimeUnit.SECONDS);

        return Map.of("code", code, "expiresInSeconds", "300");
    }
}
