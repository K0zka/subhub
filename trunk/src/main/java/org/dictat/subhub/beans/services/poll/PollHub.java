package org.dictat.subhub.beans.services.poll;

import java.io.IOException;
import java.util.Date;

import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.SubscriptionStatus;
import org.dictat.subhub.beans.services.Hub;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.beans.transport.EtagCacheControl;
import org.todomap.feed.beans.transport.LastModifiedCacheControl;
import org.todomap.feed.beans.transport.TransportCacheControl;

/**
 * This class provides equivalent functionality to SubHub for hubless streams.
 */
public class PollHub implements Hub<PollSubscription> {

	public PollHub(PollScheduler pollScheduler) {
		super();
		this.pollScheduler = pollScheduler;
	}

	final PollScheduler pollScheduler;
	
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

	@Override
	public PollSubscription subscribe(final NewsFeed feed, final String feedUrl) {
		final PollSubscription poll = new PollSubscription();
		updateCacheData(poll, feed);
		poll.setInterval(pollScheduler.getPollFrequency(feed));
		poll.setStatus(SubscriptionStatus.Subscribed);
		// poll asap
		poll.setNextPoll(new Date());
		return poll;
	}

	@Override
	public PollSubscription unsubscribe(final PollSubscription subscription) {
		subscription.setStatus(SubscriptionStatus.Unsubscribed);
		return subscription;
	}

	@Override
	public boolean accepts(NewsFeed feed) {
		// accepts anything
		return true;
	}

}
