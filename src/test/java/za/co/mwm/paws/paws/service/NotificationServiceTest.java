package za.co.mwm.paws.paws.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.dto.IncidentResponse;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String INCIDENTS_TOPIC = "/topic/incidents";

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(messagingTemplate);
    }

    @Test
    void givenIncidentResponse_whenBroadcastIncidentUpdate_shouldSendToCorrectTopic() {
        final IncidentResponse response =
                IncidentResponse.builder()
                        .id(1L)
                        .type(IncidentType.POACHING)
                        .status(IncidentStatus.RECEIVED)
                        .description("Snare found")
                        .build();

        notificationService.broadcastIncidentUpdate(response);

        verify(messagingTemplate).convertAndSend(eq(INCIDENTS_TOPIC), eq(response));
    }
}

