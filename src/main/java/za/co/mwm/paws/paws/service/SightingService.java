package za.co.mwm.paws.paws.service;

import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.mwm.paws.paws.domain.Sighting;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.SightingResponse;
import za.co.mwm.paws.paws.repository.SightingRepository;
import za.co.mwm.paws.paws.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class SightingService {

    private static final String ERROR_USER_NOT_FOUND = "User not found";

    private final SightingRepository sightingRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public SightingResponse createSighting(
            final String species,
            final String description,
            final Double latitude,
            final Double longitude,
            final MultipartFile photo,
            final String username)
            throws IOException {

        final User reporter =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            photoPath = fileStorageService.storeFile(photo);
        }

        final Sighting sighting =
                Sighting.builder()
                        .species(species)
                        .description(description)
                        .latitude(latitude)
                        .longitude(longitude)
                        .photoPath(photoPath)
                        .reportedBy(reporter)
                        .timestamp(LocalDateTime.now())
                        .build();

        final Sighting saved = sightingRepository.save(sighting);
        return toResponse(saved);
    }

    private SightingResponse toResponse(final Sighting sighting) {
        return SightingResponse.builder()
                .id(sighting.getId())
                .species(sighting.getSpecies())
                .description(sighting.getDescription())
                .latitude(sighting.getLatitude())
                .longitude(sighting.getLongitude())
                .photoUrl(sighting.getPhotoPath())
                .reportedBy(sighting.getReportedBy().getFullName())
                .timestamp(sighting.getTimestamp())
                .build();
    }
}

