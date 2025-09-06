package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.viewer.implement.Viewer

interface KakaoPayOrderRepository {
    fun save(order: KakaoPayOrder): KakaoPayOrder
    fun getByViewerUuidAndTid(viewer: Viewer, tid: String): KakaoPayOrder
    fun getByOrderId(orderId: String): KakaoPayOrder
}
