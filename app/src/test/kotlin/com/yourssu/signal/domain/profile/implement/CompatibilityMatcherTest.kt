package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val PERFECT = CompatibilityLabel.PERFECT_MATCH
private val MBTI = CompatibilityLabel.MBTI_MATCH
private val ANIMAL = CompatibilityLabel.ANIMAL_MATCH

class CompatibilityMatcherTest : DescribeSpec({

    fun male(mbti: String, animal: String, birthYear: Int = 2000) = Profile(
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

    fun female(mbti: String, animal: String, birthYear: Int = 2001) = Profile(
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
                // male 2001, female 2001 → 동갑
                val m = male("INFP", "강아지", 2001)
                val f = female("ENFJ", "고양이", 2001)
                CompatibilityMatcher.match(m, f) shouldBe PERFECT
            }

            it("남자가 3살 연상이면 나이 조건을 만족한다") {
                // male 1998, female 2001 → 3살 연상
                val m = male("INFP", "강아지", 1998)
                val f = female("ENFJ", "고양이", 2001)
                CompatibilityMatcher.match(m, f) shouldBe PERFECT
            }

            it("남자가 4살 이상 연상이면 null 반환") {
                val m = male("INFP", "강아지", 1997)
                val f = female("ENFJ", "고양이", 2001)
                CompatibilityMatcher.match(m, f) shouldBe null
            }

            it("남자가 연하이면 null 반환") {
                val m = male("INFP", "강아지", 2003)
                val f = female("ENFJ", "고양이", 2001)
                CompatibilityMatcher.match(m, f) shouldBe null
            }

            it("여자 기준으로 남자가 동갑~3살 연상 조건을 정확히 검증한다") {
                // female 기준으로 myProfile=female인 경우도 동일하게 동작해야 함
                val f = female("INFP", "고양이", 2001)
                val m = male("ENFJ", "강아지", 1998) // 3살 연상
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }
        }

        context("MBTI와 동물상이 모두 맞으면") {
            it("PERFECT_MATCH 반환 (여자 기준)") {
                // 여자: INFP+고양이, 남자: ENFJ+강아지
                val f = female("INFP", "고양이", 2001)
                val m = male("ENFJ", "강아지", 2000)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }

            it("PERFECT_MATCH 반환 (남자 기준)") {
                // 남자: INFP+강아지, 여자: ENFJ+고양이
                val m = male("INFP", "강아지", 2000)
                val f = female("ENFJ", "고양이", 2001)
                CompatibilityMatcher.match(m, f) shouldBe PERFECT
            }
        }

        context("MBTI만 맞고 동물상은 안 맞으면") {
            it("MBTI_MATCH 반환") {
                // 여자: INFP+여우, 남자: ENFJ+강아지 → MBTI 맞음, 동물상 여우→[공룡,사슴] 미매칭
                val f = female("INFP", "여우", 2001)
                val m = male("ENFJ", "강아지", 2000)
                CompatibilityMatcher.match(f, m) shouldBe MBTI
            }
        }

        context("동물상만 맞고 MBTI는 안 맞으면") {
            it("ANIMAL_MATCH 반환") {
                // 여자: ENFP+고양이, 남자: ENFJ+강아지 → 동물상 고양이→[강아지,곰] 맞음, MBTI ENFP→[INFJ,INTJ] 미매칭
                val f = female("ENFP", "고양이", 2001)
                val m = male("ENFJ", "강아지", 2000)
                CompatibilityMatcher.match(f, m) shouldBe ANIMAL
            }
        }

        context("MBTI와 동물상 모두 안 맞으면") {
            it("null 반환") {
                // 여자: ENFP+여우, 남자: ENFJ+강아지 → 둘 다 미매칭
                val f = female("ENFP", "여우", 2001)
                val m = male("ENFJ", "강아지", 2000)
                CompatibilityMatcher.match(f, m) shouldBe null
            }
        }

        context("동물상 궁합 검증") {
            it("햄스터(여)+햄스터(남)은 매칭된다") {
                val f = female("ENFP", "햄스터", 2001)
                val m = male("INFJ", "햄스터", 2001)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }

            it("꼬부기(여)+햄스터(남)은 매칭된다") {
                val f = female("ENFP", "꼬부기", 2001)
                val m = male("INFJ", "햄스터", 2001)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }

            it("토끼(여)+사슴(남)은 매칭된다") {
                val f = female("ENFP", "토끼", 2001)
                val m = male("INFJ", "사슴", 2001)
                CompatibilityMatcher.match(f, m) shouldBe PERFECT
            }
        }
    }
})
