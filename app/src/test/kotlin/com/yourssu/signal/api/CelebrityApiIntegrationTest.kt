package com.yourssu.signal.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.CelebrityTable
import com.yourssu.signal.domain.profile.implement.Gender
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CelebrityApiIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `인증 없이 해당 동물상의 연예인 전체 목록을 정의 순서대로 조회한다`() {
        val expected = CelebrityTable.get(Gender.MALE, Animal.BEAR)

        val response = mockMvc.get("/api/profiles/celebrities") {
            param("gender", "male")
            param("animal", "bear")
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        objectMapper.readTree(response).path("result").map { it.asText() } shouldBe expected
    }

    @Test
    fun `성별에 허용되지 않는 동물상 조합은 400을 반환한다`() {
        mockMvc.get("/api/profiles/celebrities") {
            param("gender", "MALE")
            param("animal", "HAMSTER")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("성별에 해당하지 않는 동물상입니다.") }
        }
    }

    @Test
    fun `존재하지 않는 동물상은 400을 반환한다`() {
        mockMvc.get("/api/profiles/celebrities") {
            param("gender", "FEMALE")
            param("animal", "ALIEN")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("해당하는 동물상을 찾을 수 없습니다.") }
        }
    }
}
