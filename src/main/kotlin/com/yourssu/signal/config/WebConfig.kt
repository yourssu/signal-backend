package com.yourssu.signal.config

import com.yourssu.signal.config.properties.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class WebConfig {
    @Bean
    fun webMvcConfigurer(corsProperties: CorsProperties): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins(*(corsProperties.allowedOrigins?.toTypedArray() ?: arrayOf("*")))
                    .allowedHeaders("*")
                    .allowedMethods(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name())
                    .allowCredentials(false)
            }
        }
    }
}
