package za.co.mwm.paws.paws.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sightings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sighting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String species;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private String photoPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}

