package be.planetsizebrain.crash.plugin;

import java.io.File;

import javax.inject.Named;

import org.crsh.plugin.CRaSHPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.util.PropsValues;

@Named
public class LiferayCrashDirectoryPlugin extends CRaSHPlugin<LiferayCrashDirectoryPlugin> {

	private static final String CRASH_DIRECTORY = PropsValues.LIFERAY_HOME + File.separator + "crash";

	private final Logger logger = LoggerFactory.getLogger(LiferayCrashDirectoryPlugin.class);

	@Override
	public void init() {
		File crashDirectory = new File(CRASH_DIRECTORY);
		if (crashDirectory.exists()) {
			logger.info("CRaSH directory {} already exists.", CRASH_DIRECTORY);
		} else {
			logger.info("CRaSH directory {} does not already exist, creating it...", CRASH_DIRECTORY);
			crashDirectory.mkdir();
		}
	}

	@Override
	public LiferayCrashDirectoryPlugin getImplementation() {
		return this;
	}
}