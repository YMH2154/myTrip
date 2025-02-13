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

        registry.addResourceHandler("/itemImages/**")
                .addResourceLocations("file:///C:/myTrip/itemImages/");

        registry.addResourceHandler("/activityImages/**")
                .addResourceLocations("file:///C:/myTrip/activityImages");
        
        registry.addResourceHandler("/banner/**")
                .addResourceLocations("file:///C:/myTrip/banner/");
    }

}
