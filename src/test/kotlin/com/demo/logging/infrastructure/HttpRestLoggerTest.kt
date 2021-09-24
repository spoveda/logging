package com.demo.logging.infrastructure

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.demo.logging.infrastructure.Document
import com.demo.logging.infrastructure.HttpPutUrl
import com.demo.logging.infrastructure.HttpRestLogger
import com.demo.logging.infrastructure.RestLogger
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.discarding
import java.util.concurrent.CompletableFuture

class HttpRestLoggerTest {

    private lateinit var restLogger: RestLogger
    private lateinit var httpClient: HttpClient
    private val loggerName = this::class.java.typeName
    private lateinit var httpPutUrl: HttpPutUrl

    @BeforeEach
    fun setUp() {
        httpPutUrl = mock()
        httpClient = mock()
        When calling httpPutUrl(any()) itReturns "http://localhost:9200/ecs-info-application-service-2020-10-27"
        restLogger = HttpRestLogger(httpClient, httpPutUrl, "password")
    }

    @Test
    fun `publish a new document`() {
        val document = `given a document`()
        val requestEntity = `given a request entity`()
        `when post a new document`(document)
        `then the document was sent`(requestEntity)
    }

    private fun `given a document`(): Document {
        return Document("Some message")
            .withLog("INFO", loggerName)
            .withLabels(mapOf("application" to "application", "service" to "service"))
    }

    private fun `given a request entity`(): KArgumentCaptor<HttpRequest> {
        val response = mock<CompletableFuture<HttpResponse<Void>>>()
        return argumentCaptor {
            When calling httpClient.sendAsync(capture(), eq(discarding())) itReturns response
            allValues
        }
    }

    private fun `when post a new document`(document: Document) {
        restLogger(document)
    }

    private fun `then the document was sent`(requestEntity: KArgumentCaptor<HttpRequest>) {
        Verify on httpClient that httpClient.sendAsync(eq(requestEntity.lastValue), eq(discarding())) was called
    }
}
