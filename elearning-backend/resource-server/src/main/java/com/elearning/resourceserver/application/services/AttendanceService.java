package com.elearning.resourceserver.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final StringRedisTemplate redisTemplate;
    // private final AttendanceRepository attendanceRepository;

    public String generateQrCodeToken(UUID seanceId) {
        String token = UUID.randomUUID().toString();
        String redisKey = "qrToken:" + token;
        
        // Stockage du seanceId avec une expiration (TTL) de 5 minutes
        redisTemplate.opsForValue().set(redisKey, seanceId.toString(), 5, TimeUnit.MINUTES);
        
        return token;
    }

    public void scanQrCode(UUID userId, String token) {
        String redisKey = "qrToken:" + token;
        String seanceIdStr = redisTemplate.opsForValue().get(redisKey);
        
        if (seanceIdStr == null) {
            throw new RuntimeException("QR Code invalide ou expiré.");
        }
        
        UUID seanceId = UUID.fromString(seanceIdStr);
        
        // Save to DB
        // Attendance attendance = new Attendance(userId, seanceId, token);
        // attendanceRepository.save(attendance);
    }
}
