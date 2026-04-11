package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "infra.google.oauth")
data class GoogleOAuthConfigurationProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String
)
