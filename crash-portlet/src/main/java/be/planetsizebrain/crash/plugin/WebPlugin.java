package be.planetsizebrain.crash.plugin;

import java.util.Collections;

import javax.inject.Named;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;

@Named
public class WebPlugin extends CRaSHPlugin<WebPlugin> {

	public static final PropertyDescriptor<Boolean> ENABLED = new PropertyDescriptor<Boolean>(Boolean.class, "web.enabled", false, "The refresh time unit") {
		@Override
		public Boolean doParse(String s) {
			return Boolean.valueOf(s);
		}
	};

	@Override
	protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
		return Collections.<PropertyDescriptor<?>>singleton(ENABLED);
	}

	@Override
	public WebPlugin getImplementation() {
		return this;
	}
}