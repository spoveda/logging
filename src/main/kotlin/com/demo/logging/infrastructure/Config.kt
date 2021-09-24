package com.demo.logging.infrastructure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.lang.System.getenv
import java.net.http.HttpClient
import java.time.Duration
import java.util.*

interface Config {
    val applicationName: String
    val serviceName: String
    val environment: String
    val host: String
    val podName: String?
    val nodeName: String?
    val serverLoggingEnabled: Boolean
    val libVersion: String
    val blackListedHeaders: List<String>
    val restAuthorization: String
}

internal object DefaultConfig : Config {
    override val applicationName: String = "APPLICATION_NAME".orElseThrow()
    override val serviceName: String = "LOGGING_SERVICE_NAME".orElseThrow()
    override val environment: String = "ENVIRONMENT".orElseThrow()
    override val host: String = "LOGGING_HOST".orElse("http://localhost:9200")
    override val podName: String? = getenv("MY_POD_NAME")
    override val nodeName: String? = getenv("MY_NODE_NAME")
    override val serverLoggingEnabled: Boolean = "SERVER_LOGGING_ENABLED".orElse("false").toBoolean()
    override val libVersion: String = getVersionNumber()

    override val blackListedHeaders: List<String> = listOf(
        "X-Authorization", "Authorization"
    ).map { it.toLowerCase() }

    override val restAuthorization: String
        get() {
            val authenticationUser = "LOGGING_AUTHENTICATION_USER".orElseThrow()
            val authenticationPassword = "LOGGING_AUTHENTICATION_PASSWORD".orElseThrow()
            return "Basic " + Base64.getEncoder().encodeToString("$authenticationUser:$authenticationPassword".toByteArray())
        }

    private fun getVersionNumber(): String {
        val reader = MavenXpp3Reader()
        val model: Model = if (File("pom.xml").exists()) reader.read(FileReader("pom.xml"))
        else reader.read(
            InputStreamReader(
                this.javaClass.getResourceAsStream(
                    "/META-INF/maven/com.demo/logging/pom.xml"
                )
            )
        )
        return if (model.version != null) model.version else model.parent.version
    }
}

const val DEFAULT_MDC_UUID_TOKEN_KEY = "LogMDCFilter.UUID"
const val CORRELATION_ID_RESPONSE_HEADER = "X-CorrelationId"
const val CORRELATION_ID_REQUEST_HEADER = "X-CorrelationId"

internal val httpClient: HttpClient = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_2)
    .followRedirects(HttpClient.Redirect.NEVER)
    .connectTimeout(Duration.ofMillis(100))
    .build()

internal val objectMapper: ObjectMapper = ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

private fun String.orElse(default: String): String = getenv(this).takeUnless { it.isNullOrEmpty() } ?: default
private fun String.orElseThrow(): String = getenv(this).takeUnless { it.isNullOrEmpty() }
    ?: throw IllegalArgumentException("$this is not defined!")
private fun String.toBoolean(): Boolean = this == "true"
