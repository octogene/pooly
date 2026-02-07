package dev.octogene.pooly.server.config

import ch.qos.logback.core.PropertyDefinerBase
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs
import sun.util.logging.resources.logging

/**
 * Used by logback to retrieve properties to be used in the logging configuration.
 */
class LoggingPropertiesDefiner : PropertyDefinerBase() {

    private val properties = mutableMapOf<String, String>()

    @Suppress("MemberVisibilityCanBePrivate")
    var propertyLookupKey = ""

    init {
        setLoggingLevel()
    }

    override fun getPropertyValue(): String {
        return properties.getValue(propertyLookupKey)
    }

    private fun setLoggingLevel() {
        val config = ApplicationConfig("application.yaml")
            .property("app")
            .getAs<AppConfig>()
        val logging = config.loglevel
        properties["rootLevel"] = logging
    }
}