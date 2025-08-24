package com.yourssu.signal.infrastructure.payment.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoPayReadyRequest(
    val cid: String,
    @JsonProperty("partner_order_id")
    val partnerOrderId: String,
    @JsonProperty("partner_user_id")
    val partnerUserId: String,
    @JsonProperty("item_name")
    val itemName: String,
    val quantity: Int,
    @JsonProperty("total_amount")
    val totalAmount: Int,
    @JsonProperty("tax_free_amount")
    val taxFreeAmount: Int = 0,
    @JsonProperty("approval_url")
    val approvalUrl: String,
    @JsonProperty("cancel_url")
    val cancelUrl: String,
    @JsonProperty("fail_url")
    val failUrl: String
)

data class KakaoPayApprovalRequest(
    val cid: String,
    val tid: String,
    @JsonProperty("partner_order_id")
    val partnerOrderId: String,
    @JsonProperty("partner_user_id")
    val partnerUserId: String,
    @JsonProperty("pg_token")
    val pgToken: String
)