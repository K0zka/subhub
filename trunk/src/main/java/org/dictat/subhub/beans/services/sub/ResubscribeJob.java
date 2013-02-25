package org.dictat.subhub.beans.services.sub;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.PushSubscription;
import org.dictat.subhub.beans.Subscription;
import org.dictat.subhub.beans.SubscriptionStatus;
import org.dictat.subhub.beans.services.Prioritized;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.todomap.feed.HttpClientReader;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.utils.NewsFeedUtils;

public class ResubscribeJob implements Runnable {

	class ResubscribeTask implements Runnable, Prioritized, Comparable<Prioritized> {
		private final PushSubscription sub;

		ResubscribeTask(PushSubscription sub) {
			this.sub = sub;
		}

		@Override
		public void run() {
			logger.info("Refreshing subscription:" + sub.getUrl());
			try {
				NewsFeed feed = HttpClientReader.read(sub.getUrl());
				String hub = NewsFeedUtils.getPubSubHub(feed);
				if(StringUtils.isEmpty(hub)) {
					logger.info("{} is no longer pushing to a hub", sub.getUrl());
					PollSubscription poll = new PollSubscription(sub);
					poll.setNextPoll(new Date());
					poll.setStatus(SubscriptionStatus.Subscribed);
					repository.save(poll);
				} else if (!ObjectUtils.equals(hub, sub.getHub())) {
					logger.info("{} changed hub from {} to {}", new Object[] {sub.getUrl(), sub.getHub(), hub});
					sub.setHub(hub);
					repository.save(sub);
				} else {
					subHub.postSubscribe(sub);
					checkSubscription(sub, SubscriptionStatus.Subscribed);
				}
			} catch (IOException e) {
				checkSubscription(sub, SubscriptionStatus.Failing);
				logger.warn("Failed to resubscribe " + sub.getUrl(), e);
			}
		}

		@Override
		public int compareTo(Prioritized o) {
			return (int)(getPriority() - o.getPriority());
		}

		@Override
		public double getPriority() {
			return 0;
		}
	}

	public ResubscribeJob(SubscriptionRepository repository, SubHub subHub,
			ExecutorService executor) {
		super();
		this.repository = repository;
		this.subHub = subHub;
		this.executor = executor;
	}

	private final static Logger logger = LoggerFactory
			.getLogger(ResubscribeJob.class);

	final SubscriptionRepository repository;
	final SubHub subHub;
	final ExecutorService executor;

	public void run() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -2);
		final List<PushSubscription> expiringSubs = repository
				.findExpring(calendar.getTime());
		logger.info("Checking {} subscriptions to refresh subscription",
				expiringSubs.size());
		for (final PushSubscription sub : expiringSubs) {
			executor.execute(new ResubscribeTask(sub));
		}
	}

	private void checkSubscription(Subscription sub, SubscriptionStatus status) {
		if (sub.getStatus() != status) {
			sub.setStatus(status);
			sub.setStatusChange(new Date());
			repository.save(sub);
		}
	}

}
