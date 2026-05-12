package za.co.mwm.paws.paws.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.dto.HeatmapPoint;
import za.co.mwm.paws.paws.dto.IncidentRequest;
import za.co.mwm.paws.paws.dto.IncidentResponse;
import za.co.mwm.paws.paws.dto.IncidentUpdateRequest;
import za.co.mwm.paws.paws.service.IncidentService;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody final IncidentRequest request, final Principal principal) {
        return ResponseEntity.ok(incidentService.createIncident(request, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAllIncidents(
            @RequestParam(required = false) final String ward,
            @RequestParam(required = false) final IncidentStatus status,
            @RequestParam(required = false) final IncidentType type) {
        return ResponseEntity.ok(incidentService.getAllIncidents(ward, status, type));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<IncidentResponse>> getMyIncidents(final Principal principal) {
        return ResponseEntity.ok(incidentService.getMyIncidents(principal.getName()));
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<IncidentResponse> respondToIncident(
            @PathVariable final Long id, final Principal principal) {
        return ResponseEntity.ok(incidentService.respondToIncident(id, principal.getName()));
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable final Long id,
            @Valid @RequestBody final IncidentUpdateRequest request,
            final Principal principal) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request, principal.getName()));
    }

    @PatchMapping("/{id}/assign/{rangerId}")
    public ResponseEntity<IncidentResponse> assignIncident(
            @PathVariable final Long id, @PathVariable final Long rangerId) {
        return ResponseEntity.ok(incidentService.assignIncident(id, rangerId));
    }

    @PatchMapping("/{id}/flag")
    public ResponseEntity<IncidentResponse> flagIncident(
            @PathVariable final Long id, @RequestParam final IncidentStatus status) {
        return ResponseEntity.ok(incidentService.flagIncident(id, status));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapPoint>> getHeatmap() {
        return ResponseEntity.ok(incidentService.getHeatmap());
    }
}
