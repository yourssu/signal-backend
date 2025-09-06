package com.yourssu.signal.domain.viewer.business.dto

import com.yourssu.signal.domain.viewer.implement.TicketPackage
import com.yourssu.signal.domain.viewer.implement.TicketPackages

data class TicketPackagesResponses(
    val packages: List<TicketPackageResponse>,
) {
    companion object {
        fun from(ticketPackages: TicketPackages) =
            TicketPackagesResponses(
                packages = ticketPackages.getPackageList().map { TicketPackageResponse.from(it) },
            )
    }
}

data class TicketPackageResponse(
    val id: String,
    val name: String,
    val quantity: List<Int>,
    val price: List<Int>,
) {
    companion object {
        fun from(ticketPackage: TicketPackage) =
            TicketPackageResponse(
                id = ticketPackage.id,
                name = ticketPackage.registeredTicketPrice.name(),
                quantity = listOf(
                    ticketPackage.registeredTicketPrice.quantity,
                    ticketPackage.unregisteredTicketPrice.quantity
                ),
                price = listOf(ticketPackage.registeredTicketPrice.price, ticketPackage.unregisteredTicketPrice.price),
            )
    }
}
