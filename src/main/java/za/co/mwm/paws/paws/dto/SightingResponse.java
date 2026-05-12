package za.co.mwm.paws.paws.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SightingResponse {
    private Long id;
    private String species;
    private String description;
    private Double latitude;
    private Double longitude;
    private String photoUrl;
    private String reportedBy;
    private LocalDateTime timestamp;
}

