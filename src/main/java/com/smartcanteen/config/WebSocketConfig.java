package com.smartcanteen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// Enable simple in-memory broker for /topic prefixed destinations
		registry.enableSimpleBroker("/topic");
		// Prefix for messages routed to @MessageMapping methods
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// Frontend connects to ws://localhost:8080/ws
		// SockJS fallback for browsers that don't support native WebSocket
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
	}
}
