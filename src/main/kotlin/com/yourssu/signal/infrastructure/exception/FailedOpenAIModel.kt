package com.yourssu.signal.infrastructure.exception

import com.yourssu.signal.handler.BadRequestException

class FailedOpenAIModel : BadRequestException(message = "OpenAI 모델 호출에 실패하였습니다.") {
}
