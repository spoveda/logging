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
import org.springframework.http.HttpMethod
import java.util.*
import java.util.Collections.emptyEnumeration
import java.util.Collections.enumeration
import javax.servlet.http.HttpServletRequest
import kotlin.test.assertTrue

class DefaultHttpRequestLoggerTest {

    private lateinit var httpRequestLogger: HttpRequestLogger
    private lateinit var logger: Logger
    private val objectMapper: ObjectMapper = jacksonObjectMapper().setSerializationInclusion(NON_NULL)

    @BeforeEach
    fun setUp() {
        logger = mock()
        httpRequestLogger = DefaultHttpRequestLogger(logger)
    }

    @Test
    fun `logs a request without headers`() {
        val requestURI = "/customers/123"
        val body = """{ "name":"John", "age":30}"""
        val request: HttpServletRequest = `given a request`(requestURI)

        val loggedMessage = `when the logger is called`(request, body).lastValue

        `then the message attribute contains the uri`(loggedMessage, requestURI)
        `and the request logger was called`(loggedMessage)
        `and the message contains the right action-method-body`(loggedMessage).also {
            `and the message does not contains request headers`(it)
        }
    }

    @Test
    fun `logs a request with headers`() {
        val requestURI = "/customers/123"
        val id = UUID.randomUUID().toString()
        val headers = mapOf("X-CorrelationId" to id)
        val body = """{ "name":"John", "age":30}"""

        val request: HttpServletRequest = `given a request`(requestURI, headers)

        val loggedMessage = `when the logger is called`(request, body).lastValue

        `then the message attribute contains the uri`(loggedMessage, requestURI)
        `and the request logger was called`(loggedMessage)
        `and the message contains the right action-method-body`(loggedMessage).also {
            `and the message contains all request headers`(it, headers)
        }
    }

    @Test
    fun `logs a request with querystrings`() {
        val requestURI = "/customers/123"
        val queryString = "abc=123"
        val body = """{ "name":"John", "age":30}"""

        val request: HttpServletRequest = `given a request`(requestURI, queryString = queryString)

        val loggedMessage = `when the logger is called`(request, body).lastValue

        `then the message attribute contains the uri`(loggedMessage, requestURI)
        `and the request logger was called`(loggedMessage)
        `and the message contains the right action-method-body`(loggedMessage, queryString).also {
            `and the message does not contains request headers`(it)
        }
    }

    @Test
    fun `logs a request with querystrings without body`() {
        val requestURI = "/customers/123"
        val queryString = "abc=123"

        val request: HttpServletRequest = `given a request`(requestURI, queryString = queryString)

        val loggedMessage = `when the logger is called`(request).lastValue

        `then the message attribute contains the uri`(loggedMessage, requestURI)
        `and the request logger was called`(loggedMessage)
        `and the message contains the right action-method`(loggedMessage, queryString).also {
            `and the message does not contains request headers`(it)
        }
    }

    private fun `given a request`(
        requestURI: String,
        headers: Map<String, String> = emptyMap(),
        queryString: String? = null
    ): HttpServletRequest {
        val httpMethod = HttpMethod.POST
        val request: HttpServletRequest = mock()
        val headerNames: Enumeration<String> =
            if (headers.keys.isEmpty()) emptyEnumeration() else enumeration(headers.keys)
        When calling request.method itReturns httpMethod.toString()
        When calling request.requestURI itReturns requestURI
        When calling request.headerNames itReturns headerNames
        When calling request.queryString itReturns queryString

        headers.forEach {
            When calling request.getHeader(it.key) itReturns it.value
        }
        return request
    }

    private fun `when the logger is called`(
        request: HttpServletRequest,
        body: String? = null
    ): KArgumentCaptor<Message> {
        val loggedMessage = argumentCaptor<Message> {
            doNothing().`when`(logger).request(capture())
            allValues
        }
        httpRequestLogger(request, body)
        return loggedMessage
    }

    private fun `then the message attribute contains the uri`(message: Message, requestURI: String) {
        message.message `should be equal to` "$requestURI request"
    }

    private fun `and the message contains the right action-method-body`(
        message: Message,
        queryString: String = ""
    ): String {
        val jsonMessage = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message)
        assertTrue("the request.referrer was invalid") {
            var uri = "/customers/123"
            if (queryString.isNotBlank()) {
                uri += "?$queryString"
            }
            jsonMessage.contains(""""referrer" : "$uri"""")
        }
        assertTrue("the request.method attribute was invalid") {
            jsonMessage.contains(""""method" : "POST"""")
        }
        assertTrue("the request.body attribute was invalid") {
            jsonMessage.contains(""""body" : "{ \"name\":\"John\", \"age\":30}"""")
        }
        return jsonMessage
    }

    private fun `and the message contains the right action-method`(
        message: Message,
        queryString: String = ""
    ): String {
        val jsonMessage = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message)
        assertTrue("the request.referrer was invalid") {
            var uri = "/customers/123"
            if (queryString.isNotBlank()) {
                uri += "?$queryString"
            }
            jsonMessage.contains(""""referrer" : "$uri"""")
        }
        assertTrue("the request.method attribute was invalid") {
            jsonMessage.contains(""""method" : "POST"""")
        }
        return jsonMessage
    }

    private fun `and the request logger was called`(loggedMessage: Message) {
        Verify on logger that logger.request(eq(loggedMessage)) was called
    }

    private fun `and the message does not contains request headers`(jsonMessage: String) {
        assertTrue("then the request.headers were not empty") {
            jsonMessage.contains("\"headers\" : { }")
        }
    }

    private fun `and the message contains all request headers`(
        jsonMessage: String,
        expectedHeaders: Map<String, String>
    ) {
        assertTrue("then the request.headers are invalid") {
            val json = expectedHeaders.map { "\"${it.key}\" : \"${it.value}\"" }.joinToString(",")
            jsonMessage.contains(json)
        }
    }
}
