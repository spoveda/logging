package com.demo.logging.spring

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse

class HttpResponseLoggingTest {

    private lateinit var httpResponseLogging: HttpResponseLogging
    private lateinit var httpResponseLogger: HttpResponseLogger

    @BeforeEach
    fun setUp() {
        httpResponseLogger = mock()
        httpResponseLogging = HttpResponseLogging(httpResponseLogger)
    }

    @Test
    fun `catches and log all`() {
        val body: Any? = mock()
        val returnType: MethodParameter = mock()
        val selectedContentType: MediaType = mock()
        val selectedConverterType: Class<out HttpMessageConverter<*>> = HttpMessageConverter::class.java
        val request: ServerHttpRequest = mock()
        val response: ServerHttpResponse = mock()

        val responseBody = `when before body write is executed`(
            body, returnType, selectedContentType,
            selectedConverterType, request, response
        )

        `then the logger was called`(body, returnType, selectedContentType, selectedConverterType, request, response)
        `and the response body was not changed`(body, responseBody)
    }

    private fun `when before body write is executed`(body: Any?, returnType: MethodParameter, selectedContentType: MediaType, selectedConverterType: Class<out HttpMessageConverter<*>>, request: ServerHttpRequest, response: ServerHttpResponse): Any? {
        return httpResponseLogging.beforeBodyWrite(
            body, returnType, selectedContentType,
            selectedConverterType, request, response
        )
    }

    private fun `then the logger was called`(body: Any?, returnType: MethodParameter, selectedContentType: MediaType, selectedConverterType: Class<out HttpMessageConverter<*>>, request: ServerHttpRequest, response: ServerHttpResponse) {
        Verify on httpResponseLogger that httpResponseLogger(
            eq(body), eq(returnType), eq(selectedContentType), eq(selectedConverterType),
            eq(request), eq(response)
        ) was called
    }

    private fun `and the response body was not changed`(body: Any?, responseBody: Any?) {
        body `should be equal to` responseBody
    }
}
