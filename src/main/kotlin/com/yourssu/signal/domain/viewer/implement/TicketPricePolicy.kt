package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.viewer.implement.exception.InvalidTicketQuantityException
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.verification.implement.domain.VerificationCode
import com.yourssu.signal.domain.viewer.implement.exception.TicketIssuedFailedException
import com.yourssu.signal.infrastructure.Notification
import com.yourssu.signal.infrastructure.deposit.SMSMessage
import org.springframework.stereotype.Component

const val NO_MATCH_TICKET_AMOUNT = 0

@Component
class TicketPricePolicy(
    private val configProperties: PolicyConfigurationProperties,
    private val profileReader: ProfileReader,
    private val viewerReader: ViewerReader,
    private val verificationReader: VerificationReader,
) {
    companion object {
        private const val PRICE_DELIMITER = "."
        private const val KEY_VALUE_DELIMITER = "n"

        private const val PRICE_INDEX = 0
        private const val QUANTITY_INDEX = 1
    }

    private val priceToTicketMap: Map<Int, Int> = initializeMap()
    private val priceToTicketRegisteredMap: Map<Int, Int> = initializeRegisteredMap()

    private fun initializeMap(): Map<Int, Int> {
        return configProperties.ticketPricePolicy
            .split(PRICE_DELIMITER)
            .map { it.split(KEY_VALUE_DELIMITER) }
            .associate { it[PRICE_INDEX].toInt() to it[QUANTITY_INDEX].toInt() }
    }

    private fun initializeRegisteredMap(): Map<Int, Int> {
        return configProperties.ticketPriceRegisteredPolicy
            .split(PRICE_DELIMITER)
            .map { it.split(KEY_VALUE_DELIMITER) }
            .associate { it[PRICE_INDEX].toInt() to it[QUANTITY_INDEX].toInt() }
    }

    fun calculateTicketQuantity(price: Int, code: VerificationCode): Int {
        val uuid = verificationReader.findByCode(code).uuid
        val quantity = calculateTicketQuantity(price, uuid)
        notifyWhenRegisteredTicketQuantity(price, code)
        return quantity
    }

    private fun notifyWhenRegisteredTicketQuantity(
        price: Int,
        code: VerificationCode
    ) {
        if (priceToTicketRegisteredMap[price] != null) {
            Notification.notifyNoFirstPurchasedTicket(price, code)
        }
    }

    fun validateTicketQuantity(
        requestQuantity: Int,
        price: Int,
        uuid: Uuid
    ) {
        val quantity = calculateTicketQuantity(price, uuid)
        if (quantity == NO_MATCH_TICKET_AMOUNT || requestQuantity != quantity) {
            throw InvalidTicketQuantityException(
                requestedQuantity = requestQuantity,
                expectedQuantity = quantity,
                price = price
            )
        }
    }

    private fun calculateTicketQuantity(price: Int, uuid: Uuid): Int {
        if (isFirstPurchasedTicket(uuid)) {
            return priceToTicketRegisteredMap[price]
                ?: priceToTicketMap[price]
                ?: NO_MATCH_TICKET_AMOUNT
        }
        return priceToTicketMap[price] ?: NO_MATCH_TICKET_AMOUNT
    }

    private fun isFirstPurchasedTicket(uuid: Uuid): Boolean {
        return profileReader.existsByUuid(uuid) && !viewerReader.existsByUuid(uuid)
    }
}
