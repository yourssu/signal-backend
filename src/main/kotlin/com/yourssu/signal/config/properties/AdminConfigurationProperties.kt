package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "domain.permission.admin")
data class AdminConfigurationProperties(
    private val accessKey: String,
    val contactSecretKey: String,
) {
    fun isValidAccessKey(accessKey: String): Boolean {
        return this.accessKey == accessKey
    }
}
