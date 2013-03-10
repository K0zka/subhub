package org.dictat.subhub.beans.services;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.Subscription;
import org.dictat.subhub.beans.SubscriptionStatus;
import org.dictat.subhub.beans.services.poll.PollJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.todomap.feed.HttpClientReader;

public class RecheckFailing extends AbstractJob {

	final class RecheckFailingTask implements Runnable, Prioritized, Comparable<Prioritized> {
		private final Subscription subscription;

		RecheckFailingTask(Subscription subscription) {
			this.subscription = subscription;
		}

		@Override
		public void run() {
			if (subscription instanceof PollSubscription) {
				logger.info("{} - rechecking", subscription.getUrl());
				try {
					HttpClientReader.read(
							subscription.getUrl(),
							PollJob.getCacheControls(((PollSubscription) subscription)));
					subscription
							.setStatus(SubscriptionStatus.Subscribed);
					repository.save(subscription);
				} catch (final IOException e) {
					logger.info(subscription.getUrl()
							+ " still failing");
					if (maxFailPeriod != null
							&& subscription.getStatusChange() != null
							&& subscription.getStatusChange().getTime()
									+ maxFailPeriod > System
										.currentTimeMillis()) {
						subscription.setStatus(SubscriptionStatus.Dead);
						repository.save(subscription);
					}
				}
			}
		}

		@Override
		public int compareTo(Prioritized o) {
			return (int) (o.getPriority() - getPriority());
		}

		@Override
		public double getPriority() {
			return -1d;
		}
	}

	private final static Logger logger = LoggerFactory
			.getLogger(RecheckFailing.class);

	Long maxFailPeriod;

	public RecheckFailing(final SubscriptionRepository repository,
			final ExecutorService executor) {
		super(repository, executor);
	}

	public Long getMaxFailPeriod() {
		return maxFailPeriod;
	}

	@Override
	public void run() {
		for (final Subscription subscription : repository.findFailing()) {
			executor.submit(new RecheckFailingTask(subscription));
		}
	}

	public void setMaxFailPeriod(final Long maxFailPeriod) {
		this.maxFailPeriod = maxFailPeriod;
	}

}
