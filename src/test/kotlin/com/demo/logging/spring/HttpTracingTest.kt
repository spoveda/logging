package com.demo.logging.spring

import com.nhaarman.mockitokotlin2.mock
import com.demo.logging.infrastructure.CORRELATION_ID_REQUEST_HEADER
import com.demo.logging.infrastructure.CORRELATION_ID_RESPONSE_HEADER
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpTracingTest {

    private lateinit var httpTracing: HttpTracing
    private lateinit var getCorrelationId: GetCorrelationId

    @BeforeEach
    fun setUp() {
        getCorrelationId = mock()
        httpTracing = HttpTracing(listOf(), getCorrelationId)
    }

    @Test
    fun `add a new correlation id into header response`() {
        val response: HttpServletResponse = mock()
        val request: HttpServletRequest = `given a request without correlation id`()
        val correlationId = `given a new correlation id`()

        `when request filtered`(request, response)

        `then the response contains a new correlation id`(response, correlationId)
    }

    @Test
    fun `add request correlation id into header response`() {
        val response: HttpServletResponse = mock()
        val request: HttpServletRequest = `given a request with correlation id`()
        val correlationId = request.getHeader(CORRELATION_ID_REQUEST_HEADER)

        `when request filtered`(request, response)

        `then the response contains a new correlation id`(response, correlationId)
        VerifyNoInteractions on getCorrelationId
    }

    private fun `given a request with correlation id`(): HttpServletRequest {
        val request: HttpServletRequest = mock()
        When calling request.requestURI itReturns "/mockUrl"
        When calling request.getHeader(CORRELATION_ID_REQUEST_HEADER) itReturns UUID.randomUUID().toString()
        return request
    }

    private fun `given a request without correlation id`(): HttpServletRequest {
        val request: HttpServletRequest = mock()
        When calling request.requestURI itReturns "/mockUrl"
        return request
    }

    private fun `given a new correlation id`(): String {
        val correlationId = UUID.randomUUID().toString()
        When calling getCorrelationId() itReturns correlationId
        return correlationId
    }

    private fun `when request filtered`(request: HttpServletRequest, response: HttpServletResponse) {
        httpTracing.doFilter(request, response, mock())
    }

    private fun `then the response contains a new correlation id`(
        response: HttpServletResponse,
        correlationId: String
    ) {
        Verify on response that response.addHeader(CORRELATION_ID_RESPONSE_HEADER, correlationId) was called
    }
}
