package com.demo.logging.spring

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.fail

class UUIDGetCorrelationIdTest {

    private lateinit var getCorrelationId: GetCorrelationId

    @BeforeEach
    fun setUp() {
        getCorrelationId = UUIDGetCorrelationId
    }

    @Test
    fun `retrieve an uuid correlation id`() {
        val newCorrelationId = getCorrelationId()
        try {
            UUID.fromString(newCorrelationId)
        } catch (e: IllegalArgumentException) {
            fail("Invalid correlation id: $newCorrelationId")
        }
    }
}
