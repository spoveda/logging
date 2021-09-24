package com.demo.logging.infrastructure

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.demo.logging.core.Message
import com.demo.logging.infrastructure.Document
import com.demo.logging.infrastructure.Slf4jLogger
import com.demo.logging.infrastructure.StoutLogger
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger

class Slf4jLoggerTest {

    private lateinit var stoutLogger: StoutLogger
    private lateinit var logger: Logger
    private val loggerName = this::class.java.typeName

    @BeforeEach
    fun setUp() {
        logger = mock()
        When calling logger.isWarnEnabled itReturns true
        When calling logger.isInfoEnabled itReturns true
        stoutLogger = Slf4jLogger(logger)
    }

    @Test
    fun send_an_info_message() {
        val document = `given a document`("INFO")
        val infoMessage = `when sending the info message`(document)
        `then the info document was sent`(infoMessage)
    }

    @Test
    fun send_a_warn_message() {
        val document = `given a document`("WARN")
        val warn = `when sending the warn message`(document)
        `then the warn document was sent`(warn)
    }

    @Test
    fun send_an_error_message() {
        val document = `given a document`("ERROR")
        val error = `when sending the error message`(document)
        `then the error document was sent`(error)
    }

    @Test
    fun send_an_error_with_exception_message() {
        val (document, exception) = `given an error`()
        val error = `when sending the error message`(document, exception)
        `then the error (with exception) document was sent`(error, exception)
    }

    private fun `given an error`(): Pair<Document, Exception> {
        val document = `given a document`("ERROR")
        val exception = Exception("Some Exception!")
        document.withError(exception)
        return Pair(document, exception)
    }

    private fun `given a document`(level: String): Document {
        val event = Message.Event("action", listOf(), "module")
        return Document("Some message").withLog(level, loggerName).withEvent(event)
    }

    private fun `when sending the info message`(document: Document): String {
        val json = argumentCaptor<String> {
            doNothing().`when`(logger).info(capture())
            allValues
        }
        stoutLogger.info(document)
        return json.lastValue
    }

    private fun `when sending the warn message`(document: Document): String {
        val json = argumentCaptor<String> {
            doNothing().`when`(logger).warn(capture())
            allValues
        }
        stoutLogger.warn(document)
        return json.lastValue
    }

    private fun `when sending the error message`(document: Document, error: Exception? = null): String {
        val json = argumentCaptor<String> {
            error?.let {
                doNothing().`when`(logger).error(capture(), eq(error))
            } ?: doNothing().`when`(logger).error(capture())
            allValues
        }

        stoutLogger.error(document, error)

        return json.lastValue
    }

    private fun `then the info document was sent`(info: String) {
        Verify on logger that logger.info(eq(info)) was called
    }

    private fun `then the warn document was sent`(warn: String) {
        Verify on logger that logger.warn(eq(warn)) was called
    }

    private fun `then the error document was sent`(error: String) {
        Verify on logger that logger.error(eq(error)) was called
    }

    private fun `then the error (with exception) document was sent`(error: String, exception: Exception) {
        Verify on logger that logger.error(eq(error), eq(exception)) was called
    }
}
