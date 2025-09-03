
import com.yourssu.signal.config.properties.TossPaymentsConfigurationProperties
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.payment.implement.TossPaymentsOutputPort
import com.yourssu.signal.domain.payment.implement.domain.TossPaymentsOrder
import com.yourssu.signal.domain.payment.implement.dto.OrderApprovalRequest
import com.yourssu.signal.domain.payment.implement.dto.OrderReadyRequest
import com.yourssu.signal.domain.payment.implement.dto.TossPaymentsApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.TossPaymentsReadyResponse
import com.yourssu.signal.infrastructure.payment.dto.TossPaymentsApprovalRequest
import com.yourssu.signal.infrastructure.payment.exception.TossPaymentsException
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

@Component
@EnableConfigurationProperties(TossPaymentsConfigurationProperties::class)
class TossPaymentsAdapter(
    private val tossPaymentsProperties: TossPaymentsConfigurationProperties,
    private val tossPaymentsFeignClient: TossPaymentsFeignClient
) : TossPaymentsOutputPort {
    
    override fun ready(request: OrderReadyRequest): TossPaymentsReadyResponse {
        val paymentKey = UUID.randomUUID().toString()
        val checkoutUrl = "https://tosspayments.com/checkout"
        
        return TossPaymentsReadyResponse(
            paymentKey = paymentKey,
            checkoutUrl = checkoutUrl
        )
    }

    override fun approve(request: OrderApprovalRequest): TossPaymentsApprovalResponse {
        val tossPaymentsRequest = TossPaymentsApprovalRequest(
            paymentKey = request.tid,
            orderId = request.orderId,
            amount = request.price
        )
        try {
            val response = tossPaymentsFeignClient.approve(tossPaymentsRequest)
            return response.toDomainResponse(request.uuid)
        } catch (e: Exception) {
            throw TossPaymentsException("토스페이먼츠 결제 승인 실패: ${e.message}")
        }
    }
}
