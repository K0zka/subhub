package org.dictat.subhub.beans.services;

import java.util.Date;
import java.util.List;

import org.dictat.subhub.beans.Subscription;

public interface SubscriptionRepository {
	Subscription getByUrl(String url);
	void save(Subscription subscription);
	List<Subscription> findExpring(Date time);
}
