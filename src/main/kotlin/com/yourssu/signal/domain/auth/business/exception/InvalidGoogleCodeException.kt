package com.yourssu.signal.domain.auth.business.exception

import com.yourssu.signal.handler.UnauthorizedException

class InvalidGoogleCodeException : UnauthorizedException(message = "Invalid a code for Google")
