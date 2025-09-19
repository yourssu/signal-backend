package com.yourssu.signal.infrastructure.openrouter.exception

class FailedOpenRouterModelException(
    override val message: String = "Failed to get response from OpenRouter API"
) : RuntimeException(message)