package za.co.mwm.paws.paws.config;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.co.mwm.paws.paws.domain.Incident;
import za.co.mwm.paws.paws.domain.IncidentResponder;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.domain.Patrol;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.repository.IncidentRepository;
import za.co.mwm.paws.paws.repository.IncidentResponderRepository;
import za.co.mwm.paws.paws.repository.PatrolRepository;
import za.co.mwm.paws.paws.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD   = "password";
    private static final String WARD_DETE          = "Ward 1 — Dete";
    private static final String WARD_HWANGE        = "Ward 2 — Hwange Town";
    private static final String WARD_NGAMO         = "Ward 3 — Ngamo";

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentResponderRepository incidentResponderRepository;
    private final PatrolRepository patrolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(final ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Seed data already present — skipping initialisation");
            return;
        }

        log.info("Seeding PAWS demo data for Hwange National Park...");
        final String hash = passwordEncoder.encode(DEFAULT_PASSWORD);

        // Users
        final User seniorManager  = save(user("seniormanager",   "Chief Warden Grace Moyo",        Role.SENIOR_MANAGER, null,        hash));
        final User manager        = save(user("manager",         "Field Manager Tendai Chuma",      Role.MANAGER,        null,        hash));
        final User analyst        = save(user("analyst",         "Data Analyst Farai Mutasa",       Role.ANALYST,        null,        hash));
        final User teamLeader     = save(user("teamleader",      "Team Leader Sipho Ndlovu",        Role.TEAM_LEADER,    WARD_DETE,   hash));
        final User ranger1        = save(user("ranger1",         "Ranger Blessing Dube",            Role.RANGER,         WARD_DETE,   hash));
        final User ranger2        = save(user("ranger2",         "Ranger Nomvula Khumalo",          Role.RANGER,         WARD_HWANGE, hash));
        final User rancher1       = save(user("rancher1",        "Themba Mpofu",                   Role.RANCHER,        WARD_DETE,   hash));
        final User rancher2       = save(user("rancher2",        "Zanele Sibanda",                 Role.RANCHER,        WARD_NGAMO,  hash));

        // Incidents within Hwange Main Camp 20km² bbox
        final Incident i1 = saveIncident(IncidentType.POACHING,                IncidentStatus.RECEIVED,        "Suspected snare near watering hole 3",      -18.890, 26.445, WARD_DETE,   "African Elephant", null,       ranger1,    daysAgo(0, 6, 15));
        final Incident i2 = saveIncident(IncidentType.POACHING,                IncidentStatus.RANGER_ASSIGNED, "Gunshot heard south of Main Camp at dawn",  -18.965, 26.510, WARD_DETE,   null,               null,       ranger2,    daysAgo(0, 5, 45));
        final Incident i3 = saveIncident(IncidentType.HUMAN_WILDLIFE_CONFLICT, IncidentStatus.RECEIVED,        "Elephant herd approaching village boundary",-18.910, 26.475, WARD_HWANGE, "African Elephant", "Maize",    rancher1,   daysAgo(0, 7, 0));
        final Incident i4 = saveIncident(IncidentType.VELD_FIRE,               IncidentStatus.RECEIVED,        "Smoke visible from eastern grasslands",     -18.930, 26.520, WARD_DETE,   null,               null,       ranger1,    daysAgo(0, 8, 30));
        final Incident i5 = saveIncident(IncidentType.WATER_PAN,               IncidentStatus.RECEIVED,        "Pan 7 drying up — only 30cm depth",         -18.945, 26.460, WARD_NGAMO,  null,               null,       ranger2,    daysAgo(0, 9, 0));
        saveIncident(IncidentType.FORAGE,                  IncidentStatus.RESOLVED,        "Overgrazing in sector B",                   -18.890, 26.500, WARD_NGAMO,  null,               null,       teamLeader, daysAgo(1, 14, 0));
        saveIncident(IncidentType.POACHING,                IncidentStatus.RECEIVED,        "Fresh poacher tracks with heavy load",      -18.975, 26.435, WARD_HWANGE, "White Rhinoceros",  null,       rancher1,   daysAgo(0, 4, 20));
        final Incident i8 = saveIncident(IncidentType.HUMAN_WILDLIFE_CONFLICT, IncidentStatus.RANGER_ASSIGNED, "Buffalo blocking main access road",         -18.920, 26.488, WARD_DETE,   "Buffalo",          null,       rancher2,   daysAgo(0, 10, 0));
        saveIncident(IncidentType.WATER_PAN,               IncidentStatus.RECEIVED,        "Pan 2 algae bloom — animals avoiding it",   -18.895, 26.455, WARD_NGAMO,  null,               null,       ranger1,    daysAgo(0, 11, 0));
        saveIncident(IncidentType.VELD_FIRE,               IncidentStatus.RESOLVED,        "Small fire contained near gate 1",          -18.955, 26.498, WARD_HWANGE, null,               null,       ranger2,    daysAgo(1, 16, 30));

        // Assign rangers to incidents
        i2.setAssignedRanger(ranger1);
        incidentRepository.save(i2);
        i8.setAssignedRanger(ranger2);
        incidentRepository.save(i8);

        incidentResponderRepository.save(IncidentResponder.builder().incident(i2).ranger(ranger1).assignedAt(daysAgo(0, 5, 50)).build());
        incidentResponderRepository.save(IncidentResponder.builder().incident(i8).ranger(ranger2).assignedAt(daysAgo(0, 10, 5)).build());

        // Sample patrol
        patrolRepository.save(Patrol.builder()
                .ranger(ranger1).startLatitude(-18.920).startLongitude(26.460)
                .endLatitude(-18.945).endLongitude(26.490)
                .distanceKm(4.2).snarersRemoved(3).wildlifeObserved(12)
                .notes("Removed 3 wire snares near waterhole. Observed elephant herd of ~20.")
                .ward(WARD_DETE)
                .startedAt(daysAgo(0, 6, 0)).endedAt(daysAgo(0, 9, 0))
                .build());

        log.info("Seed complete: 8 users, 10 incidents, 2 responders, 1 patrol");
        log.info("Credentials (password='password'): seniormanager / manager / analyst / teamleader / ranger1 / ranger2 / rancher1 / rancher2");
    }

    private User user(final String username, final String fullName, final Role role,
            final String ward, final String hash) {
        return User.builder().username(username).fullName(fullName)
                .role(role).ward(ward).passwordHash(hash).active(true).build();
    }

    private User save(final User user) {
        return userRepository.save(user);
    }

    private Incident saveIncident(final IncidentType type, final IncidentStatus status,
            final String description, final double lat, final double lng,
            final String ward, final String species, final String cropType,
            final User reportedBy, final LocalDateTime timestamp) {
        return incidentRepository.save(Incident.builder()
                .type(type).status(status).description(description)
                .latitude(lat).longitude(lng).ward(ward)
                .species(species).cropLivestockType(cropType)
                .reportedBy(reportedBy).timestamp(timestamp)
                .build());
    }

    private LocalDateTime daysAgo(final int days, final int hour, final int minute) {
        return LocalDateTime.now().minusDays(days).withHour(hour).withMinute(minute).withSecond(0);
    }
}

