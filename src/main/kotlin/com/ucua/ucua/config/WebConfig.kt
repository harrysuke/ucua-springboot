package com.ucua.ucua.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${cors.allowed-origins:*}") private val allowedOrigins: String,
    @Value("\${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") private val allowedMethods: String,
    @Value("\${cors.allowed-headers:Content-Type,Authorization,X-Requested-With}") private val allowedHeaders: String,
    @Value("\${storage.upload-dir:./uploads}") private val uploadDir: String
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(*allowedOrigins.split(",").toTypedArray())
            .allowedMethods(*allowedMethods.split(",").toTypedArray())
            .allowedHeaders(*allowedHeaders.split(",").toTypedArray())
            .maxAge(3600)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadDir/")
    }
}
