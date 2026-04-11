package com.yourssu.signal.infrastructure.openai.exception

import com.yourssu.signal.handler.BadRequestException

class FailedOpenAIModelException : BadRequestException(message = "OpenAI 모델 호출에 실패하였습니다.") {
}
