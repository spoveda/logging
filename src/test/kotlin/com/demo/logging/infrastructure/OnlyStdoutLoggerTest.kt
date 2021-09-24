package com.demo.logging.infrastructure

import com.demo.logging.TestConfig
import com.demo.logging.core.Logger
import com.demo.logging.core.Message
import com.demo.logging.infrastructure.Document
import com.demo.logging.infrastructure.OnlyStdoutLogger
import com.demo.logging.infrastructure.RestLogger
import com.demo.logging.infrastructure.StoutLogger
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.System.lineSeparator
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("only stdout logger should log ")
class OnlyStdoutLoggerTest {

    private lateinit var logger: Logger
    private lateinit var stoutLogger: StoutLogger
    private lateinit var restLogger: RestLogger
    private val loggerName = this::class.java.typeName

    @BeforeEach
    fun setUp() {
        stoutLogger = mock()
        When calling stoutLogger.name() itReturns loggerName
        restLogger = mock()
        logger = OnlyStdoutLogger(stoutLogger, TestConfig())
    }

    @Test
    fun `a warn message`() {
        val message = `given a message`("Warn")
        val document = `when log a warn message`(message)
        `then the document contains an expected warn message`(document, message)
        `and the all warn logging producers were notified`(document.lastValue)
        `and the document always contains the default labels`(document.lastValue)
    }

    @Test
    fun `an info message`() {
        val message = `given a message`("Info")
        val document = `when log an info message`(message)
        `then the document contains an expected message`(document, message, "INFO")
        `and the all info logging producers were notified`(document.lastValue)
        `and the document always contains the default labels`(document.lastValue)
    }

    @Test
    fun `an error message`() {
        val message = `given a message`("Error")
        val exception = Exception("Unexpected Error!")
        message.withError(exception)
        val document = `when log an error message`(message)
        `then the document contains an expected error message`(document, exception, message)
        `and the all error logging producers were notified`(document.lastValue, exception)
        `and the document always contains the default labels`(document.lastValue)
    }

    @Test
    fun `a request message`() {
        val message = `given a message`("Request")
        val document = `when log a request message`(message)
        `then the document contains an expected message`(document, message, "REQUEST")
        `and the all info logging producers were notified`(document.lastValue)
        `and the document always contains the default labels`(document.lastValue)
    }

    @Test
    fun `a request message with enabled headers`() {
        val message = `given a message`("Request")
        message.withHttpRequest(
            "/index", "GET", "body",
            mapOf("x-correlationId" to "UUID", "x-authorization" to "JWT")
        )
        val document = `when log a request message`(message)
        `and the request document info contains a valid headers`(document.lastValue, "x-correlationId=UUID")
        TestConfig().blackListedHeaders.forEach {
            `and the request document info no contains an invalid headers`(document.lastValue, it)
        }
    }

    @Test
    fun `a response message`() {
        val message = `given a message`("Response")
        val document = `when log a response message`(message)
        `then the document contains an expected message`(document, message, "RESPONSE")
        `and the all info logging producers were notified`(document.lastValue)
        `and the document always contains the default labels`(document.lastValue)
    }

    private fun `given a message`(levelPrefix: String): Message = Message("$levelPrefix Message")

    private fun `when log a warn message`(warnMessage: Message): KArgumentCaptor<Document> {
        val document = argumentCaptor<Document> {
            doNothing().`when`(stoutLogger).warn(capture())
            allValues
        }
        logger.warn(warnMessage)
        return document
    }

    private fun `when log an info message`(warnMessage: Message): KArgumentCaptor<Document> {
        val document = argumentCaptor<Document> {
            doNothing().`when`(stoutLogger).info(capture())
            allValues
        }
        logger.info(warnMessage)
        return document
    }

    private fun `when log a request message`(warnMessage: Message): KArgumentCaptor<Document> {
        val document = argumentCaptor<Document> {
            doNothing().`when`(stoutLogger).info(capture())
            allValues
        }
        logger.request(warnMessage)
        return document
    }

    private fun `when log a response message`(warnMessage: Message): KArgumentCaptor<Document> {
        val document = argumentCaptor<Document> {
            doNothing().`when`(stoutLogger).info(capture())
            allValues
        }
        logger.response(warnMessage)
        return document
    }

    private fun `when log an error message`(errorMessage: Message): KArgumentCaptor<Document> {
        val document = argumentCaptor<Document> {
            doNothing().`when`(stoutLogger).error(capture(), any())
            allValues
        }
        logger.error(errorMessage)
        return document
    }

    private fun `then the document contains an expected warn message`(document: KArgumentCaptor<Document>, warnMessage: Message) {
        with(document.lastValue) {
            this.error.`should be null`()
            this.message `should be equal to` warnMessage.message
            with(log!!) {
                this.logger `should be equal to` loggerName
                this.level `should be equal to` "WARN"
            }
        }
    }

    private fun `then the document contains an expected message`(document: KArgumentCaptor<Document>, infoMessage: Message, expectedLevel: String) {
        with(document.lastValue) {
            this.error.`should be null`()
            this.message `should be equal to` infoMessage.message
            with(log!!) {
                this.logger `should be equal to` loggerName
                this.level `should be equal to` expectedLevel
            }
        }
    }

    private fun `and the all warn logging producers were notified`(document: Document) {
        inOrder(stoutLogger, restLogger) {
            Verify on stoutLogger that stoutLogger.warn(document) was called
            // Verify on restLogger that restLogger(document) was called
        }
    }

    private fun `and the all info logging producers were notified`(document: Document) {
        inOrder(stoutLogger, restLogger) {
            Verify on stoutLogger that stoutLogger.info(document) was called
            // Verify on restLogger that restLogger(document) was called
        }
    }

    private fun `and the all error logging producers were notified`(document: Document, exception: Exception) {
        inOrder(stoutLogger, restLogger) {
            Verify on stoutLogger that stoutLogger.error(document, exception) was called
            // Verify on restLogger that restLogger(document) was called
        }
    }

    private fun `then the document contains an expected error message`(
        document: KArgumentCaptor<Document>,
        exception: Exception,
        errorMessage: Message
    ) {

        val expectedExceptionStackTrace = exception.stackTrace.joinToString(lineSeparator())
        with(document.lastValue) {
            error.`should not be null`()
            message `should be equal to` errorMessage.message
            error!!.stackTrace `should be equal to` expectedExceptionStackTrace
            with(log!!) {
                this.logger `should be equal to` loggerName
                this.level `should be equal to` "ERROR"
            }
        }
    }

    private fun `and the document always contains the default labels`(document: Document) {
        with(document.labels) {
            assertTrue(this.containsKey("application"))
            assertTrue(this.containsKey("service"))
        }
    }

    private fun `and the request document info no contains an invalid headers`(lastValue: Document, headerName: String) {
        assertFalse {
            lastValue.http.request?.headers?.contains(headerName)!!
        }
    }

    private fun `and the request document info contains a valid headers`(lastValue: Document, headerName: String) {
        assertTrue {
            lastValue.http.request?.headers?.contains(headerName)!!
        }
    }
}
