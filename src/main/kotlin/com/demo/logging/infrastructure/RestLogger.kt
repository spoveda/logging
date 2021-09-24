package com.demo.logging.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import java.net.URI.create
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.ofByteArray
import java.net.http.HttpResponse.BodyHandlers.discarding
import java.text.SimpleDateFormat
import java.time.Duration.ofMillis
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.util.*

internal interface RestLogger {
    operator fun invoke(document: Document)
}

internal class HttpRestLogger(
    private val client: HttpClient = httpClient,
    private val url: HttpPutUrl = ElasticHttpPostUrl(),
    private val restAuthorization: String = DefaultConfig.restAuthorization
) : RestLogger {
    private val applicationJson = "application/json"

    override fun invoke(document: Document) {
        val finalUrl = "${url(document)}/doc/"
        val request = prepareDocumentRequest(finalUrl, document)
        client.sendAsync(request, discarding())
    }

    private fun prepareDocumentRequest(finalUrl: String, document: Document): HttpRequest {
        val body = ofByteArray(document.toBody())
        return HttpRequest.newBuilder().timeout(ofMillis(100))
            .uri(create(finalUrl))
            .POST(body)
            .setHeader("Accept", applicationJson)
            .setHeader("Content-Type", applicationJson)
            .setHeader("Authorization", restAuthorization)
            .build()
    }

    private fun Document.toBody(): ByteArray = objectMapper.writeValueAsBytes(JsonDocument(this))

    internal data class JsonDocument(@get:JsonUnwrapped val document: Document) {
        @get:JsonProperty("@timestamp")
        val timestamp: String = ISO_INSTANT.format(Instant.now())
    }
}

internal interface HttpPutUrl {
    operator fun invoke(document: Document): String
}

internal class ElasticHttpPostUrl(private val host: String = DefaultConfig.host) : HttpPutUrl {
    override fun invoke(document: Document): String {
        val kind = document.log!!.level.toLowerCase()
        val application = document.labels["application"]
        val service = document.labels["service"]
        return "$host/ecs-$kind-$application-$service-${today()}"
    }

    private fun today(): String {
        val instant: Instant = Instant.now()
        val today: Date = Date.from(instant)
        val formatter = SimpleDateFormat("YYYY-MM-dd")
        return formatter.format(today)
    }
}
