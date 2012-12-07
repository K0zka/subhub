package org.dictat.subhub.beans;

import java.util.Date;

public class Subscription {
	String url;
	Date subscribed;
	Date lastResubscribe;
	String verifyToken;
	String hub;
	Integer lease;
	public String getVerifyToken() {
		return verifyToken;
	}
	public void setVerifyToken(String verifyToken) {
		this.verifyToken = verifyToken;
	}
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
	public Date getLastResubscribe() {
		return lastResubscribe;
	}
	public void setLastResubscribe(Date lastResubscribe) {
		this.lastResubscribe = lastResubscribe;
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
}
