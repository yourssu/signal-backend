package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.AnimalGenderMismatchException
import com.yourssu.signal.domain.profile.implement.exception.ContactFormatViolatedException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class ProfileValidationPolicyTest : DescribeSpec({
    describe("연락처 형식 검증") {
        context("010 다음 숫자가 2 또는 9이면") {
            it("전화번호를 허용한다") {
                shouldNotThrowAny { ProfileValidator.validateContact("01020000000") }
                shouldNotThrowAny { ProfileValidator.validateContact("01099999999") }
            }
        }

        context("010 다음 숫자가 0 또는 1이면") {
            it("전화번호를 거부한다") {
                shouldThrow<ContactFormatViolatedException> { ProfileValidator.validateContact("01000000000") }
                shouldThrow<ContactFormatViolatedException> { ProfileValidator.validateContact("01019999999") }
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
