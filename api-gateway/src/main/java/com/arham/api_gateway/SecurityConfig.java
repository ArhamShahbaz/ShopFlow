package com.arham.api_gateway;



import com.shopflow.jwt.KeycloakJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    private final KeycloakJwtAuthenticationConverter keycloakJwtConverter;

    private final JwtDecoder jwtDecoder;

    public SecurityConfig(KeycloakJwtAuthenticationConverter keycloakJwtConverter, JwtDecoder jwtDecoder) {
        this.keycloakJwtConverter = keycloakJwtConverter;
        this.jwtDecoder = jwtDecoder;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {

        httpSecurity.authorizeHttpRequests(authorization ->
                        authorization
                                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                                .requestMatchers("/user", "/user/**").hasRole("USER")
                                .anyRequest().authenticated())

                .oauth2ResourceServer(
                        oauth ->
                                oauth.jwt(
                                        jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter)
                                )
                );

        return httpSecurity.build();

    }




}
