package com.yourssu.signal.api.meeting

import com.yourssu.signal.api.MeetingController
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MeetingControllerContractTest : DescribeSpec({
    describe("미팅 API 인증 계약") {
        MeetingController::class.java.declaredMethods
            .filterNot { it.isSynthetic }
            .forEach { method ->
                it("${method.name}은 UUID 인증을 요구한다") {
                    method.isAnnotationPresent(RequireAuth::class.java) shouldBe true
                    method.parameterAnnotations
                        .flatten()
                        .any { annotation -> annotation is UserUuid } shouldBe true
                }
            }
    }
})
