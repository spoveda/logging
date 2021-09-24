package com.demo.logging.core

data class Message(val message: String) {
    internal var tags: List<String>? = null
        private set
    internal var exception: Exception? = null
        private set
    internal var traceId: String? = null
        private set
    internal var event: Event? = null
        private set
    internal var request: HttpRequest? = null
        private set
    internal var response: HttpResponse? = null
        private set

    fun withTags(tags: List<String>): Message {
        this.tags = tags
        return this
    }

    fun withError(exception: Exception): Message {
        this.exception = exception
        return this
    }

    fun withEvent(action: String, category: List<String>, module: String? = null, type: String? = null): Message {
        this.event = Event(action, category, module, type)
        return this
    }

    fun withTracing(traceId: String): Message {
        this.traceId = traceId
        return this
    }

    fun withHttpRequest(referrer: String, method: String, body: String? = null, headers: Map<String, String> = emptyMap()): Message {
        this.request = HttpRequest(referrer, method, body, headers)
        return this
    }

    fun withHttpResponse(statusCode: Int, body: String? = null): Message {
        this.response = HttpResponse(statusCode, body)
        return this
    }

    data class Event(
        val action: String,
        val categories: List<String> = emptyList(),
        val module: String? = null,
        val type: String? = null
    )

    data class HttpRequest(val referrer: String, val method: String, val body: String? = null, val headers: Map<String, String> = emptyMap())
    data class HttpResponse(val statusCode: Int, val body: String? = null)
}
