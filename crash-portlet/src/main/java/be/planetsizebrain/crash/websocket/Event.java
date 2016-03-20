package be.planetsizebrain.crash.websocket;

import com.google.gson.Gson;

public class Event {

	static final Gson gson = new Gson();
	final String type;
	final Object data;

	Event(String type) {
		this(type, null);
	}

	Event(String type, Object data) {
		this.type = type;
		this.data = data;
	}

	public String toJSON() {
		return gson.toJson(this);
	}
}