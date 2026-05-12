package za.co.mwm.paws.paws.controller;

import java.io.IOException;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import za.co.mwm.paws.paws.dto.SightingResponse;
import za.co.mwm.paws.paws.service.SightingService;

@RestController
@RequestMapping("/api/sightings")
@RequiredArgsConstructor
public class SightingController {

    private final SightingService sightingService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SightingResponse> createSighting(
            @RequestParam final String species,
            @RequestParam final String description,
            @RequestParam final Double latitude,
            @RequestParam final Double longitude,
            @RequestParam(required = false) final MultipartFile photo,
            final Principal principal)
            throws IOException {
        return ResponseEntity.ok(
                sightingService.createSighting(
                        species, description, latitude, longitude, photo, principal.getName()));
    }
}

