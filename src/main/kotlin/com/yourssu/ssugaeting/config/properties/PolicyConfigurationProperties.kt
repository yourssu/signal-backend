package com.yourssu.ssugaeting.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "domain.policy")
data class PolicyConfigurationProperties(
    val contactPrice: Int,
) {
}
