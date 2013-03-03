package org.dictat.subhub.beans.services;

import java.util.concurrent.ExecutorService;

public abstract class AbstractJob implements Runnable {
	public AbstractJob(SubscriptionRepository repository,
			ExecutorService executor) {
		super();
		this.repository = repository;
		this.executor = executor;
	}
	final protected SubscriptionRepository repository;
	final protected ExecutorService executor;

}
