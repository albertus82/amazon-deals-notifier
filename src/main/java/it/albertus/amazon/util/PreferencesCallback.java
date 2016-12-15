package it.albertus.amazon.util;

public abstract class PreferencesCallback implements IPreferencesCallback {

	private final String fileName;

	public PreferencesCallback(final String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

}
