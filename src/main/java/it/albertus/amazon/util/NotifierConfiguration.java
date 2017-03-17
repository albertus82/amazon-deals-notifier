package it.albertus.amazon.util;

import java.io.IOException;

import it.albertus.util.Configuration;

public class NotifierConfiguration extends Configuration {

	private static NotifierConfiguration configuration;

	private NotifierConfiguration() throws IOException {
		super("amazon-deals-notifier.cfg");
	}

	public static synchronized Configuration getInstance() {
		if (configuration == null) {
			try {
				configuration = new NotifierConfiguration();
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
		return configuration;
	}

}
