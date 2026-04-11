package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.dto.OrderApprovalRequest
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.OrderReadyRequest
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayReadyResponse

interface KakaoPayOutputPort {
    fun ready(request: OrderReadyRequest): KakaoPayReadyResponse
    
    fun approve(request: OrderApprovalRequest): KakaoPayApprovalResponse
}
