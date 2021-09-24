package com.demo.logging.infrastructure

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger

interface StoutLogger {
    fun info(document: Document)
    fun error(document: Document, exception: Exception?)
    fun warn(document: Document)
    fun name(): String
}

internal class Slf4jLogger(private val logger: Logger) : StoutLogger {

    private val objectMapper = ObjectMapper().setSerializationInclusion(NON_NULL)

    override fun info(document: Document) {
        if (logger.isInfoEnabled) logger.info(objectMapper.writeValueAsString(document))
    }

    override fun warn(document: Document) {
        if (logger.isWarnEnabled) logger.warn(objectMapper.writeValueAsString(document))
    }

    override fun error(document: Document, exception: Exception?) {
        with(document.message) {
            exception?.let { logger.error(this, it) } ?: logger.error(this)
        }
    }

    override fun name(): String = logger.name
}
