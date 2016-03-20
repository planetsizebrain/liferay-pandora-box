package be.planetsizebrain.common.logging;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.bridge.SLF4JBridgeHandler;

// http://www.janferko.me/
public class SLF4JBridgeListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		if (!SLF4JBridgeHandler.isInstalled()) {
			SLF4JBridgeHandler.removeHandlersForRootLogger();
			SLF4JBridgeHandler.install();
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		if (SLF4JBridgeHandler.isInstalled()) {
			SLF4JBridgeHandler.uninstall();
		}
	}
}