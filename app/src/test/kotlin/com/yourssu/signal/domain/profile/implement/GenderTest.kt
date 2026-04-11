package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.GenderNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class GenderTest : DescribeSpec({
    describe("Gender Enum") {
        context("of 메서드를 호출할 때") {
            context("'female'을 전달하면") {
                // given
                val value = "female"

                // when
                val result = Gender.of(value)

                // then
                it("Gender.FEMALE을 반환한다") {
                    result shouldBe Gender.FEMALE
                }
            }

            context("'MALE'을 전달하면") {
                // given
                val value = "MALE"

                // when
                val result = Gender.of(value)

                // then
                it("Gender.MALE을 반환한다") {
                    result shouldBe Gender.MALE
                }
            }

            context("대소문자 구분 없이 'mAlE'을 전달하면") {
                // given
                val value = "mAlE"

                // when
                val result = Gender.of(value)

                // then
                it("Gender.MALE을 반환한다") {
                    result shouldBe Gender.MALE
                }
            }

            context("존재하지 않는 성별 값을 전달하면") {
                // given
                val invalidValue = "OTHER"

                // when & then
                it("GenderNotFoundException을 발생시킨다") {
                    shouldThrow<GenderNotFoundException> {
                        Gender.of(invalidValue)
                    }
                }
            }

            context("빈 문자열을 전달하면") {
                // given
                val emptyValue = ""

                // when & then
                it("GenderNotFoundException을 발생시킨다") {
                    shouldThrow<GenderNotFoundException> {
                        Gender.of(emptyValue)
                    }
                }
            }
        }

        context("opposite 메서드를 호출할 때") {
            context("MALE 상수는") {
                // given
                val male = Gender.MALE

                // when
                val result = male.opposite()

                // then
                it("Gender.FEMALE을 반환한다") {
                    result shouldBe Gender.FEMALE
                }
            }

            context("FEMALE 상수는") {
                // given
                val female = Gender.FEMALE

                // when
                val result = female.opposite()

                // then
                it("Gender.MALE을 반환한다") {
                    result shouldBe Gender.MALE
                }
            }
        }
    }
})

