package com.shopflow.autoconfigration;



import com.shopflow.error.ProblemDetailAccessDeniedHandler;
import com.shopflow.error.ProblemDetailAuthenticationEntryPoint;
import com.shopflow.error.ProblemDetailWriter;
import com.shopflow.error.SharedRestExceptionHandler;
import com.shopflow.jwt.KeycloakJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@Import(SharedRestExceptionHandler.class)
public class SharedSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(KeycloakJwtAuthenticationConverter.class)
    KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean(ProblemDetailWriter.class)
    ProblemDetailWriter problemDetailWriter(ObjectMapper objectMapper) {
        return new ProblemDetailWriter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(ProblemDetailAuthenticationEntryPoint.class)
    ProblemDetailAuthenticationEntryPoint problemDetailAuthenticationEntryPoint(
            ProblemDetailWriter writer) {
        return new ProblemDetailAuthenticationEntryPoint(writer);
    }

    @Bean
    @ConditionalOnMissingBean(ProblemDetailAccessDeniedHandler.class)
    ProblemDetailAccessDeniedHandler problemDetailAccessDeniedHandler(
            ProblemDetailWriter writer) {
        return new ProblemDetailAccessDeniedHandler(writer);
    }
}
