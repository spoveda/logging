package com.demo.logging.infrastructure

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.demo.logging.TestConfig
import com.demo.logging.core.Message
import com.demo.logging.infrastructure.Config
import com.demo.logging.infrastructure.Document
import com.demo.logging.infrastructure.RestLogger
import com.demo.logging.infrastructure.StdoutAndServerLogger
import com.demo.logging.infrastructure.StoutLogger
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StdoutAndServerLoggerTest {

    private lateinit var stdoutAndServerLogger: StdoutAndServerLogger
    private lateinit var logger: StoutLogger
    private lateinit var restLogger: RestLogger
    private lateinit var config: Config

    @BeforeEach
    fun setUp() {
        logger = mock()
        When calling logger.name() itReturns "MockedStdoutLogger"
        restLogger = mock()
        config = TestConfig()
        stdoutAndServerLogger = StdoutAndServerLogger(logger, restLogger, config)
    }

    @Test
    fun `send a warn message`() {
        val document = argumentCaptor<Document> {
            doNothing().`when`(logger).warn(capture())
            allValues
        }
        val message = Message("Some message")
        stdoutAndServerLogger.warn(message)

        with(document.firstValue) {
            Verify on logger that logger.warn(eq(this)) was called
            Verify on restLogger that restLogger.invoke(eq(this)) was called
        }
    }

    @Test
    fun `send a info message`() {
        val document = argumentCaptor<Document> {
            doNothing().`when`(logger).info(capture())
            allValues
        }
        val message = Message("Some message")
        stdoutAndServerLogger.info(message)

        with(document.firstValue) {
            Verify on logger that logger.info(eq(this)) was called
            Verify on restLogger that restLogger.invoke(eq(this)) was called
        }
    }

    @Test
    fun `send an error message`() {
        val message = Message("Error message message")
        val error = Exception("Some Exception")
        message.withError(error)
        val document = argumentCaptor<Document> {
            doNothing().`when`(logger).error(capture(), eq(error))
            allValues
        }
        stdoutAndServerLogger.error(message)

        with(document.firstValue) {
            Verify on logger that logger.error(eq(this), eq(error)) was called
            Verify on restLogger that restLogger.invoke(eq(this)) was called
        }
    }

    @Test
    fun `send a request message`() {
        val document = argumentCaptor<Document> {
            doNothing().`when`(logger).info(capture())
            allValues
        }
        val message = Message("Some message").withHttpRequest("referer", "GET")

        stdoutAndServerLogger.request(message)

        with(document.firstValue) {
            Verify on logger that logger.info(eq(this)) was called
            Verify on restLogger that restLogger.invoke(eq(this)) was called
        }
    }

    @Test
    fun `send a response message`() {
        val document = argumentCaptor<Document> {
            doNothing().`when`(logger).info(capture())
            allValues
        }
        val message = Message("Some message").withHttpResponse(200)
        stdoutAndServerLogger.response(message)

        with(document.firstValue) {
            Verify on logger that logger.info(eq(this)) was called
            Verify on restLogger that restLogger.invoke(eq(this)) was called
        }
    }
}
