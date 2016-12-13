package it.albertus.amazon;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfiguration {

	private final Properties properties;
	private final String fileName;

	public PropertiesConfiguration(final String propertiesFileName) {
		this.fileName = propertiesFileName;
		this.properties = new Properties();
		load();
	}

	public void reload() {
		load();
	}

	public Properties getProperties() {
		return properties;
	}

	protected void load() {
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(getFileName()));
			if (inputStream != null) {
				synchronized (properties) {
					try {
						properties.clear();
						properties.load(inputStream);
					}
					catch (final IOException ioe) {
						throw new RuntimeException(ioe);
					}
				}
			}
		}
		catch (final FileNotFoundException fnfe) {/* Ignore */}
		finally {
			try {
				inputStream.close();
			}
			catch (final Exception e) {/* Ignore */}
		}
	}

	public String getFileName() {
		return fileName;
	}

}
