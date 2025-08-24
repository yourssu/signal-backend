package com.yourssu.signal.infrastructure.payment.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayApprovalResponse
import com.yourssu.signal.infrastructure.payment.vo.Amount
import com.yourssu.signal.infrastructure.payment.vo.CardInfo
import java.time.LocalDateTime

data class KakaoPayApprovalResponse(
    val aid: String,
    val tid: String,
    val cid: String,
    @JsonProperty("partner_order_id")
    val partnerOrderId: String,
    @JsonProperty("partner_user_id")
    val partnerUserId: String,
    @JsonProperty("payment_method_type")
    val paymentMethodType: String,
    val amount: Amount,
    @JsonProperty("card_info")
    val cardInfo: CardInfo?,
    @JsonProperty("item_name")
    val itemName: String,
    val quantity: Int,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime,
    @JsonProperty("approved_at")
    val approvedAt: LocalDateTime
) {
    fun toDomainResponse(): KakaoPayApprovalResponse {
        return KakaoPayApprovalResponse(
            aid = aid,
            tid = tid,
            orderId = partnerOrderId,
            viewerUuid = partnerUserId,
            itemName = itemName,
            totalAmount = amount.total,
            approvedTime = approvedAt
        )
    }
}
