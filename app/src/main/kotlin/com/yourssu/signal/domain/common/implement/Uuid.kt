package com.yourssu.signal.domain.common.implement

import java.util.*

data class Uuid(
    val value: String,
) {
    companion object {
        fun randomUUID(): Uuid {
            return Uuid(UUID.randomUUID().toString())
        }
    }
}
