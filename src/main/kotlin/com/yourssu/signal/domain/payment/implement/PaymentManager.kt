package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.payment.implement.domain.KakaoPayOrder
import com.yourssu.signal.domain.payment.implement.domain.TossPaymentsOrder
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.TossPaymentsApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.OrderApprovalRequest
import com.yourssu.signal.domain.payment.implement.dto.OrderReadyRequest
import com.yourssu.signal.domain.payment.implement.dto.PaymentInitiation
import com.yourssu.signal.domain.viewer.implement.TicketPricePolicy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

enum class PaymentProvider {
    KAKAOPAY, TOSSPAYMENTS
}

@Component
class PaymentManager(
    private val kakaoPayOutputPort: KakaoPayOutputPort,
    private val kakaoPayOrderWriter: KakaoPayOrderWriter,
    private val tossPaymentsOutputPort: TossPaymentsOutputPort,
    private val tossPaymentsOrderWriter: TossPaymentsOrderWriter,
    private val ticketPricePolicy: TicketPricePolicy
) {
    @Transactional
    fun initiateKakaoPay(
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
    fun approveKakaoPay(
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
            pgToken = pgToken,
            price = kakaoPayOrder.amount
        )
        val response = kakaoPayOutputPort.approve(request)
        kakaoPayOrderWriter.approve(kakaoPayOrder, response.aid, response.approvedTime)
        return response
    }

    @Transactional
    fun initiateTossPayments(
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
            orderId = TossPaymentsOrder.generateOrderId(uuid.value),
            itemName = TossPaymentsOrder.generateItemName(ticketPrice.quantity, ticketPrice.price),
            quantity = ticketPrice.quantity,
            price = ticketPrice.price
        )
        val response = tossPaymentsOutputPort.ready(request)
        val order = tossPaymentsOrderWriter.create(response.toOrder(request))
        return response.toPaymentInitiation(order.orderId)
    }

    @Transactional
    fun approveTossPayments(
        tossPaymentsOrder: TossPaymentsOrder,
        paymentKey: String
    ): TossPaymentsApprovalResponse {
        tossPaymentsOrder.validateReady()
        ticketPricePolicy.validateTicketQuantity(
            requestQuantity = tossPaymentsOrder.quantity,
            price = tossPaymentsOrder.amount,
            uuid = tossPaymentsOrder.uuid,
        )
        val request = OrderApprovalRequest(
            uuid = tossPaymentsOrder.uuid.value,
            orderId = tossPaymentsOrder.orderId,
            tid = paymentKey,
            pgToken = "",
            price = tossPaymentsOrder.amount
        )
        val response = tossPaymentsOutputPort.approve(request)
        tossPaymentsOrderWriter.approve(tossPaymentsOrder, response.approvedAt)
        return response
    }
}
