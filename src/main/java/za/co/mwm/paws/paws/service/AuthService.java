package za.co.mwm.paws.paws.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.mwm.paws.paws.domain.Role;
import za.co.mwm.paws.paws.domain.User;
import za.co.mwm.paws.paws.dto.LoginRequest;
import za.co.mwm.paws.paws.dto.LoginResponse;
import za.co.mwm.paws.paws.dto.RegisterRequest;
import za.co.mwm.paws.paws.repository.UserRepository;
import za.co.mwm.paws.paws.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    private static final String ERROR_USERNAME_TAKEN = "Username already taken";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(final LoginRequest request) {
        final User user =
                userRepository
                        .findByUsername(request.getUsername())
                        .orElseThrow(() -> new IllegalArgumentException(ERROR_INVALID_CREDENTIALS));

        if (!user.isActive()) {
            throw new IllegalArgumentException(ERROR_INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(ERROR_INVALID_CREDENTIALS);
        }

        final String token =
                jwtTokenProvider.generateToken(
                        user.getId(), user.getUsername(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .userId(user.getId())
                .build();
    }

    public LoginResponse register(final RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(ERROR_USERNAME_TAKEN);
        }

        final User user =
                User.builder()
                        .username(request.getUsername())
                        .passwordHash(passwordEncoder.encode(request.getPassword()))
                        .fullName(request.getFullName())
                        .role(Role.RANCHER)
                        .build();

        final User saved = userRepository.save(user);

        final String token =
                jwtTokenProvider.generateToken(
                        saved.getId(), saved.getUsername(), saved.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .role(saved.getRole().name())
                .username(saved.getUsername())
                .fullName(saved.getFullName())
                .userId(saved.getId())
                .build();
    }
}

