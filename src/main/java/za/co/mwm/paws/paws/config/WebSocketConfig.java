package za.co.mwm.paws.paws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String WS_ENDPOINT = "/ws";
    private static final String TOPIC_PREFIX = "/topic";
    private static final String APP_DESTINATION_PREFIX = "/app";

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint(WS_ENDPOINT).setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(TOPIC_PREFIX);
        registry.setApplicationDestinationPrefixes(APP_DESTINATION_PREFIX);
    }
}

