package com.shopflow.autoconfigration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(name = "feign.RequestInterceptor")
public class FeignTokenRelayInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignTokenRelayInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            template.header("Authorization", "Bearer " + jwtAuth.getToken().getTokenValue());
            log.info("Authorization Token Added in Open Feign");
        }
    }
}