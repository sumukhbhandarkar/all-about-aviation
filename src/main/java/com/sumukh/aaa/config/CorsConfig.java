package com.sumukh.aaa.config;// e.g., in a @Configuration class
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry reg) {
    reg.addMapping("/api/**")
      .allowedOrigins("http://localhost:8080")   // add your admin origin(s)
      .allowedMethods("GET","POST","PUT","PATCH","DELETE")
      .allowedHeaders("*")
      .exposedHeaders("*");
  }
}
