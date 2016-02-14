package be.planetsizebrain.fakesmtp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts a FakeSMTP Swing application instance:
 * 		- https://nilhcem.github.io/FakeSMTP/
 * 		- https://github.com/Nilhcem/FakeSMTP
 *
 * FakeSMTP is build on SubEtha SMTP: https://github.com/voodoodyne/subethasmtp
 * We could've build a web GUI to display the sent emails, but as FakeSMTP already has a
 * nice Swing GUI we'll just use that.
 *
 * Another option could be JavaMail Extension: https://github.com/m-szalik/javamail/wiki
 * This could write the emails to file which we could then visualize in the web interface.
 *
 * FakeSMTP options:
 * usage: java -jar fakeSMTP-2.0.jar [OPTION]...
 *	-a,--bind-address <arg>    IP address or hostname to bind to. Binds to
 *	                           all local IP addresses if not specified. Only
 *	                           works together with the -b (--background)
 *	                           argument.
 *	-b,--background            If specified, does not start the GUI. Must be
 *	                           used with the -s (--start-server) argument
 *	-e,--eml-viewer <arg>      Executable of program used for viewing emails
 *	-m,--memory-mode           Disable the persistence in order to avoid the
 *	                           overhead that it adds
 *	-o,--output-dir <arg>      Emails output directory
 *	-p,--port <arg>            SMTP port number
 *	-r,--relay-domains <arg>   Comma separated email domain(s) for which
 *	                           relay is accepted. If not specified, relays to
 *	                           any domain. If specified, relays only emails
 *	                           matching these domain(s), dropping (not
 *	                           saving) others
 *	-s,--start-server          Automatically starts the SMTP server at launch
 */
public class StartFakeSMTPListener implements ServletContextListener {

	private final Logger log = LoggerFactory.getLogger(StartFakeSMTPListener.class);

	private Process process;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			// Is this maybe an option to use when implementing via a StartupAction instead of a listener
			// http://hiralbarott.blogspot.be/2013/02/get-servletcontext-inside-scheduler-in.html
			String pathToLib = sce.getServletContext().getRealPath("/WEB-INF/lib/fakesmtp-2.0.jar");

			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.redirectErrorStream(true);
			// http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
			processBuilder.inheritIO();
			// http://stackoverflow.com/questions/15178937/processbuilder-works-starting-jar-but-does-not-acknolwledge-jars-arguments
			processBuilder.command("java",  "-jar", pathToLib, "--start-server", "--port", "2525", "--bind-address", "127.0.0.1", "--memory-mode");

			process = processBuilder.start();
		} catch (Exception e) {
			log.error("Unexpected problem starting FakeSMTP", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (process.isAlive()) {
			process.destroy();
		}
	}
}