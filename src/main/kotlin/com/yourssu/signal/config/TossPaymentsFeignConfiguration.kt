package com.yourssu.signal.config

import com.yourssu.signal.config.properties.TossPaymentsConfigurationProperties
import com.yourssu.signal.infrastructure.payment.decoder.TossPaymentsErrorDecoder
import feign.RequestInterceptor
import feign.codec.ErrorDecoder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Base64

@Configuration
@EnableConfigurationProperties(TossPaymentsConfigurationProperties::class)
class TossPaymentsFeignConfiguration(
    private val tossPaymentsProperties: TossPaymentsConfigurationProperties
) {
    @Bean
    fun tossPaymentsRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            val credentials = "${tossPaymentsProperties.secretKey}:"
            val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())
            requestTemplate.header("Authorization", "Basic $encodedCredentials")
            requestTemplate.header("Content-Type", "application/json")
        }
    }
    
    @Bean
    fun tossPaymentsErrorDecoder(): ErrorDecoder {
        return TossPaymentsErrorDecoder()
    }
}