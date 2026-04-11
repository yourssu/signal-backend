package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.BirthYearViolatedException
import com.yourssu.signal.domain.profile.implement.exception.ContactLimitExceededException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceLengthViolatedException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceSizeViolatedException
import com.yourssu.signal.domain.profile.implement.exception.NicknameLengthViolatedException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import java.time.LocalDate

class ProfileValidatorTest : DescribeSpec({
    
    describe("ProfileValidator") {
        
        context("validateNickname 메서드를 호출할 때") {
            
            context("유효한 닉네임일 때") {
                
                context("1자 닉네임이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateNickname("가")
                        }
                    }
                }
                
                context("15자 닉네임이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateNickname("가".repeat(15))
                        }
                    }
                }
                
                context("일반적인 길이의 닉네임이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateNickname("테스트닉네임")
                        }
                    }
                }
                
                context("영어 닉네임이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateNickname("TestNickname")
                        }
                    }
                }
                
                context("특수문자가 포함된 닉네임이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateNickname("닉네임_123")
                        }
                    }
                }
            }
            
            context("유효하지 않은 닉네임일 때") {
                
                context("빈 문자열이면") {
                    it("NicknameLengthViolatedException을 발생시킨다") {
                        shouldThrow<NicknameLengthViolatedException> {
                            ProfileValidator.validateNickname("")
                        }
                    }
                }
                
                context("16자 닉네임이면") {
                    it("NicknameLengthViolatedException을 발생시킨다") {
                        shouldThrow<NicknameLengthViolatedException> {
                            ProfileValidator.validateNickname("가".repeat(16))
                        }
                    }
                }
                
                context("매우 긴 닉네임이면") {
                    it("NicknameLengthViolatedException을 발생시킨다") {
                        shouldThrow<NicknameLengthViolatedException> {
                            ProfileValidator.validateNickname("가".repeat(100))
                        }
                    }
                }
            }
        }
        
        context("validateIntroSentences 메서드를 호출할 때") {
            
            context("유효한 자기소개 문장들일 때") {
                
                context("빈 리스트면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateIntroSentences(emptyList())
                        }
                    }
                }
                
                context("1개의 짧은 문장이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateIntroSentences(listOf("안녕하세요"))
                        }
                    }
                }
                
                context("3개의 유효한 문장들이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateIntroSentences(
                                listOf("안녕하세요", "잘 부탁드려요", "감사합니다")
                            )
                        }
                    }
                }
                
                context("각 문장이 정확히 20자면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateIntroSentences(
                                listOf(
                                    "가".repeat(20),
                                    "나".repeat(20),
                                    "다".repeat(20)
                                )
                            )
                        }
                    }
                }
                
                context("영어 자기소개 문장이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateIntroSentences(
                                listOf("Hello", "Nice to meet you")
                            )
                        }
                    }
                }
            }
            
            context("유효하지 않은 자기소개 문장들일 때") {
                
                context("4개의 문장이면") {
                    it("IntroSentenceSizeViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceSizeViolatedException> {
                            ProfileValidator.validateIntroSentences(
                                listOf("첫번째", "두번째", "세번째", "네번째")
                            )
                        }
                    }
                }
                
                context("매우 많은 문장들이면") {
                    it("IntroSentenceSizeViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceSizeViolatedException> {
                            ProfileValidator.validateIntroSentences(
                                (1..10).map { "문장$it" }
                            )
                        }
                    }
                }
                
                context("첫 번째 문장이 21자면") {
                    it("IntroSentenceLengthViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceLengthViolatedException> {
                            ProfileValidator.validateIntroSentences(
                                listOf("가".repeat(21), "짧은문장")
                            )
                        }
                    }
                }
                
                context("중간 문장이 21자면") {
                    it("IntroSentenceLengthViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceLengthViolatedException> {
                            ProfileValidator.validateIntroSentences(
                                listOf("짧은문장", "나".repeat(21), "또다른짧은문장")
                            )
                        }
                    }
                }
                
                context("마지막 문장이 21자면") {
                    it("IntroSentenceLengthViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceLengthViolatedException> {
                            ProfileValidator.validateIntroSentences(
                                listOf("짧은문장", "다".repeat(21))
                            )
                        }
                    }
                }
                
                context("여러 문장이 모두 길면") {
                    it("IntroSentenceLengthViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceLengthViolatedException> {
                            ProfileValidator.validateIntroSentences(
                                listOf("가".repeat(25), "나".repeat(30))
                            )
                        }
                    }
                }
            }
        }
        
        context("validateBirthYear 메서드를 호출할 때") {
            
            context("유효한 출생년도일 때") {
                
                context("1900년이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateBirthYear(1900)
                        }
                    }
                }
                
                context("현재 년도면") {
                    it("예외를 발생시키지 않는다") {
                        val currentYear = LocalDate.now().year
                        shouldNotThrowAny {
                            ProfileValidator.validateBirthYear(currentYear)
                        }
                    }
                }
                
                context("일반적인 출생년도면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateBirthYear(1995)
                        }
                    }
                }
                
                context("2000년대 출생이면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.validateBirthYear(2005)
                        }
                    }
                }
            }
            
            context("유효하지 않은 출생년도일 때") {
                
                context("1899년이면") {
                    it("BirthYearViolatedException을 발생시킨다") {
                        shouldThrow<BirthYearViolatedException> {
                            ProfileValidator.validateBirthYear(1899)
                        }
                    }
                }
                
                context("매우 오래된 년도면") {
                    it("BirthYearViolatedException을 발생시킨다") {
                        shouldThrow<BirthYearViolatedException> {
                            ProfileValidator.validateBirthYear(1800)
                        }
                    }
                }
                
                context("현재 년도보다 1년 큰 년도면") {
                    it("BirthYearViolatedException을 발생시킨다") {
                        val nextYear = LocalDate.now().year + 1
                        shouldThrow<BirthYearViolatedException> {
                            ProfileValidator.validateBirthYear(nextYear)
                        }
                    }
                }
                
                context("미래의 년도면") {
                    it("BirthYearViolatedException을 발생시킨다") {
                        shouldThrow<BirthYearViolatedException> {
                            ProfileValidator.validateBirthYear(3000)
                        }
                    }
                }
            }
        }
        
        context("checkContactLimit 메서드를 호출할 때") {
            
            context("연락처 제한이 무제한 정책(0)일 때") {
                
                context("현재 연락처 수가 많아도") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.checkContactLimit(
                                countContact = 1000,
                                contactLimitPolicy = 0
                            )
                        }
                    }
                }
                
                context("현재 연락처 수가 0이어도") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.checkContactLimit(
                                countContact = 0,
                                contactLimitPolicy = 0
                            )
                        }
                    }
                }
            }
            
            context("연락처 제한이 있을 때") {
                
                context("현재 연락처 수가 제한보다 적으면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.checkContactLimit(
                                countContact = 5,
                                contactLimitPolicy = 10
                            )
                        }
                    }
                }
                
                context("현재 연락처 수가 제한과 같으면") {
                    it("ContactLimitExceededException을 발생시킨다") {
                        shouldThrow<ContactLimitExceededException> {
                            ProfileValidator.checkContactLimit(
                                countContact = 10,
                                contactLimitPolicy = 10
                            )
                        }
                    }
                }
                
                context("현재 연락처 수가 제한보다 많으면") {
                    it("ContactLimitExceededException을 발생시킨다") {
                        shouldThrow<ContactLimitExceededException> {
                            ProfileValidator.checkContactLimit(
                                countContact = 15,
                                contactLimitPolicy = 10
                            )
                        }
                    }
                }
                
                context("제한이 1이고 연락처가 1개면") {
                    it("ContactLimitExceededException을 발생시킨다") {
                        shouldThrow<ContactLimitExceededException> {
                            ProfileValidator.checkContactLimit(
                                countContact = 1,
                                contactLimitPolicy = 1
                            )
                        }
                    }
                }
            }
        }
        
        context("checkContactLimitWarning 메서드를 호출할 때") {
            
            context("경고 제한이 무제한 정책(0)일 때") {
                
                context("현재 연락처 수가 많아도") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.checkContactLimitWarning(
                                countContact = 1000,
                                contactLimitWarning = 0
                            )
                        }
                    }
                }
            }
            
            context("경고 제한이 있을 때") {
                
                context("현재 연락처 수가 경고 제한보다 적으면") {
                    it("예외를 발생시키지 않는다") {
                        shouldNotThrowAny {
                            ProfileValidator.checkContactLimitWarning(
                                countContact = 3,
                                contactLimitWarning = 5
                            )
                        }
                    }
                }
                
                context("현재 연락처 수가 경고 제한과 같으면") {
                    it("예외를 발생시키지 않는다 (경고만 발생)") {
                        shouldNotThrowAny {
                            ProfileValidator.checkContactLimitWarning(
                                countContact = 5,
                                contactLimitWarning = 5
                            )
                        }
                    }
                }
                
                context("현재 연락처 수가 경고 제한보다 많으면") {
                    it("예외를 발생시키지 않는다 (경고만 발생)") {
                        shouldNotThrowAny {
                            ProfileValidator.checkContactLimitWarning(
                                countContact = 8,
                                contactLimitWarning = 5
                            )
                        }
                    }
                }
            }
        }
    }
})