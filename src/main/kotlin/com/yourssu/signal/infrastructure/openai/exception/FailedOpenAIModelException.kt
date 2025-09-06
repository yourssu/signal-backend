package com.yourssu.signal.infrastructure.openai.exception

import com.yourssu.signal.handler.BadRequestException

class FailedOpenAIModelException(
    message: String = "OpenAI 모델 호출에 실패하였습니다."
) : BadRequestException(message = message) {
}
