package com.arham.api_gateway;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {


    private static final String JWK_SET_URI =
            "http://localhost:8180/realms/shopflow/protocol/openid-connect/certs";


    @Value("${spring.security.oauth2.client.provider.keyclock.issuer-uri}")
    private  String issuerUri;






    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
    }




}
