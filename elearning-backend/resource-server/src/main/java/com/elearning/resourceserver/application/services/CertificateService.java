package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.*;
import com.elearning.resourceserver.domain.enums.InscriptionStatus;
import com.elearning.resourceserver.domain.enums.QuizStatus;
import com.elearning.resourceserver.repository.*;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final MinioClient minioClient;
    private final com.elearning.resourceserver.infrastructure.minio.MinioService minioService;
    private final CertificatRepository certificatRepository;
    private final ProgressionRepository progressionRepository;
    private final CourseRepository courseRepository;
    private final FormationRepository formationRepository;
    private final UserRepository userRepository;
    private final InscriptionRepository inscriptionRepository;
    private final TentativeQuizRepository tentativeQuizRepository;
    private final OrganisationRepository organisationRepository;
    private final NotificationService notificationService;

    private static final String MEDIA_BUCKET = "elearning-media";

    /**
     * UC-05: Check and generate certificate (RB-06: uniqueness)
     */
    @Transactional
    public Certificat checkAndGenerate(UUID apprenantId, UUID formationId) {
        // RB-06: Check existing certificate
        Optional<Certificat> existing = certificatRepository.findByApprenantIdAndFormationId(apprenantId, formationId);
        if (existing.isPresent()) {
            log.info("Certificate already exists for apprenant={} formation={}, returning existing", apprenantId, formationId);
            return existing.get();
        }

        // Verify all courses are VALIDE
        List<Course> courses = courseRepository.findByFormationIdOrderByOrderIndex(formationId);
        for (Course course : courses) {
            Progression progression = progressionRepository
                    .findByApprenantIdAndCoursId(apprenantId, course.getId())
                    .orElse(null);

            if (progression == null || progression.getQuizStatus() != QuizStatus.VALIDE) {
                log.info("Not all courses validated yet for apprenant={} formation={}", apprenantId, formationId);
                return null; // Not complete yet
            }
        }

        // All validated → Generate
        User user = userRepository.findById(apprenantId).orElse(null);
        Formation formation = formationRepository.findById(formationId).orElse(null);
        if (user == null || formation == null) return null;

        Organisation org = null;
        if (formation.getOrganisationId() != null) {
            org = organisationRepository.findById(formation.getOrganisationId()).orElse(null);
        }

        // Calculate average score
        Double avgScore = tentativeQuizRepository.findAverageScoreByApprenantAndFormation(apprenantId, formationId);
        BigDecimal averageScore = avgScore != null ?
                BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // Generate PDF
        UUID verificationCode = UUID.randomUUID();
        String pdfKey = generatePdf(user, formation, org, averageScore, verificationCode);

        // Save Certificat
        Certificat certificat = new Certificat();
        certificat.setApprenantId(apprenantId);
        certificat.setFormationId(formationId);
        certificat.setOrganisationId(formation.getOrganisationId());
        certificat.setPdfKey(pdfKey);
        certificat.setVerificationCode(verificationCode);
        certificat.setAverageScore(averageScore);
        certificatRepository.save(certificat);

        // Update inscription status
        Inscription inscription = inscriptionRepository
                .findByApprenantIdAndFormationId(apprenantId, formationId)
                .orElse(null);
        if (inscription != null) {
            inscription.setStatus(InscriptionStatus.TERMINEE);
            inscription.setCompletedAt(java.time.LocalDateTime.now());
            inscriptionRepository.save(inscription);
        }

        // Notification
        notificationService.sendToUser(apprenantId,
                "Félicitations !",
                "Votre certificat pour " + formation.getTitle() + " est disponible",
                Map.of("type", "CERTIFICATE", "certId", certificat.getId().toString(), "deepLink", "certificates"));

        log.info("Certificate generated for apprenant={} formation={}", apprenantId, formationId);
        return certificat;
    }

    public String getDownloadUrl(String objectName, int expiryMinutes) {
        try {
            return minioService.generatePresignedUrl(MEDIA_BUCKET, objectName, expiryMinutes);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for " + objectName, e);
            throw new RuntimeException("Erreur de génération du lien");
        }
    }

    private String generatePdf(User user, Formation formation, Organisation org,
                                BigDecimal averageScore, UUID verificationCode) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            // Landscape A4
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdf);

            document.setTextAlignment(TextAlignment.CENTER);

            // Organisation name
            if (org != null) {
                document.add(new Paragraph(org.getName()).setFontSize(14).setMarginTop(30));
            }

            document.add(new Paragraph("CERTIFICAT DE RÉUSSITE")
                    .setFontSize(28).setBold().setMarginTop(40).setMarginBottom(30));

            document.add(new Paragraph("Décerné à").setFontSize(14));
            document.add(new Paragraph(user.getFullName())
                    .setFontSize(22).setBold().setMarginBottom(20));

            document.add(new Paragraph("a complété avec succès").setFontSize(14));
            document.add(new Paragraph(formation.getTitle())
                    .setFontSize(20).setBold().setMarginBottom(20));

            if (org != null) {
                document.add(new Paragraph("Organisation : " + org.getName()).setFontSize(12));
            }

            document.add(new Paragraph("Score moyen : " + averageScore + "%")
                    .setFontSize(12).setMarginTop(15));

            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
            document.add(new Paragraph("Date : " + dateStr).setFontSize(12).setMarginTop(10));

            document.add(new Paragraph("Code de vérification : " + verificationCode.toString())
                    .setFontSize(10).setMarginTop(20));

            document.close();

            // Upload to MinIO
            byte[] pdfBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
            String objectKey = "certificats/" + user.getId() + "/" + formation.getId() + ".pdf";

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(MEDIA_BUCKET)
                            .object(objectKey)
                            .stream(bais, pdfBytes.length, -1)
                            .contentType("application/pdf")
                            .build()
            );

            return objectKey;
        } catch (Exception e) {
            log.error("Error generating certificate PDF", e);
            throw new RuntimeException("Could not generate certificate", e);
        }
    }
}
