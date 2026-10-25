package com.shopflow.error;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class ProblemDetailAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ProblemDetailWriter writer;

    public ProblemDetailAuthenticationEntryPoint(
            ProblemDetailWriter writer) {
        this.writer = writer;
    }

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception)
            throws IOException  {

        writer.write(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Authentication is required."
        );
    }
}
