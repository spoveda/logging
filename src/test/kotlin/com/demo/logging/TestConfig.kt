package com.demo.logging

import com.demo.logging.infrastructure.Config

class TestConfig(private val serverLoggingStatus: Boolean = false) : Config {
    override val applicationName: String
        get() = "app"
    override val serviceName: String
        get() = "service"
    override val environment: String
        get() = "test"
    override val host: String
        get() = "http://127.0.0.1"
    override val podName: String
        get() = "pod name"
    override val nodeName: String
        get() = "node name"
    override val libVersion: String
        get() = "1.0.0"
    override val serverLoggingEnabled: Boolean
        get() = serverLoggingStatus
    override val blackListedHeaders: List<String>
        get() = listOf("X-Authorization", "Authorization")
    override val restAuthorization: String
        get() = "password"
}
