package com.demo.logging.spring

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.demo.logging.core.Logger
import com.demo.logging.core.Message
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import java.net.URI
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertTrue

class DefaultHttpResponseLoggerTest {

    private val requestUri = "/customers/123"
    private lateinit var httpResponseLogger: HttpResponseLogger
    private lateinit var logger: Logger
    private val objectMapper: ObjectMapper = jacksonObjectMapper().setSerializationInclusion(NON_NULL)

    @BeforeEach
    fun setUp() {
        logger = mock()
        httpResponseLogger = DefaultHttpResponseLogger(listOf(), logger)
    }

    @Test
    fun `logs a response`() {
        val body = """{ "name":"John", "age":30}"""
        val returnType: MethodParameter = mock()
        val selectedContentType: MediaType = mock()
        val selectedConverterType: Class<out HttpMessageConverter<*>> = HttpMessageConverter::class.java
        val httpRequest: ServletServerHttpRequest = `given a server http request`()
        val response: ServletServerHttpResponse = `given a servlet http response`()
        val message = argumentCaptor<Message> {
            doNothing().`when`(logger).response(capture())
            allValues
        }

        httpResponseLogger(body, returnType, selectedContentType, selectedConverterType, httpRequest, response)

        val loggedMessage = `then the message attribute contains the uri`(message, httpRequest)
        `and the message contains the right action-method-body`(loggedMessage)
        `and the response logger was called`(loggedMessage)
    }

    private fun `and the response logger was called`(loggedMessage: Message) {
        Verify on logger that logger.response(eq(loggedMessage)) was called
    }

    private fun `then the message attribute contains the uri`(message: KArgumentCaptor<Message>, httpRequest: ServerHttpRequest): Message {
        val loggedMessage = message.lastValue
        loggedMessage.message `should be equal to` httpRequest.uri.path + " response"
        return loggedMessage
    }

    private fun `given a servlet http response`(): ServletServerHttpResponse {
        val response: ServletServerHttpResponse = mock()
        val servletResponse: HttpServletResponse = mock()
        When calling servletResponse.status itReturns HttpStatus.OK.value()
        When calling response.servletResponse itReturns servletResponse
        return response
    }

    private fun `given a server http request`(): ServletServerHttpRequest {
        val servletServerHttpRequest: ServletServerHttpRequest = mock()
        val httpServletRequest: HttpServletRequest = mock()
        When calling httpServletRequest.requestURI itReturns requestUri
        When calling servletServerHttpRequest.uri itReturns URI.create(requestUri)
        When calling servletServerHttpRequest.servletRequest itReturns httpServletRequest
        return servletServerHttpRequest
    }

    private fun `and the message contains the right action-method-body`(message: Message): String {
        val jsonMessage = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message)

        assertTrue("the response.status attribute was invalid") {
            jsonMessage.contains(""""statusCode" : 200""")
        }
        assertTrue("the response.body attribute was invalid") {
            jsonMessage.contains(""""body" : "\"{ \\\"name\\\":\\\"John\\\", \\\"age\\\":30}\""""")
        }
        return jsonMessage
    }
}
