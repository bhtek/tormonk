package tormonk

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun logger(): ReadOnlyProperty<Any, Logger> = object : ReadOnlyProperty<Any, Logger> {
    private var logger: Logger? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        logger?.let { return it }

        val loggingClass = thisRef.javaClass.enclosingClass ?: thisRef.javaClass
        return LoggerFactory.getLogger(loggingClass).also { logger = it }
    }
}
