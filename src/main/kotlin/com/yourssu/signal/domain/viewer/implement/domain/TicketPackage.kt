package com.yourssu.signal.domain.viewer.implement.domain

data class TicketPackage(
    val id: String,
    val registeredTicketPrice: TicketPrice,
    val unregisteredTicketPrice: TicketPrice,
) {
    companion object {
        private const val NAME_VALUE_DELIMITER = "@"

        fun from(registeredTicketPackage: String, unregisteredTicketPackage: String): TicketPackage {
            val registeredParts = registeredTicketPackage.split(NAME_VALUE_DELIMITER)
            val unregisteredParts = unregisteredTicketPackage.split(NAME_VALUE_DELIMITER)
            val id = registeredParts[0]
            return TicketPackage(
                id = id,
                registeredTicketPrice = TicketPrice.from(registeredParts[1]),
                unregisteredTicketPrice = TicketPrice.from(unregisteredParts[1])
            )
        }
    }
}

data class TicketPrice(
    val quantity: Int,
    val price: Int,
) {
    companion object {
        private const val PRICE_QUANTITY_DELIMITER = "n"

        private const val PRICE_INDEX = 0
        private const val QUANTITY_INDEX = 1

        fun from(priceToQuantity: String): TicketPrice {
            val parts = priceToQuantity.split(PRICE_QUANTITY_DELIMITER)
            val price = parts[PRICE_INDEX].toInt()
            val quantity = parts[QUANTITY_INDEX].toInt()
            return TicketPrice(quantity = quantity, price = price)
        }
    }

    fun name(): String = "${quantity}개"
}
