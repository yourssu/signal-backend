package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import org.springframework.stereotype.Component

@Component
class TicketPricePolicy(
    private val configProperties: PolicyConfigurationProperties,
) {
    companion object {
        private const val PRICE_DELIMITER = "&"
        private const val KEY_VALUE_DELIMITER = "="
    }

    private val priceToTicketMap: Map<Int, Int> = initializePriceToTicketMap()

    private fun initializePriceToTicketMap(): Map<Int, Int> {
        return configProperties.ticketPricePolicy
            .split(PRICE_DELIMITER)
            .map { it.split(KEY_VALUE_DELIMITER) }
            .associate { it[0].toInt() to it[1].toInt() }
    }

    fun calculateTicketQuantity(price: Int): Int {
        return priceToTicketMap[price] ?: 0
    }
}
