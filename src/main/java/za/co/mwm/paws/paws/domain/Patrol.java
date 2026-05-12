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
@Table(name = "patrols")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patrol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ranger_id", nullable = false)
    private User ranger;

    @Column(nullable = false)
    private Double startLatitude;

    @Column(nullable = false)
    private Double startLongitude;

    @Column
    private Double endLatitude;

    @Column
    private Double endLongitude;

    @Column
    private Double distanceKm;

    @Column
    private Integer snarersRemoved;

    @Column
    private Integer wildlifeObserved;

    @Column(length = 1000)
    private String notes;

    @Column
    private String ward;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;
}

