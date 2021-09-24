package com.demo.logging.core

import java.lang.Exception

interface Logger {
    fun warn(message: Message)
    fun info(message: Message)
    fun error(message: Message)

    fun request(message: Message)
    fun response(message: Message)

    @Deprecated("Slf4j: retro compatibility!", ReplaceWith("warn(message: Message)"))
    fun warn(warn: String) = warn(Message(warn))
    @Deprecated("Slf4j: retro compatibility!", ReplaceWith("info(message: Message)"))
    fun info(info: String) = info(Message(info))
    @Deprecated("Slf4j: retro compatibility!", ReplaceWith("error(message: Message)"))
    fun error(error: String) = error(Message(error))
    @Deprecated("Slf4j: retro compatibility!", ReplaceWith("error(message: Message)"))
    fun error(error: String, exception: Exception) = error(Message(error).withError(exception))
}
