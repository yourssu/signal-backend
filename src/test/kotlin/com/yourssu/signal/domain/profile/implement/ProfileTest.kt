package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.exception.BirthYearViolatedException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceLengthViolatedException
import com.yourssu.signal.domain.profile.implement.exception.IntroSentenceSizeViolatedException
import com.yourssu.signal.domain.profile.implement.exception.NicknameLengthViolatedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.ZonedDateTime

class ProfileTest : DescribeSpec({
    
    describe("Profile 도메인 엔티티") {
        
        // Data - 테스트 데이터 빌더
        fun createValidProfile(
            id: Long? = 1L,
            uuid: Uuid = Uuid.randomUUID(),
            gender: Gender = Gender.MALE,
            department: String = "컴퓨터학부",
            birthYear: Int = 2000,
            animal: String = "강아지",
            contact: String = "@test_contact",
            mbti: String = "ENFP",
            nickname: String = "테스트닉네임",
            introSentences: List<String> = listOf("안녕하세요", "잘 부탁드려요"),
            school: String = "숭실대학교"
        ) = Profile(
            id = id,
            uuid = uuid,
            gender = gender,
            department = department,
            birthYear = birthYear,
            animal = animal,
            contact = contact,
            mbti = mbti,
            nickname = nickname,
            introSentences = introSentences,
            school = school
        )
        
        context("Profile 생성 시") {
            
            context("모든 필드가 유효할 때") {
                it("Profile 객체가 정상 생성된다") {
                    // given
                    val uuid = Uuid.randomUUID()
                    
                    // when
                    val profile = createValidProfile(uuid = uuid)
                    
                    // then
                    profile shouldNotBe null
                    profile.uuid shouldBe uuid
                    profile.gender shouldBe Gender.MALE
                    profile.department shouldBe "컴퓨터학부"
                    profile.birthYear shouldBe 2000
                    profile.animal shouldBe "강아지"
                    profile.contact shouldBe "@test_contact"
                    profile.mbti shouldBe "ENFP"
                    profile.nickname shouldBe "테스트닉네임"
                    profile.introSentences shouldBe listOf("안녕하세요", "잘 부탁드려요")
                    profile.school shouldBe "숭실대학교"
                }
            }
            
            context("닉네임이 유효하지 않을 때") {
                
                context("빈 닉네임이면") {
                    it("NicknameLengthViolatedException을 발생시킨다") {
                        shouldThrow<NicknameLengthViolatedException> {
                            createValidProfile(nickname = "")
                        }
                    }
                }
                
                context("닉네임이 15자를 초과하면") {
                    it("NicknameLengthViolatedException을 발생시킨다") {
                        shouldThrow<NicknameLengthViolatedException> {
                            createValidProfile(nickname = "가".repeat(16))
                        }
                    }
                }
                
                context("닉네임이 정확히 15자면") {
                    it("Profile 객체가 정상 생성된다") {
                        // given
                        val validNickname = "가".repeat(15)
                        
                        // when
                        val profile = createValidProfile(nickname = validNickname)
                        
                        // then
                        profile.nickname shouldBe validNickname
                    }
                }
            }
            
            context("자기소개 문장이 유효하지 않을 때") {
                
                context("자기소개 문장이 3개를 초과하면") {
                    it("IntroSentenceSizeViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceSizeViolatedException> {
                            createValidProfile(introSentences = listOf("첫번째", "두번째", "세번째", "네번째"))
                        }
                    }
                }
                
                context("자기소개 문장 중 하나가 20자를 초과하면") {
                    it("IntroSentenceLengthViolatedException을 발생시킨다") {
                        shouldThrow<IntroSentenceLengthViolatedException> {
                            createValidProfile(introSentences = listOf("안녕하세요", "가".repeat(21)))
                        }
                    }
                }
                
                context("자기소개 문장이 정확히 3개이고 각각 20자 이하면") {
                    it("Profile 객체가 정상 생성된다") {
                        // given
                        val validIntroSentences = listOf(
                            "가".repeat(20),
                            "나".repeat(15),
                            "다".repeat(10)
                        )
                        
                        // when
                        val profile = createValidProfile(introSentences = validIntroSentences)
                        
                        // then
                        profile.introSentences shouldBe validIntroSentences
                    }
                }
                
                context("자기소개 문장이 빈 리스트면") {
                    it("Profile 객체가 정상 생성된다") {
                        // given & when
                        val profile = createValidProfile(introSentences = emptyList())
                        
                        // then
                        profile.introSentences shouldBe emptyList()
                    }
                }
            }
            
            context("출생년도가 유효하지 않을 때") {
                
                context("출생년도가 1900년 미만이면") {
                    it("BirthYearViolatedException을 발생시킨다") {
                        shouldThrow<BirthYearViolatedException> {
                            createValidProfile(birthYear = 1899)
                        }
                    }
                }
                
                context("출생년도가 현재 년도보다 크면") {
                    it("BirthYearViolatedException을 발생시킨다") {
                        shouldThrow<BirthYearViolatedException> {
                            createValidProfile(birthYear = 2026)
                        }
                    }
                }
                
                context("출생년도가 1900년이면") {
                    it("Profile 객체가 정상 생성된다") {
                        // given & when
                        val profile = createValidProfile(birthYear = 1900)
                        
                        // then
                        profile.birthYear shouldBe 1900
                    }
                }
                
                context("출생년도가 현재 년도면") {
                    it("Profile 객체가 정상 생성된다") {
                        // given & when
                        val profile = createValidProfile(birthYear = 2024)
                        
                        // then
                        profile.birthYear shouldBe 2024
                    }
                }
            }
        }
        
        context("copy 메서드를 호출할 때") {
            
            context("기본값으로 복사하면") {
                it("모든 필드가 동일한 새로운 Profile 객체를 반환한다") {
                    // given
                    val originalProfile = createValidProfile()
                    
                    // when
                    val copiedProfile = originalProfile.copy()
                    
                    // then
                    copiedProfile shouldNotBe originalProfile // 다른 객체
                    copiedProfile.id shouldBe originalProfile.id
                    copiedProfile.uuid shouldBe originalProfile.uuid
                    copiedProfile.gender shouldBe originalProfile.gender
                    copiedProfile.department shouldBe originalProfile.department
                    copiedProfile.birthYear shouldBe originalProfile.birthYear
                    copiedProfile.animal shouldBe originalProfile.animal
                    copiedProfile.contact shouldBe originalProfile.contact
                    copiedProfile.mbti shouldBe originalProfile.mbti
                    copiedProfile.nickname shouldBe originalProfile.nickname
                    copiedProfile.introSentences shouldBe originalProfile.introSentences
                    copiedProfile.school shouldBe originalProfile.school
                }
            }
            
            context("자기소개 문장을 변경하면") {
                it("새로운 자기소개 문장으로 복사된다") {
                    // given
                    val originalProfile = createValidProfile()
                    val newIntroSentences = listOf("새로운 소개", "변경된 내용")
                    
                    // when
                    val copiedProfile = originalProfile.copy(introSentences = newIntroSentences)
                    
                    // then
                    copiedProfile.introSentences shouldBe newIntroSentences
                    copiedProfile.introSentences shouldNotBe originalProfile.introSentences
                }
            }
            
            context("연락처를 변경하면") {
                it("새로운 연락처로 복사된다") {
                    // given
                    val originalProfile = createValidProfile()
                    val newContact = "@new_contact"
                    
                    // when
                    val copiedProfile = originalProfile.copy(contact = newContact)
                    
                    // then
                    copiedProfile.contact shouldBe newContact
                    copiedProfile.contact shouldNotBe originalProfile.contact
                }
            }
            
            context("닉네임을 변경하면") {
                it("새로운 닉네임으로 복사된다") {
                    // given
                    val originalProfile = createValidProfile()
                    val newNickname = "새로운닉네임"
                    
                    // when
                    val copiedProfile = originalProfile.copy(nickname = newNickname)
                    
                    // then
                    copiedProfile.nickname shouldBe newNickname
                    copiedProfile.nickname shouldNotBe originalProfile.nickname
                }
            }
            
            context("모든 변경 가능한 필드를 한 번에 변경하면") {
                it("변경된 값들로 새로운 Profile 객체가 생성된다") {
                    // given
                    val originalProfile = createValidProfile()
                    val newIntroSentences = listOf("완전히 새로운 소개")
                    val newContact = "@completely_new"
                    val newNickname = "완전새닉네임"
                    
                    // when
                    val copiedProfile = originalProfile.copy(
                        introSentences = newIntroSentences,
                        contact = newContact,
                        nickname = newNickname
                    )
                    
                    // then
                    copiedProfile.introSentences shouldBe newIntroSentences
                    copiedProfile.contact shouldBe newContact
                    copiedProfile.nickname shouldBe newNickname
                    // 변경되지 않은 필드들은 그대로 유지
                    copiedProfile.id shouldBe originalProfile.id
                    copiedProfile.uuid shouldBe originalProfile.uuid
                    copiedProfile.gender shouldBe originalProfile.gender
                }
            }
            
            context("유효하지 않은 값으로 복사하려 하면") {
                
                context("유효하지 않은 닉네임으로 복사하면") {
                    it("NicknameLengthViolatedException을 발생시킨다") {
                        // given
                        val originalProfile = createValidProfile()
                        
                        // when & then
                        shouldThrow<NicknameLengthViolatedException> {
                            originalProfile.copy(nickname = "")
                        }
                    }
                }
                
                context("유효하지 않은 자기소개 문장으로 복사하면") {
                    it("IntroSentenceSizeViolatedException을 발생시킨다") {
                        // given
                        val originalProfile = createValidProfile()
                        
                        // when & then
                        shouldThrow<IntroSentenceSizeViolatedException> {
                            originalProfile.copy(introSentences = listOf("1", "2", "3", "4"))
                        }
                    }
                }
            }
        }
    }
})