package com.elearning.resourceserver.infrastructure.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FcmConfig {

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            // Dans un vrai projet, utilisez une variable d'environnement ou le classpath pour le fichier JSON
            InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
            
            if (serviceAccount == null) {
                // Return null if not configured to avoid app crash during context load, 
                // handle cautiously in NotificationService.
                return null; 
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            } else {
                return FirebaseApp.getInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
