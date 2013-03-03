package org.dictat.subhub.beans;

public enum SubscriptionStatus {
	/**
	 * Initiating subscription, may not be necessary in all cases.
	 */
	Initiating,
	/**
	 * Subscribed to feed, news are coming either by push or poll.
	 */
	Subscribed,
	/**
	 * Temporary error in the source.
	 */
	Failing,
	/**
	 * The source is dead and nothing further to do.
	 */
	Dead,
	/**
	 * Unsubscribe initiated. Not needed for polling feeds.
	 */
	PendingUnsubscription,
	/**
	 * Unsubscribed from feed.
	 */
	Unsubscribed
}
