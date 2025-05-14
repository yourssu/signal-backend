package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "domain.policy")
data class PolicyConfigurationProperties(
    val contactPrice: Int,
    val ticketPricePolicy: String,
) {
}
