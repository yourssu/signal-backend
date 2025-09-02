package com.yourssu.signal.domain.auth.business.exception

import com.yourssu.signal.handler.UnauthorizedException
class InvalidGoogleAccessTokenException : UnauthorizedException(message = "Invalid Google access token")
