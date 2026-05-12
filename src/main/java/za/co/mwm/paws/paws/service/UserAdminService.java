package za.co.mwm.paws.paws.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.UserResponse;
import za.co.mwm.paws.paws.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private static final String ERROR_USER_NOT_FOUND = "User not found";
    private static final String ERROR_CANNOT_ASSIGN_SENIOR_MANAGER =
            "SENIOR_MANAGER role cannot be assigned via API";

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse updateUserRole(final Long userId, final Role newRole) {
        if (Role.SENIOR_MANAGER.equals(newRole)) {
            throw new IllegalArgumentException(ERROR_CANNOT_ASSIGN_SENIOR_MANAGER);
        }

        final User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        user.setRole(newRole);
        final User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse deactivateUser(final Long userId) {
        return setActiveStatus(userId, false);
    }

    @Transactional
    public UserResponse activateUser(final Long userId) {
        return setActiveStatus(userId, true);
    }

    private UserResponse setActiveStatus(final Long userId, final boolean active) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));
        user.setActive(active);
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(final User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .ward(user.getWard())
                .active(user.isActive())
                .build();
    }
}

