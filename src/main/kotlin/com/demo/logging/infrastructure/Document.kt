package com.demo.logging.infrastructure

import com.demo.logging.core.Message

typealias Labels = Map<String, String>
typealias Headers = List<String>
typealias Tags = List<String>
typealias Categories = List<String>

// https://www.elastic.co/guide/en/ecs/current/ecs-field-reference.html
data class Document(val message: String) {
    var tags: Tags? = null
        private set
    var trace: Trace? = null
        private set
    var event: Event? = null
        private set
    var error: Error? = null
        private set
    var log: Log? = null
        private set
    var labels: Labels = emptyMap()
        private set
    var http: Http = Http()
        private set

    fun withEvent(messageEvent: Message.Event?): Document {
        event = messageEvent?.run {
            Event(action, categories, module, type)
        }
        return this
    }

    fun withError(exception: Exception?): Document {
        exception?.run {
            val stackTrace = exception.stackTrace.joinToString(System.lineSeparator())
            error = Error(message, stackTrace, this::class.java.typeName)
        }
        return this
    }

    fun withBase(tags: Tags?): Document {
        this.tags = tags
        return this
    }

    fun withTracing(traceId: String?): Document {
        traceId?.run {
            trace = Trace(traceId)
        }
        return this
    }

    fun withLog(level: String, loggerName: String): Document {
        this.log = Log(level, loggerName)
        return this
    }

    fun withLabels(labels: Labels): Document {
        this.labels = labels
        return this
    }

    fun withHttpRequest(request: Message.HttpRequest?): Document {
        request?.run {
            val body = this.body?.ifEmpty { null }?.run { Http.Body(this) }
            http = http.copy(request = Http.Request(referrer, method, body, headers.map { "${it.key}=${it.value}" }))
        }
        return this
    }

    fun withHttpResponse(response: Message.HttpResponse?): Document {
        response?.run {
            val body = this.body?.ifEmpty { null }?.run { Http.Body(this) }
            http = http.copy(response = Http.Response(statusCode, body))
        }
        return this
    }

    data class Log(val level: String, val logger: String)
    data class Event(
        val action: String? = null,
        val category: Categories? = null,
        val module: String? = null,
        val type: String? = null
    )

    data class Error(val message: String?, val stackTrace: String, val type: String?)
    data class Trace(val id: String)

    data class Http(val request: Request? = null, val response: Response? = null) {
        data class Body(val content: String)
        data class Request(
            val referrer: String,
            val method: String,
            val body: Body? = null,
            val headers: Headers? = emptyList()
        )

        data class Response(val statusCode: Int, val body: Body? = null)
    }
}
