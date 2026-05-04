package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.domain.Formation;
import com.elearning.resourceserver.domain.User;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final MinioClient minioClient;
    private static final String MEDIA_BUCKET = "elearning-media";

    public String generateAndUploadCertificate(User user, Formation formation, int score) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // 1. Generate PDF using iText7
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            document.setTextAlignment(TextAlignment.CENTER);
            document.add(new Paragraph("CERTIFICAT DE RÉUSSITE").setFontSize(24).setBold().setMarginBottom(20));
            document.add(new Paragraph("Décerné à").setFontSize(14));
            document.add(new Paragraph(user.getEmail()).setFontSize(18).setBold().setMarginBottom(20)); // Assume email is name for now
            document.add(new Paragraph("Pour avoir complété avec succès la formation :"));
            document.add(new Paragraph(formation.getTitle()).setFontSize(16).setBold().setMarginBottom(20));
            
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            document.add(new Paragraph("Score obtenu : " + score + "%"));
            document.add(new Paragraph("Date : " + dateStr));
            
            document.close();

            // 2. Upload to MinIO
            byte[] pdfBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
            String objectKey = "certificates/" + user.getId() + "/" + formation.getId() + ".pdf";

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(MEDIA_BUCKET)
                    .object(objectKey)
                    .stream(bais, pdfBytes.length, -1)
                    .contentType("application/pdf")
                    .build()
            );

            log.info("Certificate generated and uploaded to minio: {}", objectKey);
            return objectKey;

        } catch (Exception e) {
            log.error("Error generating certificate", e);
            throw new RuntimeException("Could not generate certificate", e);
        }
    }
}
