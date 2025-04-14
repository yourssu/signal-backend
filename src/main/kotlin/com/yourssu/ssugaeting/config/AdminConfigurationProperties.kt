package com.yourssu.ssugaeting.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "permission.admin")
data class AdminConfigurationProperties(
    private val secretKey: String,
) {
    fun isValidSecretKey(secretKey: String): Boolean {
        return this.secretKey == secretKey
    }
}
