package com.yourssu.ssugaeting.infrastructure.exception

import com.yourssu.ssugaeting.handler.BadRequestException

class FailedOpenAIModel : BadRequestException(message = "OpenAI 모델 호출에 실패하였습니다.") {
}
