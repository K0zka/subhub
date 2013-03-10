package org.dictat.subhub.utils;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

/**
 * Job wrapper to schedule task at initialization. This class may be redundant
 * if ever the spring guys provide a simple mechanism for this.
 * 
 * @author kocka
 */
public class JobWrapper {
	public JobWrapper(TaskScheduler scheduler, Runnable task, Trigger trigger) {
		super();
		this.scheduler = scheduler;
		this.task = task;
		this.trigger = trigger;
	}

	final TaskScheduler scheduler;
	final Runnable task;
	final Trigger trigger;

	boolean enabled = true;

	public void init() {
		if(enabled) {
			scheduler.schedule(task, trigger);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	};
}
