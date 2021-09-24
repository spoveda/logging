package com.demo.logging.core

import com.demo.logging.infrastructure.HttpRestLogger
import com.demo.logging.infrastructure.Config
import com.demo.logging.infrastructure.DefaultConfig
import com.demo.logging.infrastructure.OnlyStdoutLogger
import com.demo.logging.infrastructure.Slf4jLogger
import com.demo.logging.infrastructure.StdoutAndServerLogger
import org.slf4j.LoggerFactory

object LoggerFactory {
    private val restTemplate by lazy { HttpRestLogger() }

    @JvmStatic
    @JvmOverloads
    fun <T> getLogger(clazz: Class<T>, config: Config = DefaultConfig): Logger {
        val slf4jLogger = LoggerFactory.getLogger(clazz)
        if (config.serverLoggingEnabled) {
            return StdoutAndServerLogger(Slf4jLogger(slf4jLogger), restTemplate, config)
        }
        return OnlyStdoutLogger(Slf4jLogger(slf4jLogger), config)
    }
}
