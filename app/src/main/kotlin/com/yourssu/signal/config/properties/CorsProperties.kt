package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.cors")
data class CorsProperties(
    var allowedOrigin: String? = null,
) {
}
