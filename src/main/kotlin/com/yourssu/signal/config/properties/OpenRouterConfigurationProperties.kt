package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "infra.openrouter")
data class OpenRouterConfigurationProperties(
    val url: String,
    val apiKey: String,
    val model: String,
    val prompt: String,
    val userInput: Int,
) {
}
