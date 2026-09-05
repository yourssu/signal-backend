package com.yourssu.signal.api.meeting

import com.yourssu.signal.api.MeetingController
import com.yourssu.signal.api.dto.meeting.MeetingAdminCancelRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val ADMIN_METHODS = setOf("adminCancel")

class MeetingControllerContractTest : DescribeSpec({
    describe("미팅 API 인증 계약") {
        MeetingController::class.java.declaredMethods
            .filterNot { it.isSynthetic }
            .filterNot { it.name in ADMIN_METHODS }
            .forEach { method ->
                it("${method.name}은 UUID 인증을 요구한다") {
                    method.isAnnotationPresent(RequireAuth::class.java) shouldBe true
                    method.parameterAnnotations
                        .flatten()
                        .any { annotation -> annotation is UserUuid } shouldBe true
                }
            }
    }

    describe("미팅 관리자 API 인증 계약") {
        it("adminCancel은 UUID 인증 대신 관리자 비밀 키를 본문으로 받는다") {
            val method = MeetingController::class.java.declaredMethods
                .first { it.name == "adminCancel" && !it.isSynthetic }

            method.isAnnotationPresent(RequireAuth::class.java) shouldBe false
            method.parameterAnnotations.flatten().any { it is UserUuid } shouldBe false
            method.parameterTypes.any { it == MeetingAdminCancelRequest::class.java } shouldBe true
        }
    }
})
