package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.AnimalGenderMismatchException
import com.yourssu.signal.domain.profile.implement.exception.ContactFormatViolatedException
import com.yourssu.signal.domain.profile.implement.exception.DummyContactException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class ProfileValidationPolicyTest : DescribeSpec({
    describe("연락처 형식 검증") {
        context("010으로 시작하는 11자리이면") {
            it("국번과 무관하게 전화번호를 허용한다") {
                shouldNotThrowAny { ProfileValidator.validateContact("01020000000") }
                shouldNotThrowAny { ProfileValidator.validateContact("01019836282") }
                shouldNotThrowAny { ProfileValidator.validateContact("01011119999") }
            }
        }

        context("010으로 시작하지 않거나 자릿수가 다르면") {
            it("전화번호를 거부한다") {
                shouldThrow<ContactFormatViolatedException> { ProfileValidator.validateContact("01112345670") }
                shouldThrow<ContactFormatViolatedException> { ProfileValidator.validateContact("0101983628") }
            }
        }

        context("뒤 8자리가 모두 같은 숫자이면") {
            it("더미 연락처로 거부한다") {
                shouldThrow<DummyContactException> { ProfileValidator.validateContact("01000000000") }
                shouldThrow<DummyContactException> { ProfileValidator.validateContact("01011111111") }
                shouldThrow<DummyContactException> { ProfileValidator.validateContact("01099999999") }
            }
        }

        context("뒤 8자리가 연속 수열이면") {
            it("더미 연락처로 거부한다") {
                shouldThrow<DummyContactException> { ProfileValidator.validateContact("01012345678") }
                shouldThrow<DummyContactException> { ProfileValidator.validateContact("01098765432") }
            }
        }

        context("기존 Instagram 형식이면") {
            it("그대로 허용한다") {
                shouldNotThrowAny { ProfileValidator.validateContact("@signal.user_01") }
            }
        }
    }

    describe("성별별 동물상 검증") {
        context("남성 동물상 목록을 검증하면") {
            it("WOLF는 허용하고 HAMSTER는 거부한다") {
                shouldNotThrowAny { ProfileValidator.validateAnimal(Gender.MALE, Animal.WOLF) }
                shouldThrow<AnimalGenderMismatchException> {
                    ProfileValidator.validateAnimal(Gender.MALE, Animal.HAMSTER)
                }
            }
        }

        context("여성 동물상 목록을 검증하면") {
            it("HAMSTER는 허용하고 WOLF는 거부한다") {
                shouldNotThrowAny { ProfileValidator.validateAnimal(Gender.FEMALE, Animal.HAMSTER) }
                shouldThrow<AnimalGenderMismatchException> {
                    ProfileValidator.validateAnimal(Gender.FEMALE, Animal.WOLF)
                }
            }
        }

        context("공통 동물상이면") {
            it("남녀 모두 허용한다") {
                listOf(Animal.DOG, Animal.CAT).forEach { animal ->
                    shouldNotThrowAny { ProfileValidator.validateAnimal(Gender.MALE, animal) }
                    shouldNotThrowAny { ProfileValidator.validateAnimal(Gender.FEMALE, animal) }
                }
            }
        }
    }
})
