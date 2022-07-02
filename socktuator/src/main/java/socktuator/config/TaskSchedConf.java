package socktuator.config;

import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TaskSchedConf {
	
	private static ThreadPoolTaskScheduler instance;

	public static synchronized ThreadPoolTaskScheduler get() {
		if (instance==null) {
			instance = new TaskSchedulerBuilder()
					.threadNamePrefix("socktuator")
					.poolSize(8)
					.build();
// Replace 'build' with the below. 
//					.configure(new ThreadPoolTaskScheduler() {
//						@Override
//						protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory,
//								RejectedExecutionHandler rejectedExecutionHandler) {
//							ScheduledThreadPoolExecutor it = (ScheduledThreadPoolExecutor) super.createExecutor(poolSize, threadFactory, rejectedExecutionHandler);
//							it.allowCoreThreadTimeOut(true);
//							return it;
//						}
//					});
			instance.setDaemon(true);
			instance.initialize();
		}
		return instance;
	}

}
