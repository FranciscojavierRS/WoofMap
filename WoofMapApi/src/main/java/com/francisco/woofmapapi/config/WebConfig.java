package com.francisco.woofmapapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${woofmap.uploads.dir:uploads}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadsDir).toAbsolutePath().normalize().toUri().toString();
        // Spring necesita la barra final para tratar la ruta como un directorio.
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }
}
