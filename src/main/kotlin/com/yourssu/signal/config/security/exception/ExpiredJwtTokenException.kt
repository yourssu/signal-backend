package com.yourssu.signal.config.security.exception

import com.yourssu.signal.handler.UnauthorizedException

class ExpiredJwtTokenException() : UnauthorizedException(message =  "JWT token has expired")
