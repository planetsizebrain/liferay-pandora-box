package be.planetsizebrain.crash.websocket;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.catalina.websocket.WsOutbound;
import org.crsh.shell.Shell;

public class CrashSession {

	final WsOutbound wsOutbound;
	final Shell shell;
	final AtomicReference<WSProcessContext> current;

	CrashSession(WsOutbound wsOutbound, Shell shell) {
		this.wsOutbound = wsOutbound;
		this.shell = shell;
		this.current = new AtomicReference();
	}

	void send(String type) {
		send(type, null);
	}

	void send(String type, Object data) {
		send(new Event(type, data));
	}

	private void send(Event event) {
		try {
			this.wsOutbound.writeTextMessage(CharBuffer.wrap(event.toJSON()));
		} catch (IOException e) {
			// TODO: log
			e.printStackTrace();
		}
	}
}