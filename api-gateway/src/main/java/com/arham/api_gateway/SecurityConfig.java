package com.arham.api_gateway;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String JWK_SET_URI =
            "http://localhost:8180/realms/shopflow/protocol/openid-connect/certs";


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {

        httpSecurity.authorizeHttpRequests(authorization ->
                        authorization
                                .anyRequest().authenticated())

                .oauth2ResourceServer(
                        oauth ->
                                oauth.jwt(
                                        Customizer.withDefaults()
                                )
                );

        return httpSecurity.build();

    }


    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
    }

}
