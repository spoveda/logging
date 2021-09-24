package com.demo.logging.spring

import com.demo.logging.core.Logger
import com.demo.logging.core.LoggerFactory
import com.demo.logging.core.Message
import javax.servlet.http.HttpServletRequest

interface HttpRequestLogger {
    operator fun invoke(request: HttpServletRequest, body: String?)
}

class DefaultHttpRequestLogger(
    private val logger: Logger = LoggerFactory.getLogger(DefaultHttpRequestLogger::class.java)
) : HttpRequestLogger {

    override operator fun invoke(request: HttpServletRequest, body: String?) {
        val message = Message("${request.requestURI} request")
        val referrer = request.getUri()
        message.withHttpRequest(referrer, request.method, body, request.allHeaders())
        logger.request(message)
    }
}

fun HttpServletRequest.getUri(): String {
    if (!queryString.isNullOrEmpty()) {
        return "$requestURI?$queryString"
    }
    return requestURI
}

fun HttpServletRequest.allHeaders(): Map<String, String> {
    val headers = mutableMapOf<String, String>()
    val asIterator = this.headerNames.asIterator()
    while (asIterator.hasNext()) {
        val headerName = asIterator.next()
        headers[headerName] = this.getHeader(headerName)
    }
    return headers
}
