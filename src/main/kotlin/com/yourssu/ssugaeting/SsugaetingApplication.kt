package com.yourssu.ssugaeting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class SsugaetingApplication

fun main(args: Array<String>) {
    runApplication<SsugaetingApplication>(*args)
}
