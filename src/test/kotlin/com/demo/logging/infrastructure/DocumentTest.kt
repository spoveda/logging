package com.demo.logging.infrastructure

import com.demo.logging.core.Message
import com.demo.logging.core.Message.HttpRequest
import com.demo.logging.infrastructure.Document
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.fail

class DocumentTest {

    private lateinit var document: Document
    private val httpRequest = HttpRequest("GET", "your body")
    private val httpResponse = Message.HttpResponse(200, "your body")

    @BeforeEach
    fun setUp() {
        document = Document("Some message")
    }

    @Nested
    @DisplayName("be built successfully with")
    inner class BeBuildADocument {
        @Test
        fun `a simple message`() {
            document.message `should be equal to` "Some message"
        }

        @Test
        fun `a base info`() {
            val tags = listOf("A", "B")
            document.withBase(tags)
            document.tags `should be equal to` tags
        }

        @Test
        fun `the log info`() {
            val loggerName = this::class.java.typeName
            document.withLog("INFO", loggerName)

            document.log?.run {
                level `should be equal to` "INFO"
                logger `should be equal to` loggerName
            } ?: "Log".failMessage()
        }

        @Test
        fun `with an error`() {
            val error = Exception("Some Error")
            document.withError(error)

            document.error?.run {
                message `should be equal to` "Some Error"
                stackTrace.`should not be null`()
                type `should be equal to` "java.lang.Exception"
            } ?: "Error".failMessage()
        }

        @Test
        fun `some labels`() {
            val labels = mapOf("key" to "value")
            document.withLabels(labels)

            document.labels `should be equal to` labels
        }

        @Test
        fun `event info`() {
            val categories = listOf("A")
            val event = Message.Event("action", categories, "module")
            document.withEvent(event)

            document.event?.run {
                action `should be equal to` "action"
                this.category `should be equal to` categories
                module `should be equal to` "module"
            } ?: "Event".failMessage()
        }

        @Test
        fun `a tracing info`() {
            val traceId = UUID.randomUUID().toString()
            document.withTracing(traceId)

            document.trace?.run {
                id `should be equal to` traceId
            } ?: "Tracing".failMessage()
        }
    }

    @Nested
    @DisplayName("be built successfully with http")
    inner class BeBuildADocumentWithHttp {
        @Test
        fun `request with body`() {
            document.withHttpRequest(httpRequest)

            with(document.http) {
                request?.run {
                    body?.run {
                        content `should be equal to` httpRequest.body
                    }
                    method `should be equal to` httpRequest.method
                }
            } ?: "Http Request".failMessage()
        }

        @Test
        fun `request without body`() {
            val httpRequest = HttpRequest(referrer = "/hello", method = "GET")
            document.withHttpRequest(httpRequest)

            with(document.http) {
                request?.run {
                    body.`should be null`()
                    method `should be equal to` httpRequest.method
                    referrer `should be equal to` httpRequest.referrer
                }
            } ?: "Http Request".failMessage()
        }

        @Test
        fun `response with body`() {
            document.withHttpResponse(httpResponse)

            with(document.http) {
                response?.run {
                    body?.run {
                        content `should be equal to` httpResponse.body
                    }
                    statusCode `should be equal to` httpResponse.statusCode
                }
            } ?: "Http Response".failMessage()
        }

        @Test
        fun `response without body`() {
            val httpResponse = Message.HttpResponse(statusCode = 200)
            document.withHttpResponse(httpResponse)

            with(document.http) {
                response?.run {
                    body.`should be null`()
                    statusCode `should be equal to` httpResponse.statusCode
                }
            } ?: "Http Response".failMessage()
        }

        @Test
        fun `request and response`() {
            document.withHttpRequest(httpRequest).withHttpResponse(httpResponse)

            with(document.http) {
                request.`should not be null`()
                response.`should not be null`()
            }
        }
    }

    private fun String.failMessage(): Unit = fail("$this field wasn't be initialized!")
}
