package it.albertus.amazon.util;

import java.io.IOException;

public interface IPreferencesCallback {

	void reload() throws IOException;

	String getFileName();

}
