package com.foodshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 *
 * <p>Configures static resource handlers and other web-related settings.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/css/**", "/js/**", "/images/**")
        .addResourceLocations("classpath:/static/css/", "classpath:/static/js/",
            "classpath:/static/images/");
    
    // Configure favicon handler to prevent NoResourceFoundException
    registry
        .addResourceHandler("/favicon.ico")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true);
  }
}
