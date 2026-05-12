package za.co.mwm.paws.paws.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.mwm.paws.paws.domain.IncidentStatus;
import za.co.mwm.paws.paws.domain.IncidentType;
import za.co.mwm.paws.paws.dto.DashboardSummaryResponse;
import za.co.mwm.paws.paws.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) final String ward,
            @RequestParam(required = false) final IncidentStatus status,
            @RequestParam(required = false) final IncidentType type) {
        final String csv = dashboardService.exportCsv(ward, status, type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=incidents.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}

