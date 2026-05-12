package za.co.mwm.paws.paws.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.mwm.paws.paws.dto.WildlifeCategoryResponse;
import za.co.mwm.paws.paws.service.WildlifeService;

@RestController
@RequestMapping("/api/wildlife")
@RequiredArgsConstructor
public class WildlifeController {

    private final WildlifeService wildlifeService;

    @GetMapping("/categories")
    public ResponseEntity<List<WildlifeCategoryResponse>> getCategories() {
        return ResponseEntity.ok(wildlifeService.getEndangeredCategories());
    }
}

