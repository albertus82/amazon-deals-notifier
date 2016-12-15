package it.albertus.amazon;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import it.albertus.amazon.email.EmailSender;
import it.albertus.amazon.job.NotifyJob;
import it.albertus.amazon.util.Configuration;

public class AmazonDealsNotifier {

	public static final Configuration configuration = new Configuration("application.properties");

	public static final void main(final String... args) throws SchedulerException {
		EmailSender.checkConfiguration();
		final JobDetail job = JobBuilder.newJob(NotifyJob.class).withIdentity("notifyJob").build();

		final Trigger trigger = TriggerBuilder.newTrigger().withIdentity("notifyTrigger").withSchedule(CronScheduleBuilder.cronSchedule(configuration.getProperties().getProperty("cron"))).build();

		final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
		scheduler.start();
		scheduler.scheduleJob(job, trigger);
	}

}
