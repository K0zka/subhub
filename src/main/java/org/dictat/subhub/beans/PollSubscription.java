package org.dictat.subhub.beans;

import java.util.Date;


/**
 * A poll subscription is without hub, needs to be polled sometimes to fetch new entries.
 * @author kocka
 *
 */
public class PollSubscription extends Subscription {
	public PollSubscription() {
		super();
	}

	public PollSubscription(Subscription sub) {
		super(sub);
	}

	private Long interval;
	private Date nextPoll;
	private String etag;
	private String lastupdate;
	private String lastGuid;

	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
	}

	public Date getNextPoll() {
		return nextPoll;
	}

	public void setNextPoll(Date nextPoll) {
		this.nextPoll = nextPoll;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public String getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(String lastupdate) {
		this.lastupdate = lastupdate;
	}

	@Override
	public String toString() {
		return "[poll " + getUrl() + " " + interval + "]";
	}

	public String getLastGuid() {
		return lastGuid;
	}

	public void setLastGuid(String lastGuid) {
		this.lastGuid = lastGuid;
	}
}
