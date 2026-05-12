package za.co.mwm.paws.paws.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private String ward;

    @Column
    private String species;

    @Column
    private String cropLivestockType;

    @Column(precision = 12, scale = 2)
    private BigDecimal damageEstimate;

    @Column(length = 1000)
    private String actionTaken;

    @Column(length = 500)
    private String deterrentsUsed;

    @Column(length = 500)
    private String outcome;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_ranger_id")
    private User assignedRanger;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private LocalDateTime resolvedAt;
}
