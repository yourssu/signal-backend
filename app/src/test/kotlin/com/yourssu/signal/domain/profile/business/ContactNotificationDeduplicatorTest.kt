package com.yourssu.signal.domain.profile.business

import com.google.common.base.Ticker
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class ContactNotificationDeduplicatorTest : DescribeSpec({
    describe("ContactNotificationDeduplicator") {
        val elapsedNanos = AtomicLong()
        val ticker = object : Ticker() {
            override fun read(): Long = elapsedNanos.get()
        }
        lateinit var deduplicator: ContactNotificationDeduplicator

        beforeEach {
            elapsedNanos.set(0)
            deduplicator = ContactNotificationDeduplicator(ticker)
        }

        it("같은 연락처와 알림 종류는 TTL 동안 한 번만 허용한다") {
            deduplicator.shouldNotify("@same_contact", ContactNotificationType.WARNING) shouldBe true
            deduplicator.shouldNotify("@same_contact", ContactNotificationType.WARNING) shouldBe false

            elapsedNanos.addAndGet(TimeUnit.HOURS.toNanos(1) + 1)

            deduplicator.shouldNotify("@same_contact", ContactNotificationType.WARNING) shouldBe true
        }

        it("warning과 failure는 기존 정책대로 별도 사건으로 제한한다") {
            deduplicator.shouldNotify("@same_contact", ContactNotificationType.WARNING) shouldBe true
            deduplicator.shouldNotify("@same_contact", ContactNotificationType.FAILURE) shouldBe true
            deduplicator.shouldNotify("@same_contact", ContactNotificationType.WARNING) shouldBe false
            deduplicator.shouldNotify("@same_contact", ContactNotificationType.FAILURE) shouldBe false
        }

        it("동시 요청에서도 같은 연락처와 알림 종류를 하나만 허용한다") {
            val executor = Executors.newFixedThreadPool(8)
            try {
                val results = (1..20).map {
                    executor.submit<Boolean> {
                        deduplicator.shouldNotify("@same_contact", ContactNotificationType.WARNING)
                    }
                }.map { it.get(5, TimeUnit.SECONDS) }

                results.count { it } shouldBe 1
            } finally {
                executor.shutdownNow()
            }
        }

        it("서로 다른 연락처는 독립적으로 허용한다") {
            deduplicator.shouldNotify("@first_contact", ContactNotificationType.WARNING) shouldBe true
            deduplicator.shouldNotify("@second_contact", ContactNotificationType.WARNING) shouldBe true
        }
    }
})
