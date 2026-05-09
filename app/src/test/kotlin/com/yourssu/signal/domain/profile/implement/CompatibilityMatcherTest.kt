package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val PERFECT = CompatibilityLabel.PERFECT_MATCH
private val MBTI_AGE = CompatibilityLabel.MBTI_AGE_MATCH
private val ANIMAL_AGE = CompatibilityLabel.ANIMAL_AGE_MATCH
private val MBTI = CompatibilityLabel.MBTI_MATCH
private val ANIMAL = CompatibilityLabel.ANIMAL_MATCH
private val AGE = CompatibilityLabel.AGE_MATCH

class CompatibilityMatcherTest : DescribeSpec({

    fun male(mbti: String, animal: Animal, birthYear: Int = 2000) = Profile(
        id = 1L,
        uuid = Uuid.randomUUID(),
        gender = Gender.MALE,
        department = "컴퓨터학부",
        birthYear = birthYear,
        animal = animal,
        contact = "@male",
        mbti = mbti,
        nickname = "남자",
        introSentences = emptyList(),
        school = "숭실대학교",
    )

    fun female(mbti: String, animal: Animal, birthYear: Int = 2001) = Profile(
        id = 2L,
        uuid = Uuid.randomUUID(),
        gender = Gender.FEMALE,
        department = "컴퓨터학부",
        birthYear = birthYear,
        animal = animal,
        contact = "@female",
        mbti = mbti,
        nickname = "여자",
        introSentences = emptyList(),
        school = "숭실대학교",
    )

    describe("CompatibilityMatcher.match") {

        context("나이 조건 검사") {
            it("남자가 동갑이면 나이 조건을 만족한다") {
                val m = male("INFP", Animal.DOG, 2001)
                val f = female("ENFJ", Animal.CAT, 2001)
                CompatibilityMatcher.match(m, f) shouldBe PERFECT
            }

            it("남자가 3살 연상이면 나이 조건을 만족한다") {
                val m = male("INFP", Animal.DOG, 1998)
                val f = female("ENFJ", Animal.CAT, 2001)
                CompatibilityMatcher.match(m, f) shouldBe PERFECT
            }

            it("남자가 4살 이상 연상이면 나이 조건 불충족으로 PERFECT_MATCH 불가, MBTI 우선으로 MBTI_MATCH 반환") {
                val m = male("INFP", Animal.DOG, 1997)
                val f = female("ENFJ", Animal.CAT, 2001)
                CompatibilityMatcher.match(m, f) shouldBe MBTI
            }

            it("남자가 연하이면 나이 조건 불충족으로 PERFECT_MATCH 불가, MBTI 우선으로 MBTI_MATCH 반환") {
                val m = male("INFP", Animal.DOG, 2003)
                val f = female("ENFJ", Animal.CAT, 2001)
                CompatibilityMatcher.match(m, f) shouldBe MBTI
            }

            it("여자 기준으로 남자가 동갑~3살 연상 조건을 정확히 검증한다") {
                val f = female("INFP", Animal.CAT, 2001)
                val m = male("ENFJ", Animal.DOG, 1998)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }
        }

        context("MBTI와 동물상이 모두 맞으면") {
            it("PERFECT_MATCH 반환 (여자 기준)") {
                val f = female("INFP", Animal.CAT, 2001)
                val m = male("ENFJ", Animal.DOG, 2000)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }

            it("PERFECT_MATCH 반환 (남자 기준)") {
                val m = male("INFP", Animal.DOG, 2000)
                val f = female("ENFJ", Animal.CAT, 2001)
                CompatibilityMatcher.match(m, f) shouldBe PERFECT
            }
        }

        context("MBTI와 나이가 맞고 동물상이 안 맞으면") {
            it("MBTI_AGE_MATCH 반환") {
                // FOX(여) → 매칭 상대: DINOSAUR, DEER / DOG은 해당 없음
                val f = female("INFP", Animal.FOX, 2001)
                val m = male("ENFJ", Animal.DOG, 2000)
                CompatibilityMatcher.match(f, m) shouldBe MBTI_AGE
            }
        }

        context("동물상과 나이가 맞고 MBTI가 안 맞으면") {
            it("ANIMAL_AGE_MATCH 반환") {
                // CAT(여) → 매칭 상대: DOG, BEAR / ENFP → 매칭 상대: INFJ, INTJ (ENFJ 해당 없음)
                val f = female("ENFP", Animal.CAT, 2001)
                val m = male("ENFJ", Animal.DOG, 2000)
                CompatibilityMatcher.match(f, m) shouldBe ANIMAL_AGE
            }
        }

        context("MBTI만 맞고 동물상·나이 모두 안 맞으면") {
            it("MBTI_MATCH 반환") {
                // FOX(여) → 매칭 상대: DINOSAUR, DEER / DOG은 해당 없음. 남자 연하 → 나이 불충족
                val f = female("INFP", Animal.FOX, 2001)
                val m = male("ENFJ", Animal.DOG, 2003)
                CompatibilityMatcher.match(f, m) shouldBe MBTI
            }
        }

        context("동물상만 맞고 MBTI·나이 모두 안 맞으면") {
            it("ANIMAL_MATCH 반환") {
                // CAT(여) → 매칭 상대: DOG, BEAR / ENFP → INFJ, INTJ (ENFJ 해당 없음). 남자 연하 → 나이 불충족
                val f = female("ENFP", Animal.CAT, 2001)
                val m = male("ENFJ", Animal.DOG, 2003)
                CompatibilityMatcher.match(f, m) shouldBe ANIMAL
            }
        }

        context("MBTI와 동물상 모두 안 맞으면") {
            it("나이만 맞으면 AGE_MATCH 반환") {
                // FOX(여) → DINOSAUR, DEER / DOG 해당 없음. ENFP → INFJ, INTJ / ENFJ 해당 없음
                val f = female("ENFP", Animal.FOX, 2001)
                val m = male("ENFJ", Animal.DOG, 2000)
                CompatibilityMatcher.match(f, m) shouldBe AGE
            }

            it("나이도 안 맞으면 null 반환") {
                val f = female("ENFP", Animal.FOX, 2001)
                val m = male("ENFJ", Animal.DOG, 2003)
                CompatibilityMatcher.match(f, m) shouldBe null
            }
        }

        context("동물상 궁합 검증") {
            it("HAMSTER(여)+HAMSTER(남)은 매칭된다") {
                val f = female("ENFP", Animal.HAMSTER, 2001)
                val m = male("INFJ", Animal.HAMSTER, 2001)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }

            it("TURTLE(여)+HAMSTER(남)은 매칭된다") {
                val f = female("ENFP", Animal.TURTLE, 2001)
                val m = male("INFJ", Animal.HAMSTER, 2001)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }

            it("RABBIT(여)+DEER(남)은 매칭된다") {
                val f = female("ENFP", Animal.RABBIT, 2001)
                val m = male("INFJ", Animal.DEER, 2001)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }
        }
    }
})
