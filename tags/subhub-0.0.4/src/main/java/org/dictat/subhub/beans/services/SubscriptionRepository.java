package org.dictat.subhub.beans.services;

import org.dictat.subhub.beans.Subscription;

public interface SubscriptionRepository {
	Subscription getByUrl(String url);
	void save(Subscription subscription);
}
