package it.albertus.amazon.email;

import java.io.File;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import it.albertus.amazon.AmazonDealsNotifier;
import it.albertus.amazon.util.Configuration;
import it.albertus.amazon.util.ConfigurationException;
import it.albertus.amazon.util.Messages;

public class EmailSender {

	public static final String EMAIL_ADDRESSES_SPLIT_REGEX = "[,;\\s]+";

	public static final String CFG_KEY_EMAIL_HOST = "email.host";
	public static final String CFG_KEY_EMAIL_USERNAME = "email.username";
	public static final String CFG_KEY_EMAIL_PASSWORD = "email.password";
	public static final String CFG_KEY_EMAIL_FROM_NAME = "email.from.name";
	public static final String CFG_KEY_EMAIL_FROM_ADDRESS = "email.from.address";
	public static final String CFG_KEY_EMAIL_TO_ADDRESSES = "email.to.addresses";
	public static final String CFG_KEY_EMAIL_CC_ADDRESSES = "email.cc.addresses";
	public static final String CFG_KEY_EMAIL_BCC_ADDRESSES = "email.bcc.addresses";
	public static final String CFG_KEY_EMAIL_PORT = "email.port";
	public static final String CFG_KEY_EMAIL_SSL_CONNECT = "email.ssl.connect";
	public static final String CFG_KEY_EMAIL_SSL_PORT = "email.ssl.port";
	public static final String CFG_KEY_EMAIL_STARTTLS_ENABLED = "email.starttls.enabled";
	public static final String CFG_KEY_EMAIL_STARTTLS_REQUIRED = "email.starttls.required";

	private static final Configuration configuration = AmazonDealsNotifier.configuration;

	public static class Defaults {
		public static final int PORT = 25;
		public static final int SSL_PORT = 465;
		public static final boolean SSL_CONNECT = false;
		public static final boolean SSL_IDENTITY = false;
		public static final boolean STARTTLS_ENABLED = false;
		public static final boolean STARTTLS_REQUIRED = false;
		public static final int SOCKET_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;
		public static final int SOCKET_CONNECTION_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public String send(final NotifyEmail ne) throws EmailException {
		checkConfiguration();
		final Email email;
		if (ne.getAttachments() != null && ne.getAttachments().length > 0) {
			final MultiPartEmail multiPartEmail = new MultiPartEmail();
			for (final File attachment : ne.getAttachments()) {
				addAttachment(multiPartEmail, attachment);
			}
			email = multiPartEmail;
		}
		else {
			email = new SimpleEmail();
		}
		initializeEmail(email);
		createContents(email, ne);
		final String mimeMessageId = email.send();
		return mimeMessageId;
	}

	public static void checkConfiguration() {
		// Configuration check
		if (configuration.getString(CFG_KEY_EMAIL_HOST, "").isEmpty()) {
			throw new ConfigurationException(Messages.get("err.configuration.invalid", CFG_KEY_EMAIL_HOST) + ' ' + Messages.get("err.configuration.review", configuration.getFileName()), CFG_KEY_EMAIL_HOST);
		}
		if (configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS, "").isEmpty()) {
			throw new ConfigurationException(Messages.get("err.configuration.invalid", CFG_KEY_EMAIL_FROM_ADDRESS) + ' ' + Messages.get("err.configuration.review", configuration.getFileName()), CFG_KEY_EMAIL_FROM_ADDRESS);
		}
	}

	private void addAttachment(final MultiPartEmail email, final File attachment) throws EmailException {
		final EmailAttachment emailAttachment = new EmailAttachment();
		emailAttachment.setPath(attachment.getPath());
		emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
		emailAttachment.setDescription(attachment.getName());
		emailAttachment.setName(attachment.getName());
		email.attach(emailAttachment);
	}

	private void initializeEmail(final Email email) throws EmailException {
		email.setSocketConnectionTimeout(configuration.getInt("email.connection.timeout", Defaults.SOCKET_CONNECTION_TIMEOUT));
		email.setSocketTimeout(configuration.getInt("email.socket.timeout", Defaults.SOCKET_TIMEOUT));
		email.setStartTLSEnabled(configuration.getBoolean(CFG_KEY_EMAIL_STARTTLS_ENABLED, Defaults.STARTTLS_ENABLED));
		email.setStartTLSRequired(configuration.getBoolean(CFG_KEY_EMAIL_STARTTLS_REQUIRED, Defaults.STARTTLS_REQUIRED));
		email.setSSLCheckServerIdentity(configuration.getBoolean("email.ssl.identity", Defaults.SSL_IDENTITY));
		email.setSSLOnConnect(configuration.getBoolean(CFG_KEY_EMAIL_SSL_CONNECT, Defaults.SSL_CONNECT));
		email.setSmtpPort(configuration.getInt(CFG_KEY_EMAIL_PORT, Defaults.PORT));
		email.setSslSmtpPort(Integer.toString(configuration.getInt(CFG_KEY_EMAIL_SSL_PORT, Defaults.SSL_PORT)));

		email.setHostName(configuration.getString(CFG_KEY_EMAIL_HOST));

		// Authentication
		if (!configuration.getString(CFG_KEY_EMAIL_USERNAME, "").isEmpty() && !configuration.getString(CFG_KEY_EMAIL_PASSWORD, "").isEmpty()) {
			email.setAuthenticator(new DefaultAuthenticator(configuration.getString(CFG_KEY_EMAIL_USERNAME), configuration.getString(CFG_KEY_EMAIL_PASSWORD)));
		}

		// Sender
		if (configuration.getString(CFG_KEY_EMAIL_FROM_NAME, "").isEmpty()) {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS));
		}
		else {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS), configuration.getString(CFG_KEY_EMAIL_FROM_NAME));
		}

		if (!configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES, "").isEmpty()) {
			email.addCc(configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
		if (!configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES, "").isEmpty()) {
			email.addBcc(configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
	}

	private void createContents(final Email email, final NotifyEmail ne) throws EmailException {
		if (ne.getAddress() != null) {
			email.addTo(ne.getAddress());
		}
		else {
			final String[] addresses = configuration.getString(CFG_KEY_EMAIL_TO_ADDRESSES, "").split(EMAIL_ADDRESSES_SPLIT_REGEX);
			if (addresses.length > 0 && addresses[0] != null && !addresses[0].isEmpty()) {
				email.addTo(addresses);
			}
		}
		email.setSubject(ne.getSubject());
		email.setMsg(ne.getMessage());
	}

}
