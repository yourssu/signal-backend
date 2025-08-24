package com.yourssu.signal.domain.user.business

import com.yourssu.signal.domain.auth.implement.UserReader
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.user.business.dto.UserInfoResponse
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userReader: UserReader
) {
    fun getMyInfo(uuid: String): UserInfoResponse {
        val user = userReader.getByUuid(Uuid(uuid))
        return UserInfoResponse(user.uuid.value)
    }
}
