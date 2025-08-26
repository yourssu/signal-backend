package com.yourssu.signal.domain.payment.business

import com.yourssu.signal.domain.payment.business.command.PaymentApprovalCommand
import com.yourssu.signal.domain.payment.business.command.PaymentInitiationCommand
import com.yourssu.signal.domain.payment.business.dto.PaymentCompletionResponse
import com.yourssu.signal.domain.payment.business.dto.PaymentInitiationResponse
import com.yourssu.signal.domain.payment.implement.KakaoPayOrderReader
import com.yourssu.signal.domain.payment.implement.PaymentManager
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import com.yourssu.signal.domain.viewer.implement.ViewerWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentManager: PaymentManager,
    private val viewerReader: ViewerReader,
    private val viewerWriter: ViewerWriter,
    private val kakaoPayOrderReader: KakaoPayOrderReader,
) {
    fun initiate(command: PaymentInitiationCommand): PaymentInitiationResponse {
        val result = paymentManager.initiate(
            uuid = command.toUuid(),
            requestQuantity = command.quantity,
            price = command.price
        )
        return PaymentInitiationResponse.from(result)
    }

    @Transactional
    fun approve(command: PaymentApprovalCommand): PaymentCompletionResponse {
        val kakaoPayOrder = kakaoPayOrderReader.getByOrderId(orderId = command.orderId)
        kakaoPayOrder.validateOwner(command.toUuid())
        val result = paymentManager.approve(
            kakaoPayOrder = kakaoPayOrder,
            pgToken = command.pgToken
        )
        // TODO: 결제 실패하는 경우 롤백 처리 필요, 현재는 결제 승인 후 티켓 발급에서 실패하는 경우가 없음
        viewerWriter.issueTicket(kakaoPayOrder.uuid, kakaoPayOrder.quantity)
        return PaymentCompletionResponse.from(result)
    }
}
