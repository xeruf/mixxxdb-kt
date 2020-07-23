package xerus.music.mixxx

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.spi.ContextAwareBase
import mu.KotlinLogging

var logLevel: Level = Level.DEBUG

val logger = KotlinLogging.logger { }

internal class LogbackConfigurator: ContextAwareBase(), Configurator {
	
	override fun configure(lc: LoggerContext) {
		
		val encoder = PatternLayoutEncoder().apply {
			context = lc
			pattern = "%d{HH:mm:ss} [%-25.25thread] %-5level  %-30logger{30} %msg%n"
			start()
		}
		
		val consoleAppender = ConsoleAppender<ILoggingEvent>().apply {
			name = "console"
			context = lc
			this.encoder = encoder
			addFilter(ThresholdFilter().apply {
				setLevel(logLevel.toString())
				start()
			})
			start()
		}
		
		val rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
		if(logLevel.levelInt < Level.DEBUG_INT)
			rootLogger.level = logLevel
		rootLogger.addAppender(consoleAppender)
	}
	
}
