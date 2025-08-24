package com.yourssu.signal.config

import com.yourssu.signal.config.properties.KakaoPayConfigurationProperties
import com.yourssu.signal.infrastructure.payment.decoder.KakaoPayErrorDecoder
import feign.RequestInterceptor
import feign.codec.ErrorDecoder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KakaoPayConfigurationProperties::class)
class KakaoPayFeignConfiguration(
    private val kakaoPayProperties: KakaoPayConfigurationProperties
) {
    @Bean
    fun kakaoPayRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("Authorization", "SECRET_KEY ${kakaoPayProperties.adminKey}")
            requestTemplate.header("Content-Type", "application/json")
        }
    }
    
    @Bean
    fun kakaoPayErrorDecoder(): ErrorDecoder {
        return KakaoPayErrorDecoder()
    }
}
