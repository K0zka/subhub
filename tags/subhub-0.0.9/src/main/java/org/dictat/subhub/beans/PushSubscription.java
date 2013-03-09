package org.dictat.subhub.beans;

import java.util.Date;

/**
 * This class stores the data of subscriptions where a hub is available.
 * 
 * @author kocka
 */
public class PushSubscription extends Subscription {
	public PushSubscription() {
		super();
	}

	public PushSubscription(Subscription sub) {
		super(sub);
	}

	private String hub;
	private Integer lease;
	private Date lastResubscribe;
	private String verifyToken;

	@Override
	public String toString() {
		return "[push " + getUrl() + " through " + hub + "]";
	}

	public String getHub() {
		return hub;
	}

	public void setHub(String hub) {
		this.hub = hub;
	}

	public Integer getLease() {
		return lease;
	}

	public void setLease(Integer lease) {
		this.lease = lease;
	}

	public Date getLastResubscribe() {
		return lastResubscribe;
	}

	public void setLastResubscribe(Date lastResubscribe) {
		this.lastResubscribe = lastResubscribe;
	}

	public String getVerifyToken() {
		return verifyToken;
	}

	public void setVerifyToken(String verifyToken) {
		this.verifyToken = verifyToken;
	}


}
