package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.viewer.implement.exception.ViolatedAddedTicketException
import com.yourssu.signal.domain.viewer.implement.exception.ViolatedExceedUsedTicketException
import com.yourssu.signal.domain.viewer.implement.exception.ViewerNotSameException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.ZonedDateTime

class ViewerTest : DescribeSpec({
    
    describe("Viewer 도메인 엔티티") {
        
        // Data - 테스트 데이터 빌더 및 헬퍼 함수들
        fun createValidViewer(
            id: Long? = 1L,
            uuid: Uuid = Uuid.randomUUID(),
            ticket: Int = 10,
            usedTicket: Int = 0,
            updatedTime: ZonedDateTime? = ZonedDateTime.now()
        ) = Viewer(
            id = id,
            uuid = uuid,
            ticket = ticket,
            usedTicket = usedTicket,
            updatedTime = updatedTime
        )
        
        fun createViewerWithTickets(totalTickets: Int, usedTickets: Int = 0) = createValidViewer(
            ticket = totalTickets,
            usedTicket = usedTickets
        )
        
        fun createViewerWithMaxUsedTickets(totalTickets: Int) = createValidViewer(
            ticket = totalTickets,
            usedTicket = totalTickets
        )
        
        fun createEmptyViewer() = createValidViewer(
            ticket = 1,  // 최소 1개는 있어야 생성 가능
            usedTicket = 0
        )
        
        context("Viewer 생성 시") {
            
            context("모든 필드가 유효할 때") {
                it("Viewer 객체가 정상 생성된다") {
                    // given
                    val uuid = Uuid.randomUUID()
                    val id = 123L
                    val ticket = 5
                    val usedTicket = 2
                    val updatedTime = ZonedDateTime.now()
                    
                    // when
                    val viewer = Viewer(
                        id = id,
                        uuid = uuid,
                        ticket = ticket,
                        usedTicket = usedTicket,
                        updatedTime = updatedTime
                    )
                    
                    // then
                    viewer shouldNotBe null
                    viewer.id shouldBe id
                    viewer.uuid shouldBe uuid
                    viewer.ticket shouldBe ticket
                    viewer.usedTicket shouldBe usedTicket
                    viewer.updatedTime shouldBe updatedTime
                }
            }
            
            context("기본값으로 생성할 때") {
                it("usedTicket이 0으로 초기화된다") {
                    // given
                    val uuid = Uuid.randomUUID()
                    val ticket = 5
                    
                    // when
                    val viewer = Viewer(
                        uuid = uuid,
                        ticket = ticket,
                        updatedTime = null
                    )
                    
                    // then
                    viewer.usedTicket shouldBe 0
                    viewer.id shouldBe null
                    viewer.updatedTime shouldBe null
                }
            }
            
            context("경계값으로 생성할 때") {
                
                context("ticket이 1일 때") {
                    it("정상적으로 생성된다") {
                        // given & when
                        val viewer = createValidViewer(ticket = 1)
                        
                        // then
                        viewer.ticket shouldBe 1
                    }
                }
                
                context("ticket이 매우 클 때") {
                    it("정상적으로 생성된다") {
                        // given & when
                        val viewer = createValidViewer(ticket = Int.MAX_VALUE)
                        
                        // then
                        viewer.ticket shouldBe Int.MAX_VALUE
                    }
                }
            }
        }
        
        context("addTicket 메서드 호출 시") {
            
            context("유효한 티켓 수량을 추가할 때") {
                
                context("최소 추가 수량(1개)을 추가하면") {
                    it("기존 티켓에 추가되어 새로운 Viewer 객체를 반환한다") {
                        // given
                        val originalViewer = createViewerWithTickets(totalTickets = 5, usedTickets = 2)
                        
                        // when
                        val updatedViewer = originalViewer.addTicket(1)
                        
                        // then
                        updatedViewer shouldNotBe originalViewer  // 새로운 객체
                        updatedViewer.ticket shouldBe 6  // 5 + 1
                        updatedViewer.usedTicket shouldBe 2  // 변경되지 않음
                        updatedViewer.id shouldBe originalViewer.id
                        updatedViewer.uuid shouldBe originalViewer.uuid
                        updatedViewer.updatedTime shouldBe originalViewer.updatedTime
                    }
                }
                
                context("여러 개의 티켓을 추가하면") {
                    it("모든 티켓이 추가된 새로운 Viewer 객체를 반환한다") {
                        // given
                        val originalViewer = createViewerWithTickets(totalTickets = 10, usedTickets = 3)
                        val ticketsToAdd = 7
                        
                        // when
                        val updatedViewer = originalViewer.addTicket(ticketsToAdd)
                        
                        // then
                        updatedViewer.ticket shouldBe 17  // 10 + 7
                        updatedViewer.usedTicket shouldBe 3  // 변경되지 않음
                    }
                }
                
                context("대량의 티켓을 추가하면") {
                    it("정상적으로 추가된다") {
                        // given
                        val originalViewer = createValidViewer(ticket = 100)
                        val largeTicketAmount = 1000000
                        
                        // when
                        val updatedViewer = originalViewer.addTicket(largeTicketAmount)
                        
                        // then
                        updatedViewer.ticket shouldBe 1000100  // 100 + 1000000
                    }
                }
            }
            
            context("유효하지 않은 티켓 수량을 추가할 때") {
                
                context("0개를 추가하려 하면") {
                    it("ViolatedAddedTicketException을 발생시킨다") {
                        // given
                        val viewer = createValidViewer()
                        
                        // when & then
                        shouldThrow<ViolatedAddedTicketException> {
                            viewer.addTicket(0)
                        }
                    }
                }
                
                context("음수 개를 추가하려 하면") {
                    it("ViolatedAddedTicketException을 발생시킨다") {
                        // given
                        val viewer = createValidViewer()
                        
                        // when & then
                        shouldThrow<ViolatedAddedTicketException> {
                            viewer.addTicket(-1)
                        }
                        
                        shouldThrow<ViolatedAddedTicketException> {
                            viewer.addTicket(-100)
                        }
                    }
                }
            }
        }
        
        context("consumeTicket 메서드 호출 시") {
            
            context("유효한 범위에서 티켓을 소비할 때") {
                
                context("1개의 티켓을 소비하면") {
                    it("사용된 티켓 수가 증가한 새로운 Viewer 객체를 반환한다") {
                        // given
                        val originalViewer = createViewerWithTickets(totalTickets = 5, usedTickets = 2)
                        
                        // when
                        val updatedViewer = originalViewer.consumeTicket(1)
                        
                        // then
                        updatedViewer shouldNotBe originalViewer  // 새로운 객체
                        updatedViewer.ticket shouldBe 5  // 변경되지 않음
                        updatedViewer.usedTicket shouldBe 3  // 2 + 1
                        updatedViewer.id shouldBe originalViewer.id
                        updatedViewer.uuid shouldBe originalViewer.uuid
                        updatedViewer.updatedTime shouldBe originalViewer.updatedTime
                    }
                }
                
                context("여러 개의 티켓을 소비하면") {
                    it("모든 티켓이 소비된 새로운 Viewer 객체를 반환한다") {
                        // given
                        val originalViewer = createViewerWithTickets(totalTickets = 10, usedTickets = 2)
                        val ticketsToConsume = 3
                        
                        // when
                        val updatedViewer = originalViewer.consumeTicket(ticketsToConsume)
                        
                        // then
                        updatedViewer.ticket shouldBe 10  // 변경되지 않음
                        updatedViewer.usedTicket shouldBe 5  // 2 + 3
                    }
                }
                
                context("남은 티켓을 모두 소비하면") {
                    it("사용 가능한 모든 티켓이 소비된다") {
                        // given
                        val originalViewer = createViewerWithTickets(totalTickets = 10, usedTickets = 7)
                        val remainingTickets = 3  // 10 - 7 = 3
                        
                        // when
                        val updatedViewer = originalViewer.consumeTicket(remainingTickets)
                        
                        // then
                        updatedViewer.usedTicket shouldBe 10  // 7 + 3
                        updatedViewer.ticket shouldBe 10
                    }
                }
                
                context("사용된 티켓이 없는 상태에서 소비하면") {
                    it("정상적으로 소비된다") {
                        // given
                        val originalViewer = createViewerWithTickets(totalTickets = 5, usedTickets = 0)
                        
                        // when
                        val updatedViewer = originalViewer.consumeTicket(2)
                        
                        // then
                        updatedViewer.usedTicket shouldBe 2
                    }
                }
            }
            
            context("유효하지 않은 범위에서 티켓을 소비할 때") {
                
                context("보유한 티켓보다 많이 소비하려 하면") {
                    it("ViolatedExceedUsedTicketException을 발생시킨다") {
                        // given
                        val viewer = createViewerWithTickets(totalTickets = 5, usedTickets = 0)
                        
                        // when & then
                        shouldThrow<ViolatedExceedUsedTicketException> {
                            viewer.consumeTicket(6)  // 5개보다 많이 소비
                        }
                    }
                }
                
                context("이미 사용한 티켓을 고려했을 때 초과하여 소비하려 하면") {
                    it("ViolatedExceedUsedTicketException을 발생시킨다") {
                        // given
                        val viewer = createViewerWithTickets(totalTickets = 10, usedTickets = 7)
                        
                        // when & then
                        shouldThrow<ViolatedExceedUsedTicketException> {
                            viewer.consumeTicket(4)  // 남은 티켓 3개보다 많이 소비
                        }
                    }
                }
                
                context("모든 티켓을 이미 사용한 상태에서 추가로 소비하려 하면") {
                    it("ViolatedExceedUsedTicketException을 발생시킨다") {
                        // given
                        val viewer = createViewerWithMaxUsedTickets(totalTickets = 5)
                        
                        // when & then
                        shouldThrow<ViolatedExceedUsedTicketException> {
                            viewer.consumeTicket(1)
                        }
                    }
                }
                
                context("0개의 티켓을 소비하려 하면") {
                    it("정상적으로 처리되어 기존과 동일한 상태를 반환한다") {
                        // given
                        val originalViewer = createViewerWithTickets(totalTickets = 5, usedTickets = 2)
                        
                        // when
                        val updatedViewer = originalViewer.consumeTicket(0)
                        
                        // then
                        updatedViewer.usedTicket shouldBe 2  // 변경되지 않음
                        updatedViewer.ticket shouldBe 5
                    }
                }
            }
            
            context("경계값 테스트") {
                
                context("정확히 한계까지 소비할 때") {
                    it("성공적으로 소비된다") {
                        // given
                        val viewer = createViewerWithTickets(totalTickets = 10, usedTickets = 5)
                        
                        // when
                        val updatedViewer = viewer.consumeTicket(5)  // 정확히 남은 만큼
                        
                        // then
                        updatedViewer.usedTicket shouldBe 10
                    }
                }
                
                context("한계를 1개 초과하여 소비할 때") {
                    it("ViolatedExceedUsedTicketException을 발생시킨다") {
                        // given
                        val viewer = createViewerWithTickets(totalTickets = 10, usedTickets = 5)
                        
                        // when & then
                        shouldThrow<ViolatedExceedUsedTicketException> {
                            viewer.consumeTicket(6)  // 남은 5개보다 1개 많이
                        }
                    }
                }
            }
        }
        
        context("ensureSameViewer 메서드 호출 시") {
            
            context("동일한 Viewer 객체를 비교할 때") {
                it("예외를 발생시키지 않는다") {
                    // given
                    val viewer = createValidViewer()
                    
                    // when & then (예외가 발생하지 않으면 성공)
                    viewer.ensureSameViewer(viewer)
                }
            }
            
            context("같은 값을 가진 다른 Viewer 객체를 비교할 때") {
                it("ViewerNotSameException을 발생시킨다 (참조 기반 비교)") {
                    // given
                    val uuid = Uuid.randomUUID()
                    val id = 1L
                    val ticket = 5
                    val usedTicket = 2
                    val updatedTime = ZonedDateTime.now()
                    
                    val viewer1 = Viewer(id, uuid, ticket, usedTicket, updatedTime)
                    val viewer2 = Viewer(id, uuid, ticket, usedTicket, updatedTime)
                    
                    // when & then (Viewer는 data class가 아니므로 참조 기반 비교)
                    shouldThrow<ViewerNotSameException> {
                        viewer1.ensureSameViewer(viewer2)
                    }
                    shouldThrow<ViewerNotSameException> {
                        viewer2.ensureSameViewer(viewer1)
                    }
                }
            }
            
            context("다른 Viewer 객체를 비교할 때") {
                
                context("다른 id를 가진 Viewer와 비교하면") {
                    it("ViewerNotSameException을 발생시킨다") {
                        // given
                        val uuid = Uuid.randomUUID()
                        val viewer1 = createValidViewer(id = 1L, uuid = uuid)
                        val viewer2 = createValidViewer(id = 2L, uuid = uuid)
                        
                        // when & then
                        shouldThrow<ViewerNotSameException> {
                            viewer1.ensureSameViewer(viewer2)
                        }
                    }
                }
                
                context("다른 uuid를 가진 Viewer와 비교하면") {
                    it("ViewerNotSameException을 발생시킨다") {
                        // given
                        val viewer1 = createValidViewer(uuid = Uuid.randomUUID())
                        val viewer2 = createValidViewer(uuid = Uuid.randomUUID())
                        
                        // when & then
                        shouldThrow<ViewerNotSameException> {
                            viewer1.ensureSameViewer(viewer2)
                        }
                    }
                }
                
                context("다른 ticket 수를 가진 Viewer와 비교하면") {
                    it("ViewerNotSameException을 발생시킨다") {
                        // given
                        val uuid = Uuid.randomUUID()
                        val viewer1 = createValidViewer(uuid = uuid, ticket = 5)
                        val viewer2 = createValidViewer(uuid = uuid, ticket = 10)
                        
                        // when & then
                        shouldThrow<ViewerNotSameException> {
                            viewer1.ensureSameViewer(viewer2)
                        }
                    }
                }
                
                context("다른 usedTicket 수를 가진 Viewer와 비교하면") {
                    it("ViewerNotSameException을 발생시킨다") {
                        // given
                        val uuid = Uuid.randomUUID()
                        val viewer1 = createValidViewer(uuid = uuid, usedTicket = 2)
                        val viewer2 = createValidViewer(uuid = uuid, usedTicket = 3)
                        
                        // when & then
                        shouldThrow<ViewerNotSameException> {
                            viewer1.ensureSameViewer(viewer2)
                        }
                    }
                }
                
                context("다른 updatedTime을 가진 Viewer와 비교하면") {
                    it("ViewerNotSameException을 발생시킨다") {
                        // given
                        val uuid = Uuid.randomUUID()
                        val time1 = ZonedDateTime.now()
                        val time2 = time1.plusMinutes(1)
                        val viewer1 = createValidViewer(uuid = uuid, updatedTime = time1)
                        val viewer2 = createValidViewer(uuid = uuid, updatedTime = time2)
                        
                        // when & then
                        shouldThrow<ViewerNotSameException> {
                            viewer1.ensureSameViewer(viewer2)
                        }
                    }
                }
                
                context("null id와 non-null id를 가진 Viewer와 비교하면") {
                    it("ViewerNotSameException을 발생시킨다") {
                        // given
                        val uuid = Uuid.randomUUID()
                        val viewer1 = createValidViewer(id = null, uuid = uuid)
                        val viewer2 = createValidViewer(id = 1L, uuid = uuid)
                        
                        // when & then
                        shouldThrow<ViewerNotSameException> {
                            viewer1.ensureSameViewer(viewer2)
                        }
                    }
                }
                
                context("null updatedTime과 non-null updatedTime을 가진 Viewer와 비교하면") {
                    it("ViewerNotSameException을 발생시킨다") {
                        // given
                        val uuid = Uuid.randomUUID()
                        val viewer1 = createValidViewer(uuid = uuid, updatedTime = null)
                        val viewer2 = createValidViewer(uuid = uuid, updatedTime = ZonedDateTime.now())
                        
                        // when & then
                        shouldThrow<ViewerNotSameException> {
                            viewer1.ensureSameViewer(viewer2)
                        }
                    }
                }
            }
        }
        
        context("복합 시나리오 테스트") {
            
            context("티켓 추가 후 소비하는 시나리오") {
                it("올바른 상태 전이가 이루어진다") {
                    // given
                    val initialViewer = createViewerWithTickets(totalTickets = 5, usedTickets = 2)
                    
                    // when
                    val afterAddition = initialViewer.addTicket(3)  // 5 + 3 = 8
                    val afterConsumption = afterAddition.consumeTicket(2)  // used: 2 + 2 = 4
                    
                    // then
                    afterConsumption.ticket shouldBe 8
                    afterConsumption.usedTicket shouldBe 4
                    afterConsumption.uuid shouldBe initialViewer.uuid
                    afterConsumption.id shouldBe initialViewer.id
                }
            }
            
            context("여러 번의 티켓 추가와 소비") {
                it("모든 상태 변화가 정확히 추적된다") {
                    // given
                    var currentViewer = createViewerWithTickets(totalTickets = 10, usedTickets = 0)
                    
                    // when
                    currentViewer = currentViewer.consumeTicket(3)  // used: 3
                    currentViewer = currentViewer.addTicket(5)      // total: 15
                    currentViewer = currentViewer.consumeTicket(2)  // used: 5
                    currentViewer = currentViewer.addTicket(10)     // total: 25
                    currentViewer = currentViewer.consumeTicket(7)  // used: 12
                    
                    // then
                    currentViewer.ticket shouldBe 25
                    currentViewer.usedTicket shouldBe 12
                }
            }
            
            context("경계값에서의 복합 시나리오") {
                it("모든 티켓을 사용한 후 추가하여 다시 사용할 수 있다") {
                    // given
                    val viewer = createViewerWithTickets(totalTickets = 5, usedTickets = 0)
                    
                    // when - 모든 티켓 사용
                    val fullyUsedViewer = viewer.consumeTicket(5)
                    
                    // then - 더 이상 사용할 수 없음
                    shouldThrow<ViolatedExceedUsedTicketException> {
                        fullyUsedViewer.consumeTicket(1)
                    }
                    
                    // when - 티켓 추가 후 다시 사용
                    val replenishedViewer = fullyUsedViewer.addTicket(3)
                    val finalViewer = replenishedViewer.consumeTicket(2)
                    
                    // then
                    finalViewer.ticket shouldBe 8  // 5 + 3
                    finalViewer.usedTicket shouldBe 7  // 5 + 2
                }
            }
        }
        
        context("불변성 테스트") {
            
            context("addTicket 호출 시") {
                it("원본 객체는 변경되지 않는다") {
                    // given
                    val originalViewer = createValidViewer(ticket = 5, usedTicket = 2)
                    val originalTicket = originalViewer.ticket
                    val originalUsedTicket = originalViewer.usedTicket
                    
                    // when
                    val newViewer = originalViewer.addTicket(3)
                    
                    // then
                    originalViewer.ticket shouldBe originalTicket
                    originalViewer.usedTicket shouldBe originalUsedTicket
                    newViewer shouldNotBe originalViewer
                }
            }
            
            context("consumeTicket 호출 시") {
                it("원본 객체는 변경되지 않는다") {
                    // given
                    val originalViewer = createValidViewer(ticket = 5, usedTicket = 2)
                    val originalTicket = originalViewer.ticket
                    val originalUsedTicket = originalViewer.usedTicket
                    
                    // when
                    val newViewer = originalViewer.consumeTicket(2)
                    
                    // then
                    originalViewer.ticket shouldBe originalTicket
                    originalViewer.usedTicket shouldBe originalUsedTicket
                    newViewer shouldNotBe originalViewer
                }
            }
        }
    }
})