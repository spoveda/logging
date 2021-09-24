package com.demo.logging.infrastructure

import com.demo.logging.infrastructure.Document
import com.demo.logging.infrastructure.HttpRestLogger
import com.demo.logging.infrastructure.objectMapper
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

class DocumentToJsonDocumentTest {

    @Test
    fun `add a timestamp`() {
        val document = Document("Message").withLog("info", "JsonDocumentSerializerShould")
        val jsonDocument = HttpRestLogger.JsonDocument(document)
        val json = objectMapper.writeValueAsString(jsonDocument)

        json `should be equal to` "{\"log\":{\"level\":\"${jsonDocument.document.log!!.level}\"," +
            "\"logger\":\"${jsonDocument.document.log!!.logger}\"},\"labels\":{}," +
            "\"http\":{},\"message\":\"${jsonDocument.document.message}\",\"@timestamp\":\"${jsonDocument.timestamp}\"}"
    }
}
