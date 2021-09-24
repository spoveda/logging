package com.demo.logging.spring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.demo.logging.core.Logger
import com.demo.logging.core.LoggerFactory
import com.demo.logging.core.Message
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse

interface HttpResponseLogger {
    operator fun invoke(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    )
}

class DefaultHttpResponseLogger(
    private val exclusionUriList: List<String> = listOf(),
    private val logger: Logger = LoggerFactory.getLogger(DefaultHttpResponseLogger::class.java)
) : HttpResponseLogger {
    private val objectMapper = jacksonObjectMapper()

    override fun invoke(body: Any?, returnType: MethodParameter, selectedContentType: MediaType, selectedConverterType: Class<out HttpMessageConverter<*>>, request: ServerHttpRequest, response: ServerHttpResponse) {
        if (exclusionUriList.none { request.uri.path.contains(it) }) {
            val message = Message(request.uri.path + " response")
            val statusCode = getResponseStatus(response)
            message.withHttpResponse(statusCode, body.toJsonString())

            logger.response(message)
        }
    }

    private fun getResponseStatus(response: ServerHttpResponse): Int {
        val res: ServletServerHttpResponse = response as ServletServerHttpResponse
        return res.servletResponse.status
    }

    private fun Any?.toJsonString(): String? {
        return this?.run {
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
            } catch (e: Exception) {
                return this.toString()
            }
        }
    }
}
