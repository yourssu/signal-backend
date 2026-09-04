package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.implement.exception.AnimalGenderMismatchException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class ProfileRequestValidationTest : DescribeSpec({
    val validator = Validation.buildDefaultValidatorFactory().validator

    describe("프로필 생성 요청") {
        context("공식 구조가 아닌 전화번호이면") {
            it("contact 위반으로 거부한다") {
                val request = ProfileCreatedRequest(
                    gender = "MALE",
                    department = "컴퓨터학부",
                    birthYear = 2000,
                    animal = "DOG",
                    contact = "0101234567",
                    mbti = "ENFP",
                    nickname = "테스트",
                    introSentences = emptyList(),
                )

                validator.validate(request).map { it.propertyPath.toString() } shouldContain "contact"
            }
        }

        context("성별과 동물상 조합이 맞지 않으면") {
            it("도메인 변환 전에 거부한다") {
                val request = ProfileCreatedRequest(
                    gender = "MALE",
                    department = "컴퓨터학부",
                    birthYear = 2000,
                    animal = "FOX",
                    contact = "@signal_user",
                    mbti = "ENFP",
                    nickname = "테스트",
                    introSentences = emptyList(),
                )

                shouldThrow<AnimalGenderMismatchException> { request.toCommand("uuid").toDomain() }
            }
        }
    }

    describe("프로필 수정 요청") {
        context("공식 구조가 아닌 전화번호이면") {
            it("contact 위반으로 거부한다") {
                val request = ProfileUpdateRequest(
                    nickname = "테스트",
                    introSentences = emptyList(),
                    contact = "01112345678",
                )

                validator.validate(request).map { it.propertyPath.toString() } shouldContain "contact"
            }
        }

        context("기존 Instagram 형식이면") {
            it("contact 위반이 없다") {
                val request = ProfileUpdateRequest(
                    nickname = "테스트",
                    introSentences = emptyList(),
                    contact = "@signal.user_01",
                )

                validator.validate(request).map { it.propertyPath.toString() }.contains("contact") shouldBe false
            }
        }
    }
})
