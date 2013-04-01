package org.dictat.subhub.beans.services.feedpolice;

public class FeedProfile {

	/**
	 * Does the stream have caching headers
	 */
	boolean caching;
	/**
	 * Does the stream use any compression
	 */
	boolean compressing;
	/**
	 * Does the stream have correct timestamps
	 */
	boolean correctTimestamp;
	/**
	 * Is the stream using pubsubhubbub
	 */
	boolean pubsubhubbub;
	/**
	 * Does the stream actually respect cache headers
	 */
	boolean respectCache;

	/**
	 * UUID looks correct, no duplicates
	 */
	boolean correctUuid;
	
	public boolean isCaching() {
		return caching;
	}

	public boolean isCompressing() {
		return compressing;
	}

	public boolean isCorrectTimestamp() {
		return correctTimestamp;
	}

	public boolean isPubsubhubbub() {
		return pubsubhubbub;
	}

	public boolean isRespectCache() {
		return respectCache;
	}

	public void setCaching(final boolean caching) {
		this.caching = caching;
	}

	public void setCompressing(final boolean compressing) {
		this.compressing = compressing;
	}

	public void setCorrectTimestamp(final boolean correctTimestamp) {
		this.correctTimestamp = correctTimestamp;
	}

	public void setPubsubhubbub(final boolean pubsubhubbub) {
		this.pubsubhubbub = pubsubhubbub;
	}

	public void setRespectCache(final boolean respectCache) {
		this.respectCache = respectCache;
	}

	public boolean isCorrectUuid() {
		return correctUuid;
	}

	public void setCorrectUuid(boolean correctUuid) {
		this.correctUuid = correctUuid;
	}

}
