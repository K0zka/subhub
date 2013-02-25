package org.dictat.subhub.beans;

import java.util.Date;

/**
 * Abstract subscription class. Only serves as a baseclass for
 * {@link PushSubscription} and {@link PollSubscription}, common attributes
 * here.
 */
public abstract class Subscription {
	public Subscription() {
		super();
	}

	/**
	 * Creates a subscription as a copy of an existing subscription.
	 * @param sub subscription to copy from.
	 */
	public Subscription(Subscription sub) {
		super();
		this.id = sub.getId();
		this.status = sub.getStatus();
		this.statusChange = sub.getStatusChange();
		this.subscribed = sub.getSubscribed();
		this.url = sub.getUrl();
	}

	private String id;
	private String url;
	private Date subscribed;
	private SubscriptionStatus status;
	private Date statusChange;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(Date subscribed) {
		this.subscribed = subscribed;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SubscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}

	public Date getStatusChange() {
		return statusChange;
	}

	public void setStatusChange(Date statusChange) {
		this.statusChange = statusChange;
	}
}
