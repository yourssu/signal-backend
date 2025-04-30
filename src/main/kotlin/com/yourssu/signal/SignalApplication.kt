package com.yourssu.signal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableCaching
@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
class SignalApplication

fun main(args: Array<String>) {
    runApplication<SignalApplication>(*args)
}
