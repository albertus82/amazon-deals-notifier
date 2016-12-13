package it.albertus.amazon;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(NotifyJob.class);

	private static final String URLS_FILE_NAME = "urls.txt";

	private final PropertiesConfiguration configuration = AmazonDealsNotifier.configuration;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		final File urlsFile = new File(URLS_FILE_NAME);

		final Set<String> urls = new HashSet<>();
		try (final BufferedReader br = new BufferedReader(new FileReader(urlsFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				final String trimmed = line.trim();
				if (!trimmed.isEmpty()) {
					urls.add(trimmed);
				}
			}
		}
		catch (final IOException ioe) {
			throw new RuntimeException(ioe);
		}

		HttpURLConnection.setFollowRedirects(true);
		for (final String url : urls) {
			logger.info("Connecting to: {}", url);
			try {
				final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				conn.setRequestMethod("GET");
				conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.01; WiPOSTndows NT 5.0)");
				conn.addRequestProperty("Accept", "*/*");
				conn.addRequestProperty("Accept-Encoding", "gzip");
				final String responseContentEncoding = conn.getHeaderField("Content-Encoding");
				final boolean gzip = responseContentEncoding != null && responseContentEncoding.toLowerCase().contains("gzip");
				try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(); final InputStream is = gzip ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream()) {
					IOUtils.copy(is, baos);
					logger.info("Response size: {} bytes", baos.size());
					if (baos.toString("UTF-8").contains("priceblock_dealprice")) {
						logger.warn("Deal! {}", url);
						sendMail(url);
					}
					else {
						logger.debug("No deal for {}", url);
					}
				}
				catch (final EmailException ee) {
					logger.error("Cannot send email", ee);
				}
			}
			catch (final IOException ioe) {
				logger.error("Skipped URL: " + url, ioe);
			}
			try {
				Thread.sleep(2000);
			}
			catch (final InterruptedException ie) {
				break;
			}
		}
	}

	private void sendMail(final String url) throws EmailException {
		final Properties configuration = this.configuration.getProperties();
		final Email email = new SimpleEmail();
		email.setHostName(configuration.getProperty("email.hostname"));
		email.setSmtpPort(Integer.parseInt(configuration.getProperty("email.port")));
		email.setAuthenticator(new DefaultAuthenticator(configuration.getProperty("email.username"), configuration.getProperty("email.password")));
		email.setSSLOnConnect(Boolean.parseBoolean(configuration.getProperty("email.ssl")));
		email.setFrom(configuration.getProperty("email.from"));
		email.setSubject("Amazon deal");
		email.setMsg(url);
		email.addTo(configuration.getProperty("email.to"));
		email.send();
		logger.debug("Email sent");
	}

}
