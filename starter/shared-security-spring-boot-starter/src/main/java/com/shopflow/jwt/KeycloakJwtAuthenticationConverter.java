package com.shopflow.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(KeycloakJwtAuthenticationConverter.class);

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

        // 1. Extract default scopes (SCOPE_openid, etc.)
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        Set<GrantedAuthority> authorities = new HashSet<>(defaultConverter.convert(jwt));

        // 2. Extract Keycloak realm roles or client roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.containsKey("api-gateway")) {
            Map<String, Object> client = (Map<String, Object>) resourceAccess.get("api-gateway");
            List<String> roles = (List<String>) client.get("roles");
            if (roles != null) {
                roles.forEach(role -> {
                    String r = "ROLE_" + role.toUpperCase();
                    authorities.add(new SimpleGrantedAuthority(r));
                    log.info(r);
                });

            }
        }
        return authorities;
    }
}