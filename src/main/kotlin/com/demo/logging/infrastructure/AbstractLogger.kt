package com.demo.logging.infrastructure

import com.demo.logging.core.Logger
import com.demo.logging.core.Message
import org.slf4j.MDC

abstract class AbstractLogger(
    private val defaultLogger: StoutLogger,
    private val defaultConfig: Config
) : Logger {

    internal fun Message.toDocument(level: String): Document {
        return Document(message)
            .withBase(tags)
            .withLog(level, defaultLogger.name())
            .withTracing(traceId ?: MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
            .withEvent(event)
            .withError(exception)
            .withLabels(labels())
            .withHttpRequest(request?.replaceBlacklistedHeadersValues())
            .withHttpResponse(response)
    }

    private fun labels(): Map<String, String> {
        val labels = mutableMapOf<String, String>()

        labels.putAll(
            mapOf(
                "application" to defaultConfig.applicationName.toKebabCase(),
                "service" to defaultConfig.serviceName.toKebabCase(),
                "environment" to defaultConfig.environment,
                "lib_version" to defaultConfig.libVersion,
                "lib_language" to "java/kotlin"
            )
        )

        defaultConfig.nodeName?.run { labels["node_name"] to this }
        defaultConfig.podName?.run { labels["pod_name"] to this }

        return labels
    }

    private fun String.toKebabCase(): String = toLowerCase().replace("\\s+".toRegex(), "-")

    private fun Message.HttpRequest.replaceBlacklistedHeadersValues(): Message.HttpRequest {
        val filterBlacklistedHeaders = this.headers.mapValues {
            if (defaultConfig.blackListedHeaders.any { header -> header.equals(it.key, ignoreCase = true) }) {
                return@mapValues "secret"
            } else {
                it.value
            }
        }
        return this.copy(headers = filterBlacklistedHeaders)
    }
}
