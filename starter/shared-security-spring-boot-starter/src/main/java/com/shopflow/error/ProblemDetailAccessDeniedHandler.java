package com.shopflow.error;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailWriter writer;

    public ProblemDetailAccessDeniedHandler(
            ProblemDetailWriter writer) {
        this.writer = writer;
    }

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException exception)
            throws IOException {

        writer.write(
                response,
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to access this resource."
        );
    }
}
