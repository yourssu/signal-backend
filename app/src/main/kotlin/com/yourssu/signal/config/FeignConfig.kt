package com.yourssu.signal.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["com.yourssu.signal.infrastructure.payment"])
class FeignConfig