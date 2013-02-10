package org.dictat.subhub.beans.services.jobs;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.dictat.subhub.beans.Subscription;
import org.dictat.subhub.beans.services.SubHub;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResubscribeJob implements Runnable {

	public ResubscribeJob(SubscriptionRepository repository, SubHub subHub) {
		super();
		this.repository = repository;
		this.subHub = subHub;
	}

	private final static Logger logger = LoggerFactory
			.getLogger(ResubscribeJob.class);

	final SubscriptionRepository repository;
	final SubHub subHub;

	public void run() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -2);
		for (Subscription sub : repository.findExpring(calendar.getTime())) {
			logger.info("Refreshing subscription:" + sub.getUrl());
			try {
				subHub.subscribe(sub.getUrl());
			} catch (IOException e) {
				logger.debug("Failed to resubscribe " + sub.getUrl(), e);
			}
		}
	}

}
