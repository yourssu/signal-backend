package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.domain.viewer.implement.exception.TicketPackageNotFoundException

class TicketPackages(
    private val packageMap: Map<String, TicketPackage>
) {
    companion object {
        const val PACKAGE_DELIMITER = "."

        fun of(registeredTicketPackages: String, unregisteredTicketPackages: String): TicketPackages {
            return of(registeredTicketPackages.split(PACKAGE_DELIMITER), unregisteredTicketPackages.split(PACKAGE_DELIMITER))
        }

        private fun of(registeredTicketPackages: List<String>, unregisteredTicketPackages: List<String>): TicketPackages {
            val packageMap = mutableMapOf<String, TicketPackage>()
            for (i in 0 until unregisteredTicketPackages.size) {
                val ticketPackage = TicketPackage.from(registeredTicketPackages[i], unregisteredTicketPackages[i])
                packageMap.put(ticketPackage.id, ticketPackage)
            }
            return TicketPackages(packageMap)
        }
    }

    fun findByPrice(price: Int, isFirst: Boolean): Int {
        val priceToQuantityRegisteredMap = packageMap.values.associate { it.registeredTicketPrice.price to it.registeredTicketPrice.quantity }
        val priceToQuantityUnregisteredMap = packageMap.values.associate { it.unregisteredTicketPrice.price to it.unregisteredTicketPrice.quantity }
        if (isFirst) {
            return priceToQuantityRegisteredMap[price]
                ?: priceToQuantityUnregisteredMap[price]
                ?: NO_MATCH_TICKET_AMOUNT
        }
        return priceToQuantityUnregisteredMap[price] ?: NO_MATCH_TICKET_AMOUNT
    }

    fun getPackageList(): List<TicketPackage> {
        return packageMap.values.toList()
    }

    fun getByPackageId(packageId: String): TicketPackage {
        return packageMap[packageId] ?: throw TicketPackageNotFoundException()
    }
}
