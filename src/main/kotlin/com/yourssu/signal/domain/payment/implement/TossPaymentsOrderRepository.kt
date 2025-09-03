package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.domain.TossPaymentsOrder
import com.yourssu.signal.domain.viewer.implement.domain.Viewer

interface TossPaymentsOrderRepository {
    fun save(order: TossPaymentsOrder): TossPaymentsOrder
    fun getByViewerUuidAndPaymentKey(viewer: Viewer, paymentKey: String): TossPaymentsOrder
    fun getByOrderId(orderId: String): TossPaymentsOrder
}