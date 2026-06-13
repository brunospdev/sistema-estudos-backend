package com.planejamais.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Value("${security.rate-limit.auth-max:10}")
    private int authMax;

    @Value("${security.rate-limit.api-max:120}")
    private int apiMax;

    @Value("${security.rate-limit.window-ms:60000}")
    private long windowMs;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        int max = path.startsWith("/api/auth/") ? authMax : apiMax;
        String key = resolveKey(request, path);

        Window window = windows.computeIfAbsent(key, k -> new Window());

        synchronized (window) {
            long now = Instant.now().toEpochMilli();
            if (now - window.startMs > windowMs) {
                window.startMs = now;
                window.count.set(0);
            }

            if (window.count.incrementAndGet() > max) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Muitas requisições. Tente novamente em instantes.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request, String path) {
        String ip = com.planejamais.util.RequestUtils.resolveClientIp(request);
        return path.startsWith("/api/auth/") ? "auth:" + ip : "api:" + ip;
    }

    private static class Window {
        private long startMs = Instant.now().toEpochMilli();
        private final AtomicInteger count = new AtomicInteger(0);
    }
}
