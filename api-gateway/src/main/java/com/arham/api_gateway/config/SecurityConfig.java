package com.arham.api_gateway.config;

import com.shopflow.error.ProblemDetailAccessDeniedHandler;
import com.shopflow.error.ProblemDetailAuthenticationEntryPoint;
import com.shopflow.jwt.KeycloakJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            KeycloakJwtAuthenticationConverter jwtConverter,
            ProblemDetailAuthenticationEntryPoint entryPoint,
            ProblemDetailAccessDeniedHandler accessDeniedHandler
    ) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                
                // 1. Endpoint Authorization Rules
                .authorizeHttpRequests(authorization ->
                        authorization
                                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                                .requestMatchers("/user", "/user/**").hasRole("USER")
                                .requestMatchers("/actuator", "/actuator/**").permitAll()
                                .anyRequest().authenticated())


                // 2. Exception Handling for Filter-level 401 & 403
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 3. JWT Resource Server with Keycloak Converter and Starter Error Handlers
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }
}
