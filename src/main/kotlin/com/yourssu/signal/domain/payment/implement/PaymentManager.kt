package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.payment.implement.domain.KakaoPayOrder
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayReadyResponse
import com.yourssu.signal.domain.payment.implement.dto.OrderApprovalRequest
import com.yourssu.signal.domain.payment.implement.dto.OrderReadyRequest
import com.yourssu.signal.domain.payment.implement.dto.PaymentInitiation
import com.yourssu.signal.domain.viewer.implement.TicketPricePolicy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentManager(
    private val kakaoPayOutputPort: KakaoPayOutputPort,
    private val kakaoPayOrderWriter: KakaoPayOrderWriter,
    private val ticketPricePolicy: TicketPricePolicy
) {
    @Transactional
    fun initiate(
        viewerUuid: Uuid,
        requestQuantity: Int,
        price: Int
    ): PaymentInitiation {
        ticketPricePolicy.validateTicketQuantity(
            requestQuantity = requestQuantity,
            price = price,
            uuid = viewerUuid
        )
        val request = OrderReadyRequest(
            viewerUuid = viewerUuid.value,
            orderId = KakaoPayOrder.generateOrderId(viewerUuid.value),
            itemName = KakaoPayOrder.generateItemName(requestQuantity, price),
            quantity = requestQuantity,
            price = price
        )
        val response = kakaoPayOutputPort.ready(request)
        val order = kakaoPayOrderWriter.create(response.toOrder(request))
        return response.toPaymentInitiation(order.orderId)
    }

    @Transactional
    fun approve(
        kakaoPayOrder: KakaoPayOrder,
        pgToken: String
    ): KakaoPayApprovalResponse {
        kakaoPayOrder.validateReady()
        ticketPricePolicy.validateTicketQuantity(
            requestQuantity = kakaoPayOrder.quantity,
            price = kakaoPayOrder.amount,
            uuid = kakaoPayOrder.viewerUuid,
        )
        val request = OrderApprovalRequest(
            viewerUuid = kakaoPayOrder.viewerUuid.value,
            orderId = kakaoPayOrder.orderId,
            tid = kakaoPayOrder.tid,
            pgToken = pgToken
        )
        val response = kakaoPayOutputPort.approve(request)
        kakaoPayOrderWriter.approve(kakaoPayOrder, response.aid, response.approvedTime)
        return response
    }
}
