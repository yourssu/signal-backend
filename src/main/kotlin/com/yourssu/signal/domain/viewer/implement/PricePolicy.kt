package com.yourssu.signal.domain.viewer.implement

object PricePolicy {
    fun toTicket(price: Int): Int {
        return when (price) {
            1 -> 1
            3 -> 3
            5 -> 5
            else -> throw IllegalArgumentException("Invalid price: $price")
        }
    }
}
