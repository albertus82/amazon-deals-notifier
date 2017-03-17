package it.albertus.amazon.log;

import ch.qos.logback.classic.net.SMTPAppender;
import it.albertus.amazon.AmazonDealsNotifier;
import it.albertus.amazon.email.EmailSender;
import it.albertus.amazon.email.EmailSender.Defaults;
import it.albertus.util.Configuration;

public class SmtpAppender extends SMTPAppender {

	@Override
	public void start() {
		final Configuration configuration = AmazonDealsNotifier.configuration;
		final String host = configuration.getString(EmailSender.CFG_KEY_EMAIL_HOST);
		final String sender = configuration.getString(EmailSender.CFG_KEY_EMAIL_FROM_ADDRESS);
		final String[] recipients = configuration.getString("email.log.addresses", "").split(EmailSender.EMAIL_ADDRESSES_SPLIT_REGEX);

		if (host != null && !host.isEmpty() && sender != null && !sender.isEmpty() && recipients.length > 0 && recipients[0] != null && !recipients[0].isEmpty()) {
			// Host
			setSmtpHost(host);

			// SSL/TLS
			final boolean sslOnConnect = configuration.getBoolean(EmailSender.CFG_KEY_EMAIL_SSL_CONNECT, Defaults.SSL_CONNECT);
			setSSL(sslOnConnect);
			setSmtpPort(sslOnConnect ? configuration.getInt(EmailSender.CFG_KEY_EMAIL_SSL_PORT, Defaults.SSL_PORT) : configuration.getInt(EmailSender.CFG_KEY_EMAIL_PORT, Defaults.PORT));
			setSTARTTLS(sslOnConnect ? false : configuration.getBoolean(EmailSender.CFG_KEY_EMAIL_STARTTLS_ENABLED, Defaults.STARTTLS_ENABLED) || configuration.getBoolean(EmailSender.CFG_KEY_EMAIL_STARTTLS_REQUIRED, Defaults.STARTTLS_REQUIRED));

			// Authentication
			if (!configuration.getString(EmailSender.CFG_KEY_EMAIL_USERNAME, "").isEmpty() && !configuration.getString(EmailSender.CFG_KEY_EMAIL_PASSWORD, "").isEmpty()) {
				setUsername(configuration.getString(EmailSender.CFG_KEY_EMAIL_USERNAME));
				setPassword(configuration.getString(EmailSender.CFG_KEY_EMAIL_PASSWORD));
			}

			// Sender
			if (configuration.getString(EmailSender.CFG_KEY_EMAIL_FROM_NAME, "").isEmpty()) {
				setFrom(sender);
			}
			else {
				setFrom(configuration.getString(EmailSender.CFG_KEY_EMAIL_FROM_NAME) + " <" + sender + ">");
			}

			// Recipients
			for (final String recipient : recipients) {
				addTo(recipient);
			}
			super.start();
		}
	}

}
