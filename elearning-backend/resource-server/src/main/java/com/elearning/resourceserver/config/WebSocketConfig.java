package com.elearning.resourceserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker in-memory pour les abonnements
        config.enableSimpleBroker("/topic");
        
        // Préfixe pour les messages envoyés par les clients vers le serveur
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket principal avec un fallback SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // En production, restreindre aux domaines de l'App / Web
                .withSockJS();
    }
}
