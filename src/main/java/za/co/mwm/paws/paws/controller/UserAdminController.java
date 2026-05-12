package za.co.mwm.paws.paws.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.dto.UserResponse;
import za.co.mwm.paws.paws.service.UserAdminService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userAdminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable final Long id, @RequestParam final Role role) {
        return ResponseEntity.ok(userAdminService.updateUserRole(id, role));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable final Long id) {
        return ResponseEntity.ok(userAdminService.deactivateUser(id));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable final Long id) {
        return ResponseEntity.ok(userAdminService.activateUser(id));
    }
}

