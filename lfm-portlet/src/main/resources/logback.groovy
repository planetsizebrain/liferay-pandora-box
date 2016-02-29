import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.jul.LevelChangePropagator
import ch.qos.logback.core.FileAppender
import org.slf4j.bridge.SLF4JBridgeHandler

// Performance speedup for redirected JUL loggers
// See also: http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
// http://mailman.qos.ch/pipermail/logback-user/2012-February/003010.html
def lcp = new LevelChangePropagator()
lcp.context = context
lcp.resetJUL = true
context.addListener(lcp)

// Needed only for the JUL bridge: http://stackoverflow.com/a/9117188/1915920
// http://stackoverflow.com/questions/9117030/jul-to-slf4j-bridge
java.util.logging.LogManager.getLogManager().reset()
SLF4JBridgeHandler.removeHandlersForRootLogger()
SLF4JBridgeHandler.install()
java.util.logging.Logger.getLogger( "global" ).setLevel( java.util.logging.Level.FINEST )

def currentDay = timestamp("yyyy-MM-dd")

appender("LIFERAY", FileAppender) {
	file = "../../logs/liferay.${currentDay}.log"
	append = true
	encoder(PatternLayoutEncoder) {
		pattern = "%d{dd/MM HH:mm:ss} [%thread] %-5level %logger - %msg%n"
	}
}

//logger("be.aca.liferay.angular.portlet", DEBUG)

// Reduce various logging levels that are only needed to debug problems
//logger("org.crsh", DEBUG)
//logger("be.planetsizebrain.portlet.crash.WebsocketMessageInbound", ERROR)

root(INFO, ["LIFERAY"])