package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.EgenTetoNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EgenTetoTest : DescribeSpec({
    describe("EgenTeto.of") {
        context("null을 입력하면") {
            it("null을 반환한다") {
                EgenTeto.of(null) shouldBe null
            }
        }

        context("유효한 값을 입력하면") {
            it("EGEN을 반환한다") {
                EgenTeto.of("EGEN") shouldBe EgenTeto.EGEN
            }
            it("TETO를 반환한다") {
                EgenTeto.of("TETO") shouldBe EgenTeto.TETO
            }
            it("NOT_SELECTED를 반환한다") {
                EgenTeto.of("NOT_SELECTED") shouldBe EgenTeto.NOT_SELECTED
            }
        }

        context("잘못된 값을 입력하면") {
            it("EgenTetoNotFoundException을 던진다") {
                shouldThrow<EgenTetoNotFoundException> {
                    EgenTeto.of("INVALID")
                }
            }
        }
    }
})
