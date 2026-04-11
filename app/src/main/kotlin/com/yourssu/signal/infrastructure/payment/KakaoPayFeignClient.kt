package com.yourssu.signal.infrastructure.payment

import com.yourssu.signal.config.KakaoPayFeignConfiguration
import com.yourssu.signal.infrastructure.payment.dto.KakaoPayApprovalRequest
import com.yourssu.signal.infrastructure.payment.dto.KakaoPayApprovalResponse
import com.yourssu.signal.infrastructure.payment.dto.KakaoPayReadyRequest
import com.yourssu.signal.infrastructure.payment.dto.KakaoPayReadyResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "kakaopay-client",
    url = "https://open-api.kakaopay.com",
    configuration = [KakaoPayFeignConfiguration::class]
)
@Component
interface KakaoPayFeignClient {
    @PostMapping("/online/v1/payment/ready")
    fun ready(@RequestBody request: KakaoPayReadyRequest): KakaoPayReadyResponse
    
    @PostMapping("/online/v1/payment/approve")
    fun approve(@RequestBody request: KakaoPayApprovalRequest): KakaoPayApprovalResponse
}
