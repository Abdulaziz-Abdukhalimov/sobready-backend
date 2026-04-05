package com.sobready.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * NestJS equivalent would be configuring Passport + Guards:
 *
 *   @Module({ imports: [PassportModule, JwtModule.register({...})] })
 *   export class AuthModule {}
 *
 * @Configuration = tells Spring "this class contains bean definitions"
 *   Think of it like a NestJS @Module() — it configures how things work.
 *
 * @Bean = a method that creates an object Spring will manage.
 *   Like a provider in NestJS: { provide: 'HASH', useFactory: () => bcrypt }
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS using our CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Disable CSRF — we're using JWT tokens, not session cookies for protection
                .csrf(csrf -> csrf.disable())

                // Stateless session — no server-side sessions, JWT handles auth
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL permissions — which endpoints are public vs protected
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — anyone can access
                        .requestMatchers("/product/**").permitAll()
                        .requestMatchers("/user/signup", "/user/login").permitAll()
                        .requestMatchers("/user/top-users", "/user/manager").permitAll()

                        // Static files (product images, etc.)
                        .requestMatchers("/uploads/**").permitAll()

                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                /**
                 * Add our JWT filter BEFORE Spring's default authentication filter.
                 *
                 * NestJS equivalent:
                 *   app.useGlobalGuards(new JwtAuthGuard());
                 *
                 * This means: "For every request, first run JwtAuthenticationFilter
                 * to check for tokens, THEN check the URL permissions above."
                 */
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder — for hashing passwords securely.
     * NEVER store plain text passwords!
     *
     * BCrypt automatically:
     * - Generates a random salt
     * - Hashes the password
     * - Is intentionally slow (prevents brute-force attacks)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
