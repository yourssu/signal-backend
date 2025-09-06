package com.yourssu.signal.domain.viewer.implement.exception

import com.yourssu.signal.handler.BadRequestException

class ViewerNotSameException : BadRequestException(message = "Viewer does not match.")
