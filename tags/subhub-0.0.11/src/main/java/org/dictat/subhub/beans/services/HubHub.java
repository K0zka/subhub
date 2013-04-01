package org.dictat.subhub.beans.services;

import java.io.IOException;
import java.util.Date;

import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.PushSubscription;
import org.dictat.subhub.beans.Subscription;
import org.dictat.subhub.beans.SubscriptionStatus;
import org.todomap.feed.HttpClientReader;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.utils.NewsFeedUtils;

public class HubHub {

	public HubHub(Hub<PollSubscription> pollHub, Hub<PushSubscription> subhub,
			SubscriptionRepository repository) {
		super();
		this.pollHub = pollHub;
		this.subhub = subhub;
		this.repository = repository;
	}

	final Hub<PollSubscription> pollHub;
	final Hub<PushSubscription> subhub;
	final SubscriptionRepository repository;

	@SuppressWarnings("unchecked")
	public void subscribe(String url) throws IOException {
		final NewsFeed feed = HttpClientReader.read(url);
		@SuppressWarnings("rawtypes")
		final Hub hub = getHubForFeed(feed);
		final Subscription sub = hub.subscribe(feed, url);

		sub.setUrl(url);
		final Date now = new Date();
		sub.setSubscribed(now);
		sub.setStatusChange(now);
		repository.save(sub);

		hub.postSubscribe(sub);
	}

	Hub<?> getHubForFeed(final NewsFeed feed) {
		if (NewsFeedUtils.getPubSubHub(feed) != null) {
			return subhub;
		} else {
			return pollHub;
		}
	}

	public void unsubscribe(final String url) {
		Subscription sub = repository.getByUrl(url);
		if (sub instanceof PushSubscription) {
			sub = subhub.unsubscribe((PushSubscription) sub);
		} else {
			sub.setStatus(SubscriptionStatus.Unsubscribed);
		}
	}

}
