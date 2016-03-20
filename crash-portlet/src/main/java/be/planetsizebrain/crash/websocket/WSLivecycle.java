package be.planetsizebrain.crash.websocket;

import org.crsh.plugin.WebPluginLifeCycle;

//@WebListener
public class WSLivecycle extends WebPluginLifeCycle {

	protected String getDefaultCmdMountPointConfig() {
		return super.getDefaultCmdMountPointConfig() + ";classpath:/crash/commands/";
	}

	protected String getDefaultConfMountPointConfig() {
		return super.getDefaultConfMountPointConfig() + ";classpath:/crash/";
	}
}