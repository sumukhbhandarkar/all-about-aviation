package com.sumukh.aaa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Value("${app.admin-key}")
  private String adminKey;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .cors(c-> {})
      .csrf(csrf -> csrf.disable()) // APIs; UI uses header key
      .headers(h -> h.frameOptions(f -> f.sameOrigin()))
      .authorizeHttpRequests(reg -> reg
        .requestMatchers("/actuator/health").permitAll()
        .requestMatchers("/admin/**", "/assets/**").permitAll()    // serve admin UI
        .requestMatchers("/", "/{code}", "/api/**").permitAll()     // GETs allowed; filter protects writes
        .anyRequest().permitAll()
      )
      .addFilterBefore(new ApiKeyAuthFilter(adminKey), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
