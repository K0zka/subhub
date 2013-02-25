package org.dictat.subhub.beans.services.poll;

import java.io.IOException;
import java.util.Date;

import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.SubscriptionStatus;
import org.dictat.subhub.beans.services.Hub;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.beans.NewsItem;
import org.todomap.feed.beans.transport.EtagCacheControl;
import org.todomap.feed.beans.transport.LastModifiedCacheControl;
import org.todomap.feed.beans.transport.TransportCacheControl;

/**
 * This class provides equivalent functionality to SubHub for hubless streams.
 */
public class PollHub implements Hub<PollSubscription> {

	private long defaultPollFrequencyMs = 1000 * 60 * 60 * 24;

	public long getDefaultPollFrequencyMs() {
		return defaultPollFrequencyMs;
	}

	public static void updateCacheData(PollSubscription poll, NewsFeed feed) {
		if (feed.getCacheControl() == null) {
			poll.setLastupdate(null);
			poll.setEtag(null);
		} else {
			for (TransportCacheControl cacheControl : feed.getCacheControl()) {
				if (cacheControl instanceof EtagCacheControl) {
					poll.setEtag(cacheControl.getValue());
				} else if (cacheControl instanceof LastModifiedCacheControl) {
					poll.setLastupdate(cacheControl.getValue());
				}
			}
		}
	}

	public long getPollFrequency(final NewsFeed feed) {
		Date first = null;
		Date last = null;
		for (final NewsItem item : feed.getNewsItems()) {
			if (first == null
					|| (item.getPublished() != null && item.getPublished()
							.before(first))) {
				first = item.getPublished();
			}
			if (last == null
					|| (item.getPublished() != null && item.getPublished()
							.after(last))) {
				last = item.getPublished();
			}
		}
		if (first != null && last != null) {
			return (last.getTime() - first.getTime())
					* feed.getNewsItems().size() / 2;
		} else {
			return defaultPollFrequencyMs;
		}
	}

	@Override
	public PollSubscription postSubscribe(final PollSubscription subscription)
			throws IOException {
		// nothing to be done
		return subscription;
	}

	@Override
	public PollSubscription postUnsubscribe(final PollSubscription subscription)
			throws IOException {
		// nothing to be done
		return subscription;
	}

	public void setDefaultPollFrequencyMs(final long defaultPollFrequencyMs) {
		this.defaultPollFrequencyMs = defaultPollFrequencyMs;
	}

	@Override
	public PollSubscription subscribe(final NewsFeed feed, final String feedUrl) {
		final PollSubscription poll = new PollSubscription();
		updateCacheData(poll, feed);
		poll.setInterval(getPollFrequency(feed));
		poll.setStatus(SubscriptionStatus.Subscribed);
		// poll asap
		poll.setNextPoll(new Date());
		return poll;
	}

	@Override
	public PollSubscription unsubscribe(final PollSubscription subscription) {

		return subscription;
	}

	@Override
	public boolean accepts(NewsFeed feed) {
		// accepts anything
		return true;
	}

}
