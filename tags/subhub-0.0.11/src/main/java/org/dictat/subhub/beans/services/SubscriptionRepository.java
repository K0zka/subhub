package org.dictat.subhub.beans.services;

import java.util.Date;
import java.util.List;

import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.PushSubscription;
import org.dictat.subhub.beans.Subscription;

/**
 * Persistent repository interface for subscriptions.
 */
public interface SubscriptionRepository {
	Subscription getByUrl(String url);
	void save(Subscription subscription);
	List<PushSubscription> findExpring(Date time);
	List<PollSubscription> findPolling(Date time);
	List<Subscription> findFailing();
}
