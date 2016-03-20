package be.planetsizebrain.crash.websocket;

import java.security.Principal;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

// TODO: check user session and set threadlocals?
public class CrashWebsocketServlet extends WebSocketServlet {

	private static final String DEFAULT_CRASH_PORTLET_CONTEXT = "/crash-portlet";
	private static final String PORTLET_CONTEXT_KEY = "portlet-context";

	private String portletContext = DEFAULT_CRASH_PORTLET_CONTEXT;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		portletContext = config.getInitParameter(PORTLET_CONTEXT_KEY);
	}

	@Override
	protected StreamInbound createWebSocketInbound(String message, HttpServletRequest request) {
		String id = UUID.randomUUID().toString();
		Principal principal = request.getUserPrincipal();

		return new WebsocketMessageInbound(id, principal, portletContext);
	}
}