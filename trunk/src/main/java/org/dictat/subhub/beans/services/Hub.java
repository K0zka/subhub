package org.dictat.subhub.beans.services;

import java.io.IOException;

import org.dictat.subhub.beans.Subscription;
import org.todomap.feed.beans.NewsFeed;

/**
 * Common interface for poll and subscription hubs.
 * 
 * @param <T>
 *            subscription record type
 */
public interface Hub<T extends Subscription> {
	/**
	 * Subscribe to a newsfeed. Implementation should not fetch it, the hubhub
	 * fetched it and provides all data. A {@link Subscription} instance should
	 * be created and returned.
	 * 
	 * @param feed
	 *            the data retrieved from the URL
	 * @param feedUrl
	 *            the feed url
	 * @return
	 */
	T subscribe(NewsFeed feed, String feedUrl);

	/**
	 * Action to perform after subscription.
	 * 
	 * @param sub
	 * @throws IOException
	 */
	T postSubscribe(T sub) throws IOException;

	/**
	 * Unsubscribe from the subscription.
	 * 
	 * @param subscription
	 *            the subscription to unsubscribe from
	 * @return the subscription updated
	 */
	T unsubscribe(T subscription);

	/**
	 * Action to perform after unsubscription.
	 * 
	 * @param subscription
	 * @return
	 * @throws IOException
	 */
	T postUnsubscribe(T subscription) throws IOException;

	/**
	 * Check if the feed is (still) acceptable for this hub.
	 * 
	 * @param feed
	 *            the feed
	 * @return if the feed is acceptable for the hub
	 */
	boolean accepts(NewsFeed feed);

}
