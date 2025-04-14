package com.yourssu.ssugaeting.domain.common.implement

import java.util.*

class Uuid(
    val value: String,
) {
    companion object {
        fun randomUUID(): Uuid {
            return Uuid(UUID.randomUUID().toString())
        }
    }
}
