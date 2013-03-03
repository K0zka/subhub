package org.dictat.subhub.beans.services;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecheckFailing extends AbstractJob {

	private final static Logger logger = LoggerFactory.getLogger(RecheckFailing.class);
	
	public RecheckFailing(SubscriptionRepository repository,
			ExecutorService executor) {
		super(repository, executor);
	}

	@Override
	public void run() {
		logger.info("This job is doing nothing at the moment.");
	}

}
