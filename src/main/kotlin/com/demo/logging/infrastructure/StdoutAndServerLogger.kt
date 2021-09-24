package com.demo.logging.infrastructure

import com.demo.logging.core.Message

internal class StdoutAndServerLogger(
    private val logger: StoutLogger,
    private val restLogger: RestLogger,
    config: Config
) : AbstractLogger(logger, config) {

    override fun warn(message: Message) = message.send("WARN", logger::warn)
    override fun info(message: Message) = message.send("INFO", logger::info)
    override fun error(message: Message) = message.send("ERROR", logger::error)
    override fun request(message: Message) = message.send("REQUEST", logger::info)
    override fun response(message: Message) = message.send("RESPONSE", logger::info)

    private fun Message.send(level: String, stoutLogger: (document: Document) -> Unit) {
        val document = toDocument(level)
        stoutLogger(document).also { restLogger(document) }
    }

    private fun Message.send(level: String, stoutLogger: (document: Document, exception: Exception?) -> Unit) {
        val document = toDocument(level)
        stoutLogger(document, exception).also { restLogger(document) }
    }
}
