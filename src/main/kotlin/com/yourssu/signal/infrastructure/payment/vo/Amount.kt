package com.yourssu.signal.infrastructure.payment.vo

import com.fasterxml.jackson.annotation.JsonProperty

data class Amount(
    val total: Int,
    @JsonProperty("tax_free")
    val taxFree: Int,
    val vat: Int,
    val point: Int,
    val discount: Int
)