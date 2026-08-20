package com.yourssu.signal.config.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import jakarta.servlet.FilterChain
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class LoggingFilterTest : DescribeSpec({
    val logger = LoggerFactory.getLogger("com.yourssu.signal.config.filter.LoggingFilter") as Logger
    lateinit var appender: ListAppender<ILoggingEvent>

    beforeEach {
        appender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(appender)
        logger.level = Level.INFO
    }

    afterEach {
        logger.detachAppender(appender)
        appender.stop()
    }

    describe("LoggingFilter") {
        context("OPTIONS 요청이면") {
            it("요청과 응답을 로깅하지 않는다") {
                execute(method = "OPTIONS", uri = "/api/profiles/deck", responseBody = "{}")

                appender.list.shouldBeEmpty()
            }
        }

        context("허용 헤더와 민감 헤더가 함께 전달되면") {
            it("운영 분석용 헤더만 기록하고 Authorization과 Cookie는 기록하지 않는다") {
                val request = MockHttpServletRequest("GET", "/api/users/me").apply {
                    addHeader("x-real-ip", "203.0.113.1")
                    addHeader(HttpHeaders.USER_AGENT, "signal-app")
                    addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    addHeader(HttpHeaders.AUTHORIZATION, "Bearer secret.jwt.token")
                    addHeader(HttpHeaders.COOKIE, "session=secret-cookie")
                    addHeader("x-forwarded-for", "198.51.100.2")
                }

                execute(request = request, responseBody = "{\"id\":1}")

                val detailLog = appender.list.first().formattedMessage
                detailLog shouldContain "\"x-real-ip\": \"203.0.113.1\""
                detailLog shouldContain "\"user-agent\": \"signal-app\""
                detailLog shouldContain "\"content-type\": \"application/json\""
                detailLog shouldNotContain "secret.jwt.token"
                detailLog shouldNotContain "secret-cookie"
                detailLog shouldNotContain "x-forwarded-for"
                detailLog shouldNotContain "authorization"
                detailLog shouldNotContain "cookie"
            }
        }

        context("작은 GET 성공 응답이면") {
            it("장애 분석을 위해 응답 페이로드를 그대로 기록한다") {
                execute(method = "GET", uri = "/api/users/me", responseBody = "{\"id\":1}")

                appender.list.first().formattedMessage shouldContain "\"Reply\":{\"Payload\":{\"id\":1}}"
            }
        }

        context("GET 성공 응답이 크기 제한을 넘으면") {
            it("응답 전문 대신 크기 요약만 기록한다") {
                val largeBody = "{\"value\":\"${"a".repeat(3_000)}\"}"

                execute(method = "GET", uri = "/api/users/me", responseBody = largeBody)

                val detailLog = appender.list.first().formattedMessage
                detailLog shouldContain "\"Payload\":{\"Truncated\":true,\"Bytes\":"
                detailLog shouldNotContain "a".repeat(100)
            }
        }

        context("대용량 응답 엔드포인트이면") {
            it("응답 크기와 무관하게 페이로드 전문을 기록하지 않는다") {
                execute(
                    method = "GET",
                    uri = "/api/profiles/deck",
                    responseBody = "[{\"nickname\":\"sensitive-profile\"}]",
                )

                val detailLog = appender.list.first().formattedMessage
                detailLog shouldContain "\"Payload\":{\"Truncated\":true,\"Bytes\":"
                detailLog shouldNotContain "sensitive-profile"
            }
        }

        context("인증 요청과 토큰 응답이면") {
            it("민감 필드만 마스킹하고 나머지 진단 정보는 보존한다") {
                val request = MockHttpServletRequest("POST", "/api/auth/refresh").apply {
                    setContent("{\"refreshToken\":\"request.jwt.token\",\"client\":\"android\"}".toByteArray())
                }

                execute(
                    request = request,
                    responseBody = "{\"accessToken\":\"access.jwt.token\",\"refreshToken\":\"response.jwt.token\"}",
                )

                val detailLog = appender.list.first().formattedMessage
                detailLog shouldContain "\"refreshToken\":\"***\""
                detailLog shouldContain "\"accessToken\":\"***\""
                detailLog shouldContain "\"client\":\"android\""
                detailLog shouldNotContain "request.jwt.token"
                detailLog shouldNotContain "access.jwt.token"
                detailLog shouldNotContain "response.jwt.token"
            }
        }

        context("연락처와 운영 시크릿을 포함한 요청이면") {
            it("contact, token, secretKey 값을 app 로그에서 마스킹한다") {
                val request = MockHttpServletRequest("POST", "/api/profiles").apply {
                    setContent(
                        """{"contact":"@plain_contact","token":"plain-token","secretKey":"plain-secret","nickname":"signal"}"""
                            .toByteArray(),
                    )
                }

                execute(request = request, responseBody = "{}")

                val detailLog = appender.list.first().formattedMessage
                detailLog shouldContain "\"contact\":\"***\""
                detailLog shouldContain "\"token\":\"***\""
                detailLog shouldContain "\"secretKey\":\"***\""
                detailLog shouldContain "\"nickname\":\"signal\""
                detailLog shouldNotContain "@plain_contact"
                detailLog shouldNotContain "plain-token"
                detailLog shouldNotContain "plain-secret"
            }

            it("경로 변수 secretKey를 app 로그에서 마스킹한다") {
                execute(
                    method = "POST",
                    uri = "/api/viewers/sms/deposit/plain-path-secret",
                    responseBody = "{}",
                )

                val logs = appender.list.joinToString("\n") { it.formattedMessage }
                logs shouldContain "/api/viewers/sms/deposit/***"
                logs shouldNotContain "plain-path-secret"
            }

            it("JSON으로 파싱할 수 없는 payload는 원문을 app 로그에 남기지 않는다") {
                val request = MockHttpServletRequest("POST", "/api/profiles").apply {
                    setContent("contact=@plain_contact&token=plain-token&secretKey=plain-secret".toByteArray())
                }

                execute(request = request, responseBody = "{}")

                val detailLog = appender.list.first().formattedMessage
                detailLog shouldContain "\"Redacted\":true"
                detailLog shouldNotContain "@plain_contact"
                detailLog shouldNotContain "plain-token"
                detailLog shouldNotContain "plain-secret"
            }
        }

        context("오류 응답이면") {
            it("GET 성공 응답보다 큰 페이로드도 제한 안에서 보존한다") {
                val errorBody = "{\"message\":\"${"failure-context".repeat(200)}\"}"

                execute(method = "GET", uri = "/api/users/me", status = 500, responseBody = errorBody)

                appender.list.first().formattedMessage shouldContain "failure-context"
            }

            it("대용량 응답 엔드포인트의 오류 본문도 제한 안에서 보존한다") {
                execute(
                    method = "GET",
                    uri = "/api/profiles/deck",
                    status = 500,
                    responseBody = "{\"message\":\"deck-failure-context\"}",
                )

                appender.list.first().formattedMessage shouldContain "deck-failure-context"
            }
        }
    }
}) {
    companion object {
        private fun execute(
            method: String = "GET",
            uri: String = "/api/users/me",
            status: Int = 200,
            responseBody: String,
            request: MockHttpServletRequest = MockHttpServletRequest(method, uri),
        ): MockHttpServletResponse {
            val response = MockHttpServletResponse()
            val chain = FilterChain { servletRequest, servletResponse ->
                servletRequest.inputStream.readAllBytes()
                (servletResponse as jakarta.servlet.http.HttpServletResponse).apply {
                    this.status = status
                    writer.write(responseBody)
                }
            }

            LoggingFilter(ObjectMapper()).doFilter(request, response, chain)

            response.contentAsString shouldBe responseBody
            return response
        }
    }
}
