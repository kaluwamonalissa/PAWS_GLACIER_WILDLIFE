package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.UserResponse;
import za.co.mwm.paws.paws.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserAdminService userAdminService;

    @BeforeEach
    void setUp() {
        userAdminService = new UserAdminService(userRepository);
    }

    @Test
    void givenExistingUser_andNewRole_whenUpdateUserRole_shouldReturnUpdatedUser() {
        final User user =
                User.builder()
                        .id(5L)
                        .username("community1")
                        .fullName("Themba Mpofu")
                        .role(Role.RANCHER)
                        .active(true)
                        .build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final UserResponse response = userAdminService.updateUserRole(5L, Role.RANGER);

        assertThat(response.getRole()).isEqualTo(Role.RANGER);
    }

    @Test
    void givenSeniorManagerRole_whenUpdateUserRole_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> userAdminService.updateUserRole(1L, Role.SENIOR_MANAGER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SENIOR_MANAGER");
    }

    @Test
    void givenNonExistentUser_whenUpdateUserRole_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAdminService.updateUserRole(99L, Role.MANAGER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void givenActiveUser_whenDeactivate_shouldSetActiveFalse() {
        final User user = User.builder().id(3L).username("ranger1").fullName("Ranger")
                .role(Role.RANGER).active(true).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final UserResponse response = userAdminService.deactivateUser(3L);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    void givenInactiveUser_whenActivate_shouldSetActiveTrue() {
        final User user = User.builder().id(4L).username("ex-ranger").fullName("Ex Ranger")
                .role(Role.RANGER).active(false).build();
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final UserResponse response = userAdminService.activateUser(4L);

        assertThat(response.isActive()).isTrue();
    }

    @Test
    void givenAllUsers_whenGetAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(1L).username("u1").fullName("User 1")
                        .role(Role.RANCHER).active(true).build()));

        assertThat(userAdminService.getAllUsers()).hasSize(1);
    }
}
