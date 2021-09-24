package com.demo.logging.spring

import org.springframework.web.filter.AbstractRequestLoggingFilter
import javax.servlet.http.HttpServletRequest

class HttpRequestLogging(
    private val requestLogger: HttpRequestLogger = DefaultHttpRequestLogger(),
    private val exclusionUriList: List<String> = emptyList()
) : AbstractRequestLoggingFilter() {

    init {
        super.setIncludeHeaders(true)
        super.setIncludePayload(true)
    }

    override fun shouldLog(request: HttpServletRequest): Boolean {
        return exclusionUriList.none { request.requestURI.contains(it) }
    }

    override fun beforeRequest(request: HttpServletRequest, message: String) = Unit

    public override fun afterRequest(request: HttpServletRequest, unusedMessage: String) {
        val body = super.getMessagePayload(request)
        requestLogger(request, body)
    }
}
