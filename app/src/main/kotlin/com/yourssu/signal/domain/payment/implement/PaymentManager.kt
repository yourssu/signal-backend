package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayApprovalResponse
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
        uuid: Uuid,
        packageId: String,
    ): PaymentInitiation {
        val ticketPrice = ticketPricePolicy.matchTicketPriceByPackageId(packageId, uuid)
        ticketPricePolicy.validateTicketQuantity(
            requestQuantity = ticketPrice.quantity,
            price = ticketPrice.price,
            uuid = uuid
        )
        val request = OrderReadyRequest(
            uuid = uuid.value,
            orderId = KakaoPayOrder.generateOrderId(uuid.value),
            itemName = KakaoPayOrder.generateItemName(ticketPrice.quantity, ticketPrice.price),
            quantity = ticketPrice.quantity,
            price = ticketPrice.price
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
            uuid = kakaoPayOrder.uuid,
        )
        val request = OrderApprovalRequest(
            uuid = kakaoPayOrder.uuid.value,
            orderId = kakaoPayOrder.orderId,
            tid = kakaoPayOrder.tid,
            pgToken = pgToken
        )
        val response = kakaoPayOutputPort.approve(request)
        kakaoPayOrderWriter.approve(kakaoPayOrder, response.aid, response.approvedTime)
        return response
    }
}
