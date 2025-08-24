package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.domain.KakaoPayOrder
import com.yourssu.signal.domain.viewer.implement.domain.Viewer
import org.springframework.stereotype.Component

@Component
class KakaoPayOrderReader(
    val kakaoPayOrderRepository: KakaoPayOrderRepository
) {
    fun getByViewerAndTid(viewer: Viewer, tid: String): KakaoPayOrder {
        return kakaoPayOrderRepository.getByViewerUuidAndTid(viewer, tid)
    }
}
