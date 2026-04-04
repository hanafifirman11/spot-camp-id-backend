package com.spotcamp.security;

import com.spotcamp.module.authuser.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import org.springframework.http.HttpMethod;

/**
 * Security configuration for the application
 * Configures JWT authentication, CORS, and endpoint security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.profiles.active:production}")
    private String activeProfile;

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> {
                        // H2 Console only in dev/local
                        if ("dev".equals(activeProfile) || "local".equals(activeProfile)) {
                            authz.requestMatchers("/h2-console/**", "/api/v1/h2-console/**").permitAll();
                        }
                        
                        authz
                        // Public authentication endpoints
                        .requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
                        
                        // Public market endpoints
                        .requestMatchers("/public/**").permitAll()

                        // Public uploads (images, maps)
                        .requestMatchers("/uploads/**").permitAll()
                        
                        // Webhook endpoints (should have separate validation)
                        .requestMatchers("/webhooks/**").permitAll()
                        
                        // Health check and documentation
                        .requestMatchers("/actuator/health", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Map view endpoint (public for browsing)
                        .requestMatchers("/maps/*/view").permitAll()
                        
                        // Product listing (public for browsing)
                        .requestMatchers("/products", "/bundles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/campsites/*/products").permitAll()

                        // Superadmin endpoints
                        .requestMatchers("/admin/**").hasAnyRole("SUPERADMIN", "ADMIN")
                        
                        // Merchant-only endpoints
                        .requestMatchers("/maps/*/config").hasAnyRole("MERCHANT", "MERCHANT_ADMIN", "SUPERADMIN", "ADMIN")
                        .requestMatchers("/products/**", "/bundles/**").hasAnyRole("MERCHANT", "MERCHANT_ADMIN", "MERCHANT_MEMBER", "SUPERADMIN", "ADMIN")
                        .requestMatchers("/reports/**").hasAnyRole("MERCHANT", "MERCHANT_ADMIN", "SUPERADMIN", "ADMIN")
                        
                        // User-specific endpoints
                        .requestMatchers("/cart/**", "/bookings/**", "/users/me/**").hasAnyRole("CAMPER", "MERCHANT", "MERCHANT_ADMIN", "SUPERADMIN", "ADMIN")
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if ("dev".equals(activeProfile) || "local".equals(activeProfile)) {
            http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        }

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/uploads/**",
                "/api/v1/uploads/**"
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*", 
            "https://*.campsite.com", 
            "https://spotcamp.id"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control",
            "Origin"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Set-Cookie",
            "Authorization",
            "Content-Disposition"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
