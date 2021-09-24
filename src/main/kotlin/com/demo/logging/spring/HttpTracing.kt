package com.demo.logging.spring

import com.demo.logging.infrastructure.CORRELATION_ID_REQUEST_HEADER
import com.demo.logging.infrastructure.CORRELATION_ID_RESPONSE_HEADER
import com.demo.logging.infrastructure.DEFAULT_MDC_UUID_TOKEN_KEY
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpTracing(
    private val shouldNotFilterUris: List<String>,
    private val getCorrelationId: GetCorrelationId = UUIDGetCorrelationId
) : OncePerRequestFilter() {

    @Throws(java.io.IOException::class, ServletException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        try {
            val token = getCorrelationIdFromHeader(request)
            MDC.put(DEFAULT_MDC_UUID_TOKEN_KEY, token)
            response.addHeader(CORRELATION_ID_RESPONSE_HEADER, token)
            chain.doFilter(request, response)
        } finally {
            MDC.remove(DEFAULT_MDC_UUID_TOKEN_KEY)
        }
    }

    override fun isAsyncDispatch(request: HttpServletRequest): Boolean {
        return false
    }

    override fun shouldNotFilterErrorDispatch(): Boolean {
        return false
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return shouldNotFilterUris.any { request.requestURI.contains(it) }
    }

    private fun getCorrelationIdFromHeader(request: HttpServletRequest): String? {
        var correlationId = request.getHeader(CORRELATION_ID_REQUEST_HEADER)
        if (correlationId.isNullOrBlank()) {
            correlationId = getCorrelationId()
        }
        return correlationId
    }
}
