package com.yourssu.ssugaeting.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "domain.permission.admin")
data class AdminConfigurationProperties(
    private val secretKey: String,
) {
    fun isValidSecretKey(secretKey: String): Boolean {
        return this.secretKey == secretKey
    }
}
