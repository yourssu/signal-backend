package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "infra.tosspayments")
data class TossPaymentsConfigurationProperties(
    val secretKey: String,
    val clientKey: String,
    val successUrl: String,
    val failUrl: String,
)