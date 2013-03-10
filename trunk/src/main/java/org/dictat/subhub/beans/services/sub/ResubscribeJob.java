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
import org.dictat.subhub.beans.services.AbstractJob;
import org.dictat.subhub.beans.services.Prioritized;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.todomap.feed.HttpClientReader;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.utils.NewsFeedUtils;

/**
 * This job is refreshing subscriptions for subhub protocol. Theoretically it is
 * not needed, practically it is sometimes needed (workaround).
 * 
 * @author kocka
 */
public class ResubscribeJob extends AbstractJob {

	class ResubscribeTask implements Runnable, Prioritized,
			Comparable<Prioritized> {
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
				if (StringUtils.isEmpty(hub)) {
					logger.info("{} is no longer pushing to a hub",
							sub.getUrl());
					PollSubscription poll = new PollSubscription(sub);
					poll.setNextPoll(new Date());
					poll.setStatus(SubscriptionStatus.Subscribed);
					repository.save(poll);
				} else if (!ObjectUtils.equals(hub, sub.getHub())) {
					// TODO: this needs testing
					logger.info("{} changed hub from {} to {}", new Object[] {
							sub.getUrl(), sub.getHub(), hub });
					sub.setHub(hub);
					repository.save(sub);
					subHub.postSubscribe(sub);
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
			return (int) (getPriority() - o.getPriority());
		}

		@Override
		public double getPriority() {
			return -1;
		}
	}

	public ResubscribeJob(SubscriptionRepository repository, SubHub subHub,
			ExecutorService executor) {
		super(repository, executor);
		this.subHub = subHub;
	}

	private final static Logger logger = LoggerFactory
			.getLogger(ResubscribeJob.class);

	final SubHub subHub;

	public void run() {
		final List<PushSubscription> expiringSubs = repository
				.findExpring(getExpriginSubscriptionDate());
		logger.info("Checking {} subscriptions to refresh subscription",
				expiringSubs.size());
		for (final PushSubscription sub : expiringSubs) {
			executor.execute(new ResubscribeTask(sub));
		}
	}

	static Date getExpriginSubscriptionDate() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -2);
		Date time = calendar.getTime();
		return time;
	}

	private void checkSubscription(Subscription sub, SubscriptionStatus status) {
		if (sub.getStatus() != status) {
			sub.setStatus(status);
			sub.setStatusChange(new Date());
			repository.save(sub);
		}
	}

}
