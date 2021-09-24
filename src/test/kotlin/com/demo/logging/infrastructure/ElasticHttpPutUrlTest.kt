package com.demo.logging.infrastructure

import com.demo.logging.infrastructure.Document
import com.demo.logging.infrastructure.ElasticHttpPostUrl
import com.demo.logging.infrastructure.HttpPutUrl
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class ElasticHttpPutUrlTest {

    private lateinit var httpPutUrl: HttpPutUrl

    @BeforeEach
    fun setUp() {
        httpPutUrl = ElasticHttpPostUrl("http://localhost:9200")
    }

    @Test
    fun `retrieves an url with host and document info`() {
        val document = `given a document`()
        val result = `when getting http put url`(document)
        `then the url was created with the document info`(result, document)
    }

    private fun `given a document`(): Document {
        return Document("Message")
            .withLog("info", "LoggerName")
            .withLabels(mapOf("application" to "application", "service" to "service"))
    }

    private fun `when getting http put url`(document: Document): String = httpPutUrl(document)

    private fun `then the url was created with the document info`(result: String, document: Document) {
        val application = document.labels["application"]
        val service = document.labels["service"]
        result `should be equal to` "http://localhost:9200/ecs-${document.log!!.level}-$application-$service-${today()}"
    }

    private fun today(): String {
        val instant: Instant = Instant.now()
        val today: Date = Date.from(instant)
        val formatter = SimpleDateFormat("YYYY-MM-dd")
        return formatter.format(today)
    }
}
