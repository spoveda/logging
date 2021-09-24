package com.demo.logging.core

import com.demo.logging.core.Message.Event
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class MessageTest {

    private lateinit var message: Message

    private val simpleMessage = "Some String"
    private val action = "check account status"
    private val categories = listOf("ACCOUNTS")
    private val module = "accounts-api"
    private val transactionId = UUID.randomUUID().toString()
    private val tags = listOf("ACCOUNTS")

    @BeforeEach
    fun setUp() {
        message = Message(simpleMessage)
    }

    @Nested
    @DisplayName("be built successfully with")
    inner class BeBuiltSuccessfully {
        @Test
        fun `a simple text`() {
            message.message `should be equal to` simpleMessage
        }

        @Test
        fun `an event`() {
            message.withEvent(action, categories, module)

            message.event?.run {
                this `should be equal to` Event(action, categories, module)
            }
        }

        @Test
        fun `an error`() {
            val exception = Exception("Some Error!")
            message.withError(exception)

            message.exception?.run {
                this `should be equal to` exception
            }
        }

        @Test
        fun `a tracing info`() {
            message.withTracing(transactionId)
            message.traceId?.run {
                this `should be equal to` transactionId
            }
        }

        @Test
        fun `some tags`() {
            message.withTags(tags)
            message.tags `should be equal to` tags
        }

        @Test
        fun `a http request info`() {
            message.withHttpRequest("GET", "body")
            message.request `should be equal to` Message.HttpRequest("GET", "body")
        }

        @Test
        fun `a http response info`() {
            message.withHttpResponse(200, "body")
            message.response `should be equal to` Message.HttpResponse(200, "body")
        }
    }
}
