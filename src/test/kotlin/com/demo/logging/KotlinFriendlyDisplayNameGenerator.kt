package com.demo.logging

import org.apache.commons.lang3.StringUtils.join
import org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase
import org.junit.jupiter.api.DisplayNameGenerator
import java.lang.reflect.Method
import kotlin.reflect.jvm.kotlinFunction

class KotlinFriendlyDisplayNameGenerator : DisplayNameGenerator.Standard() {
    override fun generateDisplayNameForNestedClass(nestedClass: Class<*>): String {
        return nestedClass.kotlin.simpleName ?: super.generateDisplayNameForNestedClass(nestedClass)
    }

    override fun generateDisplayNameForMethod(testClass: Class<*>, testMethod: Method): String {
        return (testMethod.kotlinFunction?.name ?: super.generateDisplayNameForMethod(testClass, testMethod)).removeSuffix("()")
    }

    override fun generateDisplayNameForClass(testClass: Class<*>): String {
        val className = testClass.kotlin.simpleName ?: super.generateDisplayNameForClass(testClass)
        return join(splitByCharacterTypeCamelCase(className.replace("\\d+", "")), " ")
            .toLowerCase().replace(" test", " should").trim()
    }
}
