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
            viewerUuid = command.toUuid(),
            requestQuantity = command.quantity,
            price = command.price
        )
        return PaymentInitiationResponse.from(result)
    }

    @Transactional
    fun approve(command: PaymentApprovalCommand): PaymentCompletionResponse {
        val viewer = viewerReader.get(command.toUuid())
        val kakaoPayOrder = kakaoPayOrderReader.getByViewerAndTid(viewer = viewer, tid = command.tid)
        viewerWriter.issueTicket(viewer.uuid, kakaoPayOrder.quantity)
        val result = paymentManager.approve(
            kakaoPayOrder = kakaoPayOrder,
            pgToken = command.pgToken
        )
        return PaymentCompletionResponse.from(result)
    }
}
