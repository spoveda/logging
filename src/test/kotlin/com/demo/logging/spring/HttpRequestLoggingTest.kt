package com.demo.logging.spring

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.Verify
import org.amshove.kluent.called
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.servlet.http.HttpServletRequest

class HttpRequestLoggingTest {

    private lateinit var httpRequestLogging: HttpRequestLogging
    private lateinit var httpRequestLogger: HttpRequestLogger

    @BeforeEach
    fun setUp() {
        httpRequestLogger = mock()
        httpRequestLogging = HttpRequestLogging(httpRequestLogger)
    }

    @Test
    fun `catches and log all`() {
        val request: HttpServletRequest = mock()

        `when the pre handle is executed`(request)
        `then the logger was called`(request)
    }

    private fun `when the pre handle is executed`(request: HttpServletRequest) {
        httpRequestLogging.afterRequest(request, "")
    }

    private fun `then the logger was called`(request: HttpServletRequest) {
        Verify on httpRequestLogger that httpRequestLogger(eq(request), eq(null)) was called
    }
}
