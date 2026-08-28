package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.AnimalGenderMismatchException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

class CelebrityTableTest : DescribeSpec({
    describe("CelebrityTable.get") {
        it("허용된 12개 성별과 동물상 조합은 비어 있지 않고 목록 내부 중복이 없다") {
            val combinations = ProfileValidationPolicy.maleAnimals.map { Gender.MALE to it } +
                ProfileValidationPolicy.femaleAnimals.map { Gender.FEMALE to it }

            combinations.size shouldBe 12
            combinations.forEach { (gender, animal) ->
                val celebrities = CelebrityTable.get(gender, animal)
                celebrities.shouldNotBeEmpty()
                celebrities.distinct().size shouldBe celebrities.size
            }
        }

        it("우기는 여자 강아지상과 햄스터상 양쪽에 존재한다") {
            CelebrityTable.get(Gender.FEMALE, Animal.DOG) shouldContain "우기"
            CelebrityTable.get(Gender.FEMALE, Animal.HAMSTER) shouldContain "우기"
        }

        it("성별에 허용되지 않는 동물상 조합은 거부한다") {
            shouldThrow<AnimalGenderMismatchException> {
                CelebrityTable.get(Gender.MALE, Animal.HAMSTER)
            }
            shouldThrow<AnimalGenderMismatchException> {
                CelebrityTable.get(Gender.FEMALE, Animal.WOLF)
            }
        }
    }
})
