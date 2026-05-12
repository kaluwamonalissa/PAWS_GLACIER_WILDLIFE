package za.co.mwm.paws.paws.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.mwm.paws.paws.dto.PatrolRequest;
import za.co.mwm.paws.paws.dto.PatrolResponse;
import za.co.mwm.paws.paws.service.PatrolService;

@RestController
@RequestMapping("/api/patrols")
@RequiredArgsConstructor
public class PatrolController {

    private final PatrolService patrolService;

    @PostMapping
    public ResponseEntity<PatrolResponse> logPatrol(
            @Valid @RequestBody final PatrolRequest request, final Principal principal) {
        return ResponseEntity.ok(patrolService.logPatrol(request, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<PatrolResponse>> getAllPatrols() {
        return ResponseEntity.ok(patrolService.getAllPatrols());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<PatrolResponse>> getMyPatrols(final Principal principal) {
        return ResponseEntity.ok(patrolService.getMyPatrols(principal.getName()));
    }
}

