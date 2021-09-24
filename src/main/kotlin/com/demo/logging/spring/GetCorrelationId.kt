package com.demo.logging.spring

import java.util.*

interface GetCorrelationId {
    operator fun invoke(): String
}

object UUIDGetCorrelationId : GetCorrelationId {
    override fun invoke(): String = UUID.randomUUID().toString()
}
