package be.planetsizebrain.crash.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.keyboard.KeyType;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellProcess;
import org.crsh.spring.SpringWebBootstrap;
import org.crsh.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import be.planetsizebrain.common.spring.BeanLocator;
import be.planetsizebrain.crash.plugin.WebPlugin;

public class WebsocketMessageInbound extends MessageInbound {

	private final Logger log = LoggerFactory.getLogger(WebsocketMessageInbound.class);

	private static final ConcurrentHashMap<String, CrashSession> sessions = new ConcurrentHashMap<>();

	private String id;
	private final Principal principal;
	private final String contextPath;

	public WebsocketMessageInbound(String id, Principal principal, String contextPath) {
		this.id = id;
		this.principal = principal;
		this.contextPath = contextPath;
	}

	@Override
	protected void onOpen(WsOutbound outbound) {
		log.info("Establishing session for {}", contextPath);

//		PluginContext context = WebPluginLifeCycle.getPluginContext(contextPath);
		SpringWebBootstrap springWebBootstrap = BeanLocator.getBean(SpringWebBootstrap.class);
		PluginContext context = springWebBootstrap.getContext();
		if (context != null) {
			Boolean enabled = context.getProperty(WebPlugin.ENABLED);
			if ((enabled != null) && (enabled.booleanValue())) {
				log.info("Using shell {}", context);
				ShellFactory factory = context.getPlugin(ShellFactory.class);
				Shell shell = factory.create(principal);
				CrashSession session = new CrashSession(outbound, shell);
				sessions.put(id, session);

				log.info("Established session {}", id);
			} else {
				log.info("Web plugin disabled");
			}
		} else {
			log.info("No shell found");
		}
	}

	@Override
	protected void onClose(int status) {
		CrashSession session = sessions.remove(id);
		if (session != null) {
			log.info("Destroying session {}", id);
			WSProcessContext current = session.current.getAndSet(null);
			if (current != null) {
				log.info("Cancelling on going command {} for {}", current.command, id);
				current.process.cancel();
			}
		} else {
			log.info("No shell session found");
		}
	}

	@Override
	protected void onTextMessage(CharBuffer charBuffer) throws IOException {
		String message = charBuffer.toString();

		String key = id;
		log.info("Received message {} from session {}", message, key);

		CrashSession session = sessions.get(key);
		if (session != null) {
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(message);
			if ((json instanceof JsonObject)) {
				JsonObject event = (JsonObject) json;
				JsonElement type = event.get("type");
				if (type.getAsString().equals("welcome")) {
					log.info("Sending welcome + prompt");
					session.send("print", session.shell.getWelcome());
					session.send("prompt", session.shell.getPrompt());
				} else if (type.getAsString().equals("execute")) {
					String command = event.get("command").getAsString();
					int width = event.get("width").getAsInt();
					int height = event.get("height").getAsInt();
					ShellProcess process = session.shell.createProcess(command);
					WSProcessContext context = new WSProcessContext(session, process, command, width, height);
					if (session.current.getAndSet(context) == null) {
						log.info(new StringBuilder().append("Executing \"").append(command).append("\"").toString());
						process.execute(context);
					} else {
						log.info(new StringBuilder().append("Could not execute \"").append(command).append("\"").toString());
					}
				} else if (type.getAsString().equals("cancel")) {
					WSProcessContext current = session.current.getAndSet(null);
					if (current != null) {
						log.info(new StringBuilder().append("Cancelling command \"").append(current.command).append("\"").toString());
						current.process.cancel();
					} else {
						log.info("No process to cancel");
					}
				} else if (type.getAsString().equals("key")) {
					WSProcessContext current = session.current.get();
					if (current != null) {
						String _keyType = event.get("keyType").getAsString();
						KeyType keyType = KeyType.valueOf(_keyType.toUpperCase());
						if (keyType == KeyType.CHARACTER) {
							int code = event.get("keyCode").getAsInt();
							if (code >= 32)
								current.handle(KeyType.CHARACTER, new int[]{code});
						} else {
							current.handle(keyType, new int[0]);
						}
					} else {
						log.info("No process can handle the key event");
					}
				} else if (type.getAsString().equals("complete")) {
					String prefix = event.get("prefix").getAsString();
					CompletionMatch completion = session.shell.complete(prefix);
					Completion completions = completion.getValue();
					Delimiter delimiter = completion.getDelimiter();
					StringBuilder sb = new StringBuilder();
					List values = new ArrayList();
					try {
						if (completions.getSize() == 1) {
							String value = completions.getValues().iterator().next();
							delimiter.escape(value, sb);
							if (completions.get(value).booleanValue()) {
								sb.append(delimiter.getValue());
							}
							values.add(sb.toString());
						} else {
							String commonCompletion = Utils.findLongestCommonPrefix(completions.getValues());
							if (commonCompletion.length() > 0) {
								delimiter.escape(commonCompletion, sb);
								values.add(sb.toString());
							} else {
								for (Map.Entry entry : completions) {
									delimiter.escape((CharSequence) entry.getKey(), sb);
									values.add(sb.toString());
									sb.setLength(0);
								}
							}
						}
					} catch (IOException ioe) {
						log.error("Unexpected IO exception", ioe);
					}
					log.info("Completing \"{}\" with ", prefix, values);
					session.send("complete", values);
				}
			}
		} else {
			log.info("No shell session found");
		}
	}

	@Override
	protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
		log.warn("Discarding binary message");
	}
}