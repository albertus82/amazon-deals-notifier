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

	private static final String EMAIL_ADDRESSES_SPLIT_REGEX = "[,;\\s]+";

	private static final String CFG_KEY_EMAIL_HOST = "email.host";
	private static final String CFG_KEY_EMAIL_USERNAME = "email.username";
	private static final String CFG_KEY_EMAIL_PASSWORD = "email.password";
	private static final String CFG_KEY_EMAIL_FROM_NAME = "email.from.name";
	private static final String CFG_KEY_EMAIL_FROM_ADDRESS = "email.from.address";
	private static final String CFG_KEY_EMAIL_TO_ADDRESSES = "email.to.addresses";
	private static final String CFG_KEY_EMAIL_CC_ADDRESSES = "email.cc.addresses";
	private static final String CFG_KEY_EMAIL_BCC_ADDRESSES = "email.bcc.addresses";

	private static final Configuration configuration = AmazonDealsNotifier.configuration;

	public static class Defaults {
		private static final int PORT = 25;
		private static final int SSL_PORT = 465;
		private static final boolean SSL_CONNECT = false;
		private static final boolean SSL_IDENTITY = false;
		private static final boolean STARTTLS_ENABLED = false;
		private static final boolean STARTTLS_REQUIRED = false;
		private static final int SOCKET_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;
		private static final int SOCKET_CONNECTION_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;

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
		email.setStartTLSEnabled(configuration.getBoolean("email.starttls.enabled", Defaults.STARTTLS_ENABLED));
		email.setStartTLSRequired(configuration.getBoolean("email.starttls.required", Defaults.STARTTLS_REQUIRED));
		email.setSSLCheckServerIdentity(configuration.getBoolean("email.ssl.identity", Defaults.SSL_IDENTITY));
		email.setSSLOnConnect(configuration.getBoolean("email.ssl.connect", Defaults.SSL_CONNECT));
		email.setSmtpPort(configuration.getInt("email.port", Defaults.PORT));
		email.setSslSmtpPort(Integer.toString(configuration.getInt("email.ssl.port", Defaults.SSL_PORT)));

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
