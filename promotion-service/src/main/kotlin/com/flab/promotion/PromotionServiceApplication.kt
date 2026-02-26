package com.flab.promotion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class PromotionServiceApplication

fun main(args: Array<String>) {
    runApplication<PromotionServiceApplication>(*args)
}
