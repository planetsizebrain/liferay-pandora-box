package be.planetsizebrain.common.security;

import java.security.Security;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

// https://stackoverflow.com/questions/10379799/bouncycastle-cannot-load-when-i-redeploy-application
public class BouncyCastleListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();

		String name = bouncyCastleProvider.getName();
        Security.removeProvider(name);

        Security.addProvider(bouncyCastleProvider);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}
}