package it.albertus.amazon;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class AmazonDealsNotifier {

	public static final PropertiesConfiguration configuration = new PropertiesConfiguration("application.properties");

	public static final void main(final String... args) throws SchedulerException {
		final JobDetail job = JobBuilder.newJob(NotifyJob.class).withIdentity("notifyJob").build();

		final Trigger trigger = TriggerBuilder.newTrigger().withIdentity("notifyTrigger").withSchedule(CronScheduleBuilder.cronSchedule("0/15 * * * * ?")).build();

		final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
		scheduler.start();
		scheduler.scheduleJob(job, trigger);
	}

}
