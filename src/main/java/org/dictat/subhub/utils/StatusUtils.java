package org.dictat.subhub.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dictat.subhub.beans.SubscriptionStatus;

public class StatusUtils {

	@SuppressWarnings("serial")
	private final static Map<String, SubscriptionStatus> statusMap = Collections
			.unmodifiableMap(new HashMap<String, SubscriptionStatus>() {
				{
					put("i", SubscriptionStatus.Initiating);
					put("s", SubscriptionStatus.Subscribed);
					put("f", SubscriptionStatus.Failing);
					put("u", SubscriptionStatus.Unsubscribed);
				}
			});

	public static SubscriptionStatus toStatus(String statusStr) {
		if (statusStr == null) {
			return SubscriptionStatus.Subscribed;
		}
		return statusMap.get(statusStr);
	}

	public static String toLetter(SubscriptionStatus status) {
		//TODO: this is kind of a lazy solution
		return status.name().substring(0, 1).toLowerCase();
	}

}
