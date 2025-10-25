package com.thilina.WorkingTimeApplication.config;

import com.thilina.WorkingTimeApplication.config.jwt.CachedBodyHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Wrap request to read body multiple times
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        String requestBody = new String(cachedRequest.getInputStream().readAllBytes());
        System.out.println("➡️ Incoming Request: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("Body: " + requestBody);

        // Wrap response to capture body
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(cachedRequest, wrappedResponse);

        byte[] responseArray = wrappedResponse.getContentAsByteArray();
        String responseBody = new String(responseArray);
        System.out.println("⬅️ Outgoing Response: " + response.getStatus());
        System.out.println("Body: " + responseBody);

        wrappedResponse.copyBodyToResponse(); // important to send response to client
    }
}
