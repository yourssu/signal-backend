package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "infra.openai")
data class OpenAIConfigurationProperties(
    val url: String,
    val apiKey: String,
    val model: String,
    val prompt: String,
    val userInput: Int,
) {
}
