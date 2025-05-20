package com.yourssu.signal.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

const val CONTACT_UNLIMITED = 0

@ConfigurationProperties(prefix = "domain.policy")
data class PolicyConfigurationProperties(
    val contactLimit: Int = CONTACT_UNLIMITED,
    val contactLimitWarning: Int = CONTACT_UNLIMITED,
    val contactPrice: Int,
    val ticketPricePolicy: String,
    val ticketPriceRegisteredPolicy: String,
) {
}
