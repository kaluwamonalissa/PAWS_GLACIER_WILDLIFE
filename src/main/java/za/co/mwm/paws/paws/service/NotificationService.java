package za.co.mwm.paws.paws.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import za.co.mwm.paws.paws.dto.IncidentResponse;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String INCIDENTS_TOPIC = "/topic/incidents";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastIncidentUpdate(final IncidentResponse incidentResponse) {
        messagingTemplate.convertAndSend(INCIDENTS_TOPIC, incidentResponse);
    }
}

