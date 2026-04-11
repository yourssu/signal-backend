package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "infra.kakaopay")
data class KakaoPayConfigurationProperties(
    val adminKey: String,
    val cid: String,
    val approvalUrl: String,
    val cancelUrl: String,
    val failUrl: String,
)
