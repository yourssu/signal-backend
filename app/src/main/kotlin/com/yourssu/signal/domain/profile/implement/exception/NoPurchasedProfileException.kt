package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.ForbiddenException

class NoPurchasedProfileException: ForbiddenException(message = "No purchased profile was found"){
}
