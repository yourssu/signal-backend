package com.yourssu.signal.domain.viewer.implement.domain.exception

import com.yourssu.signal.handler.BadRequestException

class ViewerNotSameException : BadRequestException(message = "Viewer does not match.")
