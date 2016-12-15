package it.albertus.amazon.email;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyEmail implements Serializable {

	private static final long serialVersionUID = -5241562797785414380L;

	private static final Logger logger = LoggerFactory.getLogger(NotifyEmail.class);

	protected final Date date;
	protected final String address;
	protected final String subject;
	protected final String message;
	protected final File[] attachments;

	public NotifyEmail(final String address, final String subject, final String message, final File[] attachments) {
		this.address = address;
		if (address == null || address.isEmpty()) {
			logger.warn("No address supplied");
		}
		if (message != null && !message.isEmpty()) {
			this.message = message;
		}
		else {
			throw new IllegalArgumentException("Invalid message supplied");
		}
		this.date = new Date();
		this.subject = subject != null ? subject.trim() : null;
		this.attachments = attachments;
	}

	public Date getDate() {
		return date;
	}

	public String getAddress() {
		return address;
	}

	public String getSubject() {
		return subject;
	}

	public String getMessage() {
		return message;
	}

	public File[] getAttachments() {
		return attachments;
	}

	@Override
	public String toString() {
		return "NotifyEmail [date=" + date + ", address=" + address + ", subject=" + subject + ", message=" + message + ", attachments=" + Arrays.toString(attachments) + "]";
	}

}
