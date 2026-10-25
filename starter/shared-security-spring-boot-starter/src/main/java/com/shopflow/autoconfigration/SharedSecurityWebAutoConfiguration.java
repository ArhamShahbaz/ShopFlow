package com.shopflow.autoconfigration;

import com.shopflow.jwt.KeycloakJwtAuthenticationConverter;
import com.shopflow.error.ProblemDetailAccessDeniedHandler;
import com.shopflow.error.ProblemDetailAuthenticationEntryPoint;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
public class SharedSecurityWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain sharedSecurityFilterChain(
            HttpSecurity http,
            KeycloakJwtAuthenticationConverter jwtConverter,
            ProblemDetailAuthenticationEntryPoint entryPoint,
            ProblemDetailAccessDeniedHandler deniedHandler)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtConverter)
                )
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler)
            );

        return http.build();
    }
}
