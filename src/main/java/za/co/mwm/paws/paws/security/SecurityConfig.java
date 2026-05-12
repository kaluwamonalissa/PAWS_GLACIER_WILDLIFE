package za.co.mwm.paws.paws.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ROLE_SENIOR_MANAGER = "SENIOR_MANAGER";
    private static final String ROLE_MANAGER        = "MANAGER";
    private static final String ROLE_TEAM_LEADER    = "TEAM_LEADER";
    private static final String ROLE_RANGER         = "RANGER";
    private static final String ROLE_ANALYST        = "ANALYST";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**", "/uploads/**", "/", "/index.html",
                                "/manifest.json", "/service-worker.js",
                                "/css/**", "/js/**", "/icons/**", "/h2-console/**")
                        .permitAll()
                        .requestMatchers("/api/admin/**")
                        .hasRole(ROLE_SENIOR_MANAGER)
                        .requestMatchers("/api/analytics/**")
                        .hasAnyRole(ROLE_ANALYST, ROLE_SENIOR_MANAGER)
                        .requestMatchers("/api/dashboard/**")
                        .hasAnyRole(ROLE_MANAGER, ROLE_SENIOR_MANAGER, ROLE_ANALYST)
                        .requestMatchers(HttpMethod.GET, "/api/incidents/heatmap")
                        .hasAnyRole(ROLE_MANAGER, ROLE_SENIOR_MANAGER, ROLE_ANALYST)
                        .requestMatchers(HttpMethod.PATCH, "/api/incidents/*/assign/*",
                                "/api/incidents/*/flag")
                        .hasAnyRole(ROLE_MANAGER, ROLE_SENIOR_MANAGER, ROLE_TEAM_LEADER)
                        .requestMatchers(HttpMethod.PATCH, "/api/incidents/*/respond",
                                "/api/incidents/*/update")
                        .hasAnyRole(ROLE_RANGER, ROLE_TEAM_LEADER, ROLE_MANAGER,
                                ROLE_SENIOR_MANAGER)
                        .requestMatchers("/ws/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
