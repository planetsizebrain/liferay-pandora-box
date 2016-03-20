package be.planetsizebrain.crash.websocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;

import org.crsh.keyboard.KeyHandler;
import org.crsh.keyboard.KeyType;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Color;
import org.crsh.text.Screenable;
import org.crsh.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSProcessContext implements ShellProcessContext, KeyHandler {

	private final Logger log = LoggerFactory.getLogger(WSProcessContext.class);

	final ShellProcess process;
	final CrashSession session;
	private StringBuilder buffer = new StringBuilder();
	final int width;
	final int height;
	final String command;
	private static final EnumMap<Color, String> COLOR_MAP = new EnumMap(Color.class);

	private Style style = Style.reset;

	public WSProcessContext(CrashSession session, ShellProcess process, String command, int width, int height) {
		this.session = session;
		this.process = process;
		this.width = width;
		this.height = height;
		this.command = command;
	}

	public void handle(KeyType type, int[] sequence) {
		KeyHandler keyHandler = this.process.getKeyHandler();
		if (keyHandler != null) {
			log.debug("Processing key event {} {}", type, Arrays.toString(sequence));
			try {
				keyHandler.handle(type, sequence);
			} catch (Exception e) {
				log.error("Processing key handler {} threw an exception", keyHandler, e);
			}
		}
	}

	public void end(ShellResponse response) {
		log.debug("Ended \"{}\"", this.command);
		this.session.current.compareAndSet(this, null);
		flush();
		String msg = response.getMessage();
		if (msg.length() > 0) {
			this.session.send("print", msg);
		}
		String prompt = this.session.shell.getPrompt();
		this.session.send("prompt", prompt);
		this.session.send("end");
	}

	public boolean takeAlternateBuffer() throws IOException {
		return false;
	}

	public boolean releaseAlternateBuffer() throws IOException {
		return false;
	}

	public String getProperty(String propertyName) {
		return null;
	}

	public String readLine(String msg, boolean echo) {
		return null;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public Appendable append(char c)
			throws IOException {
		return append(Character.toString(c));
	}

	public Appendable append(CharSequence s) throws IOException {
		return append(s, 0, s.length());
	}

	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		if (start < end) {
			if (this.style.equals(Style.reset)) {
				this.buffer.append(csq, start, end);
			} else {
				Style.Composite composite = (Style.Composite) this.style;
				this.buffer.append("[[");
				if (composite.getUnderline() == Boolean.TRUE) {
					this.buffer.append('u');
				}
				if (composite.getBold() == Boolean.TRUE) {
					this.buffer.append('b');
				}
				this.buffer.append(';');
				if (composite.getForeground() != null) {
					this.buffer.append(COLOR_MAP.get(composite.getForeground()));
				}
				this.buffer.append(';');
				if (composite.getBackground() != null) {
					this.buffer.append(COLOR_MAP.get(composite.getBackground()));
				}
				this.buffer.append(']');
				while (start < end) {
					char c = csq.charAt(start++);
					if (c == ']')
						this.buffer.append("\\]");
					else {
						this.buffer.append(c);
					}
				}
				this.buffer.append(']');
			}
		}
		return this;
	}

	public Screenable append(Style style) throws IOException {
		this.style = style.merge(style);
		return this;
	}

	public Screenable cls() throws IOException {
		this.buffer.append("\033[");
		this.buffer.append("2J");
		this.buffer.append("\033[");
		this.buffer.append("1;1H");
		return this;
	}

	public void flush() {
		if (this.buffer.length() > 0) {
			this.session.send("print", this.buffer.toString());
			this.buffer.setLength(0);
		}
	}

	static {
		COLOR_MAP.put(Color.black, "#000");
		COLOR_MAP.put(Color.blue, "#0000AA");
		COLOR_MAP.put(Color.cyan, "#00AAAA");
		COLOR_MAP.put(Color.green, "#00AA00");
		COLOR_MAP.put(Color.magenta, "#AA00AA");
		COLOR_MAP.put(Color.white, "#AAAAAA");
		COLOR_MAP.put(Color.yellow, "#AAAA00");
		COLOR_MAP.put(Color.red, "#AA0000");
	}
}