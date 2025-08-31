package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.verification.implement.domain.VerificationCode
import com.yourssu.signal.domain.viewer.implement.domain.TicketPackages
import com.yourssu.signal.domain.viewer.implement.domain.TicketPrice
import com.yourssu.signal.domain.viewer.implement.exception.InvalidTicketQuantityException
import org.springframework.stereotype.Component

const val NO_MATCH_TICKET_AMOUNT = 0

@Component
class TicketPricePolicy(
    private val configProperties: PolicyConfigurationProperties,
    private val profileReader: ProfileReader,
    private val viewerReader: ViewerReader,
    private val verificationReader: VerificationReader,
) {
    private val ticketPackages: TicketPackages = initializeTicketPackages()

    private fun initializeTicketPackages(): TicketPackages {
        val registeredTicketPackages = configProperties.ticketPriceRegisteredPolicy
        val unregisteredTicketPackages = configProperties.ticketPricePolicy
        return TicketPackages.of(registeredTicketPackages, unregisteredTicketPackages)
    }

    fun calculateTicketQuantity(price: Int, code: VerificationCode): Int {
        val uuid = verificationReader.findByCode(code).uuid
        val quantity = calculateTicketQuantity(price, uuid)
        return quantity
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
        return ticketPackages.findByPrice(price, isFirstPurchasedTicket(uuid))
    }

    private fun isFirstPurchasedTicket(uuid: Uuid): Boolean {
        return profileReader.existsByUuid(uuid) && !viewerReader.existsByUuid(uuid)
    }

    fun getTicketPackages(): TicketPackages {
        return ticketPackages
    }

    fun matchTicketPriceByPackageId(packageId: String, uuid: Uuid): TicketPrice {
        val ticketPackage = ticketPackages.getByPackageId(packageId)
        if (isFirstPurchasedTicket(uuid)) {
            return ticketPackage.registeredTicketPrice
        }
        return ticketPackage.unregisteredTicketPrice
    }
}
