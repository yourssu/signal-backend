package com.yourssu.signal.config.security.exception

import com.yourssu.signal.handler.UnauthorizedException

class InvalidJwtTokenException() : UnauthorizedException(message = "Invalid JWT token")
