package socktuator.config;

import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TaskSchedConf {
	
	private static ThreadPoolTaskScheduler instance;

	public static synchronized ThreadPoolTaskScheduler get() {
		if (instance==null) {
			instance = new TaskSchedulerBuilder()
					.threadNamePrefix("socktuator")
					.poolSize(10)
					.build();
			instance.setDaemon(true);
			instance.initialize();
		}
		return instance;
	}

}
