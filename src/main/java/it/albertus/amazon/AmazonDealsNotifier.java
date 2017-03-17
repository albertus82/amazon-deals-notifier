package it.albertus.amazon;

import java.io.IOException;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.albertus.amazon.email.EmailSender;
import it.albertus.amazon.job.NotifyJob;
import it.albertus.amazon.util.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.Version;

public class AmazonDealsNotifier {

	public static final Configuration configuration;

	private static final Logger logger = LoggerFactory.getLogger(AmazonDealsNotifier.class);

	private AmazonDealsNotifier() {
		throw new IllegalAccessError();
	}

	static {
		try {
			configuration = new Configuration("amazon-deals-notifier.cfg");
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final void main(final String... args) throws SchedulerException {
		logger.info(Messages.get("msg.application.name") + ' ' + Version.getInstance().getNumber());

		EmailSender.checkConfiguration();

		final JobDetail job = JobBuilder.newJob(NotifyJob.class).withIdentity("notifyJob").build();

		final Trigger trigger = TriggerBuilder.newTrigger().withIdentity("notifyTrigger").withSchedule(CronScheduleBuilder.cronSchedule(configuration.getProperties().getProperty("cron"))).build();

		final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
		scheduler.start();
		scheduler.scheduleJob(job, trigger);
	}

}
