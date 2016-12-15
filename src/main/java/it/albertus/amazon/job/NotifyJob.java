package it.albertus.amazon.job;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.EmailException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.albertus.amazon.AmazonDealsNotifier;
import it.albertus.amazon.email.EmailSender;
import it.albertus.amazon.email.NotifyEmail;
import it.albertus.util.Configuration;
import it.albertus.util.ThreadUtils;

public class NotifyJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(NotifyJob.class);
	private static final Configuration configuration = AmazonDealsNotifier.configuration;
	private static final EmailSender emailSender = new EmailSender();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Job started at {}", new Date());
		final File urlsFile = new File(configuration.getString("urls.filename"));

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

		for (final String url : urls) {
			logger.info("Connecting to: {}", url);
			try {
				final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
				conn.addRequestProperty("Accept", "*/*");
				conn.addRequestProperty("Accept-Encoding", "gzip");
				final String responseContentEncoding = conn.getHeaderField("Content-Encoding");
				final boolean gzip = responseContentEncoding != null && responseContentEncoding.toLowerCase().contains("gzip");
				try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(); final InputStream is = gzip ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream()) {
					IOUtils.copy(is, baos);
					logger.debug("Response size: {} bytes", baos.size());
					if (baos.toString("UTF-8").contains("priceblock_dealprice")) {
						logger.warn("Deal! {}", url);
						emailSender.send(new NotifyEmail("Amazon deal", url, null));
						logger.debug("Email sent.");
					}
					else {
						logger.info("No deal for {}", url);
					}
				}
				catch (final EmailException ee) {
					logger.error("Cannot send email", ee);
				}
			}
			catch (final IOException ioe) {
				logger.error("Skipped URL: " + url, ioe);
			}
			if (ThreadUtils.sleep(2500) != null) {
				break;
			}
		}
		logger.info("Job completed at {}", new Date());
	}

}
