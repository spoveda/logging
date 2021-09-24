package com.demo.logging.core

import com.demo.logging.TestConfig
import com.demo.logging.core.LoggerFactory.getLogger
import com.demo.logging.infrastructure.OnlyStdoutLogger
import com.demo.logging.infrastructure.StdoutAndServerLogger
import org.junit.jupiter.api.Test

class LoggerFactoryTest {

    @Test
    fun `retrieves stdout logger`() {
        val logger: Logger = getLogger(LoggerFactoryTest::class.java)
        assert(logger is OnlyStdoutLogger)
    }

    @Test
    fun `retrieves stdout and server logger`() {
        val logger: Logger = getLogger(LoggerFactoryTest::class.java, TestConfig(serverLoggingStatus = true))
        assert(logger is StdoutAndServerLogger)
    }

    @Test
    fun `send a deprecated warn message to stdout`() {
        val logger: Logger = getLogger(LoggerFactoryTest::class.java)
        logger.warn("asdsad")
    }

    @Test
    fun `send a deprecated an info message to stdout`() {
        val logger: Logger = getLogger(LoggerFactoryTest::class.java)
        logger.info("asdsad")
    }

    @Test
    fun `send a deprecated an error message to stdout`() {
        val logger: Logger = getLogger(LoggerFactoryTest::class.java)
        logger.error("asdsad")
    }

    @Test
    fun `send a deprecated an error message with exception to stdout`() {
        val logger: Logger = getLogger(LoggerFactoryTest::class.java)
        logger.error("asdsad", Exception("Some Exception"))
    }
}
