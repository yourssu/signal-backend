//package com.yourssu.signal.infrastructure
//
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
//
//@RestController
//@RequestMapping("/api/viewers")
//class TicketSseManagerTest {
//    var emitter: SseEmitter? = null;
//
//    @GetMapping("/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
//    fun handle(): SseEmitter {
//        val emitter = SseEmitter(30_0000L)
//        this.emitter = emitter
//        emitter.send("\"hello\"", MediaType.TEXT_EVENT_STREAM)
//        return emitter
//    }
//}
