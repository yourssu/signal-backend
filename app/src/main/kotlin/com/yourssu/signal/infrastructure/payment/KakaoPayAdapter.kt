package com.yourssu.signal.infrastructure.payment

import com.yourssu.signal.config.properties.KakaoPayConfigurationProperties
import com.yourssu.signal.domain.payment.implement.KakaoPayOutputPort
import com.yourssu.signal.domain.payment.implement.dto.OrderApprovalRequest
import com.yourssu.signal.domain.payment.implement.dto.OrderReadyRequest
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayReadyResponse
import com.yourssu.signal.infrastructure.payment.dto.KakaoPayApprovalRequest
import com.yourssu.signal.infrastructure.payment.dto.KakaoPayReadyRequest
import com.yourssu.signal.infrastructure.payment.exception.KakaoPayException
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(KakaoPayConfigurationProperties::class)
class KakaoPayAdapter(
    private val kakaoPayProperties: KakaoPayConfigurationProperties,
    private val kakaoPayFeignClient: KakaoPayFeignClient
) : KakaoPayOutputPort {
    override fun ready(request: OrderReadyRequest): KakaoPayReadyResponse {
        val approvalUrl = "${kakaoPayProperties.approvalUrl}?order_id=${request.orderId}"
        val kakaoPayRequest = KakaoPayReadyRequest(
            cid = kakaoPayProperties.cid,
            partnerOrderId = request.orderId,
            partnerUserId = request.uuid,
            itemName = request.itemName,
            quantity = request.quantity,
            totalAmount = request.price,
            taxFreeAmount = 0,
            approvalUrl = approvalUrl,
            cancelUrl = kakaoPayProperties.cancelUrl,
            failUrl = kakaoPayProperties.failUrl,
        )
        try {
            val response = kakaoPayFeignClient.ready(kakaoPayRequest)
            return response.toPaymentReady()
        } catch (e: Exception) {
            throw KakaoPayException("카카오페이 결제 준비 실패: ${e.message}")
        }
    }

    override fun approve(request: OrderApprovalRequest): KakaoPayApprovalResponse {
        val kakaoPayRequest = KakaoPayApprovalRequest(
            cid = kakaoPayProperties.cid,
            tid = request.tid,
            partnerOrderId = request.orderId,
            partnerUserId = request.uuid,
            pgToken = request.pgToken
        )
        try {
            val response = kakaoPayFeignClient.approve(kakaoPayRequest)
            return response.toDomainResponse()
        } catch (e: Exception) {
            throw KakaoPayException("카카오페이 결제 승인 실패: ${e.message}")
        }
    }
}
