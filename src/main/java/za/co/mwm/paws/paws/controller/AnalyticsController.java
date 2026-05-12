package za.co.mwm.paws.paws.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.mwm.paws.paws.dto.ModelMetricsResponse;
import za.co.mwm.paws.paws.dto.WhatIfRequest;
import za.co.mwm.paws.paws.dto.WhatIfResponse;
import za.co.mwm.paws.paws.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/model-metrics")
    public ResponseEntity<ModelMetricsResponse> getModelMetrics() {
        return ResponseEntity.ok(analyticsService.getModelMetrics());
    }

    @PostMapping("/whatif")
    public ResponseEntity<WhatIfResponse> simulateWhatIf(
            @Valid @RequestBody final WhatIfRequest request) {
        return ResponseEntity.ok(analyticsService.simulateWhatIf(request));
    }
}

