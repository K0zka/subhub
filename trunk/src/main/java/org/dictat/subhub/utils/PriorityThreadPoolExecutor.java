package org.dictat.subhub.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dictat.subhub.beans.services.Prioritized;

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

	static class PrioritizedFutureTask<T> extends FutureTask<T> implements
			Comparable<T> {
		
		private final Object job;

		public PrioritizedFutureTask(Callable<T> callable) {
			super(callable);
			this.job = callable;
		}

		public PrioritizedFutureTask(Runnable runnable, T result) {
			super(runnable, result);
			this.job = runnable;
		}

		@Override
		public int compareTo(T other) {
			return (int) (getPriority(job) - getPriority(other));
		}

		static double getPriority(Object job) {
			if(job instanceof Prioritized) {
				return ((Prioritized)job).getPriority();
			} else if (job instanceof PrioritizedFutureTask) {
				return getPriority(((PrioritizedFutureTask<?>)job).job);
			}
			return 0;
		}
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new PrioritizedFutureTask<T>(runnable, value);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new PrioritizedFutureTask<T>(callable);
	}

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
	}

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				handler);
	}

	public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
	}

}
