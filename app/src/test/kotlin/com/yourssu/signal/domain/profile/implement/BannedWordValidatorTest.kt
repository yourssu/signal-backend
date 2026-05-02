package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.BannedWordException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class BannedWordValidatorTest : DescribeSpec({
    describe("BannedWordValidator.validate") {
        val bannedWords = setOf("나쁜말", "badword")

        context("금지어가 없는 텍스트를 입력하면") {
            it("예외를 던지지 않는다") {
                shouldNotThrowAny {
                    BannedWordValidator.validate("안녕하세요", bannedWords)
                }
            }
        }

        context("금지어가 포함된 텍스트를 입력하면") {
            it("BannedWordException을 던진다") {
                shouldThrow<BannedWordException> {
                    BannedWordValidator.validate("나쁜말닉네임", bannedWords)
                }
            }
        }

        context("반복 문자로 금지어를 변형하면") {
            it("정규화 후 BannedWordException을 던진다") {
                shouldThrow<BannedWordException> {
                    BannedWordValidator.validate("나나쁜말", bannedWords)
                }
            }
        }

        context("공백을 섞어 금지어를 변형하면") {
            it("정규화 후 BannedWordException을 던진다") {
                shouldThrow<BannedWordException> {
                    BannedWordValidator.validate("나쁜 말", bannedWords)
                }
            }
        }

        context("특수문자를 섞어 금지어를 변형하면") {
            it("정규화 후 BannedWordException을 던진다") {
                shouldThrow<BannedWordException> {
                    BannedWordValidator.validate("나쁜.말", bannedWords)
                }
            }
        }

        context("영어 금지어를 대문자로 입력하면") {
            it("소문자 변환 후 BannedWordException을 던진다") {
                shouldThrow<BannedWordException> {
                    BannedWordValidator.validate("BADWORD", bannedWords)
                }
            }
        }

        context("빈 금지어 Set으로 검사하면") {
            it("예외를 던지지 않는다") {
                shouldNotThrowAny {
                    BannedWordValidator.validate("나쁜말", emptySet())
                }
            }
        }
    }
})
