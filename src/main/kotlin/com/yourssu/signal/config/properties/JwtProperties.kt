package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long = 1800000, // 30 minutes in milliseconds
    val refreshTokenExpiration: Long = 2592000000 // 30 days in milliseconds
)