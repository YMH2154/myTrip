package com.soloProject.myTrip.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/itemDetailImages/**")
                        .addResourceLocations("file:///C:/myTrip/itemDetailImages/");

        registry.addResourceHandler("/thumbnailImages/**")
                .addResourceLocations("file:///C:/myTrip/thumbnailImages/");

        registry.addResourceHandler("/activityImages/**")
                .addResourceLocations("file:///C:/myTrip/activityImages");

    }

}
