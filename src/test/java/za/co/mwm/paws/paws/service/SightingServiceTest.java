package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.Sighting;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.SightingResponse;
import za.co.mwm.paws.paws.repository.SightingRepository;
import za.co.mwm.paws.paws.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SightingServiceTest {

    @Mock
    private SightingRepository sightingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    private SightingService sightingService;

    @BeforeEach
    void setUp() {
        sightingService = new SightingService(sightingRepository, userRepository, fileStorageService);
    }

    @Test
    void givenValidSighting_andPhotoFile_whenCreateSighting_shouldReturnSightingWithPhotoUrl()
            throws IOException {
        final User ranger = buildRanger();
        final MockMultipartFile photo =
                new MockMultipartFile("photo", "img.jpg", "image/jpeg", "data".getBytes());

        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(ranger));
        when(fileStorageService.storeFile(photo)).thenReturn("/uploads/img.jpg");
        when(sightingRepository.save(any())).thenAnswer(inv -> {
            final Sighting s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        final SightingResponse response =
                sightingService.createSighting(
                        "African Elephant", "Large herd at waterhole", -18.93, 26.48, photo, "ranger1");

        assertThat(response.getPhotoUrl()).isEqualTo("/uploads/img.jpg");
        assertThat(response.getSpecies()).isEqualTo("African Elephant");
    }

    @Test
    void givenValidSighting_andNoPhoto_whenCreateSighting_shouldReturnSightingWithNullPhotoUrl()
            throws IOException {
        final User ranger = buildRanger();
        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(ranger));
        when(sightingRepository.save(any())).thenAnswer(inv -> {
            final Sighting s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        final SightingResponse response =
                sightingService.createSighting(
                        "Sable Antelope", "Single male spotted", -18.95, 26.47, null, "ranger1");

        assertThat(response.getPhotoUrl()).isNull();
    }

    @Test
    void givenEmptyPhotoFile_whenCreateSighting_shouldNotStoreFile() throws IOException {
        final User ranger = buildRanger();
        final MockMultipartFile emptyPhoto =
                new MockMultipartFile("photo", "empty.jpg", "image/jpeg", new byte[0]);
        when(userRepository.findByUsername("ranger1")).thenReturn(Optional.of(ranger));
        when(sightingRepository.save(any())).thenAnswer(inv -> {
            final Sighting s = inv.getArgument(0);
            s.setId(3L);
            return s;
        });

        final SightingResponse response =
                sightingService.createSighting(
                        "Wild Dog", "Pack of 5", -18.91, 26.49, emptyPhoto, "ranger1");

        assertThat(response.getPhotoUrl()).isNull();
    }

    @Test
    void givenUnknownUser_whenCreateSighting_shouldThrowIllegalArgumentException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                sightingService.createSighting(
                                        "Elephant", "desc", -18.9, 26.4, null, "ghost"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    private User buildRanger() {
        return User.builder()
                .id(1L)
                .username("ranger1")
                .fullName("Ranger Blessing Dube")
                .role(Role.RANGER)
                .build();
    }
}

