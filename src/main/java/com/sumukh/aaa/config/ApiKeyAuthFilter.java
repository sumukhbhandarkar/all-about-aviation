package com.sumukh.aaa.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private final String adminKey;

  public ApiKeyAuthFilter(String adminKey) {
    this.adminKey = adminKey;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    // Allow all safe GETs (your public lookups) and static files
    boolean isRead = HttpMethod.GET.matches(request.getMethod());
    boolean isActuatorHealth = request.getRequestURI().startsWith("/actuator/health");
    boolean isStatic = request.getRequestURI().startsWith("/admin/") || request.getRequestURI().startsWith("/assets/");

    if (isRead || isActuatorHealth || isStatic) {
      chain.doFilter(request, response);
      return;
    }

    // Require API key for anything else (POST/PUT/PATCH/DELETE)
    String key = request.getHeader("X-Admin-Key");
    if (StringUtils.hasText(key) && key.equals(adminKey)) {
      chain.doFilter(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid X-Admin-Key\"}");
    }
  }
}
