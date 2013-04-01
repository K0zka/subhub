package org.dictat.subhub.beans.services.poll;

import java.util.Date;

import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.beans.NewsItem;

public class PollScheduler {
	public static final long defaultPollFrequencyMsHardDefault = 1000 * 60 * 60 * 24;
	private int compressionBonus = 10;

	private int correctCacheBonus = 50;

	private long defaultPollFrequencyMs = defaultPollFrequencyMsHardDefault;

	private double getBonus(final NewsFeed feed) {
		double factor = 1d;
		if (feed.getCacheControl() != null && !feed.getCacheControl().isEmpty()) {
			factor = factor * correctCacheBonus;
		}
		if (feed.isTransportCompressed()) {
			factor = factor * compressionBonus;
		}
		return factor;
	}

	public int getCompressionBonus() {
		return compressionBonus;
	}

	public int getCorrectCacheBonus() {
		return correctCacheBonus;
	}

	public long getDefaultPollFrequencyMs() {
		return defaultPollFrequencyMs;
	}

	public long getPollFrequency(final NewsFeed feed) {
		Date first = null;
		Date last = null;
		for (final NewsItem item : feed.getNewsItems()) {
			if (first == null
					|| (item.getPublished() != null && item.getPublished()
							.before(first))) {
				first = item.getPublished();
			}
			if (last == null
					|| (item.getPublished() != null && item.getPublished()
							.after(last))) {
				last = item.getPublished();
			}
		}
		if (first != null && last != null) {
			return (long) ((last.getTime() - first.getTime())
					/ (feed.getNewsItems().size() * getBonus(feed)));
		} else {
			return (long) (defaultPollFrequencyMs / getBonus(feed));
		}
	}

	public void setCompressionBonus(final int compressionBonus) {
		this.compressionBonus = compressionBonus;
	}

	public void setCorrectCacheBonus(final int correctCacheBonus) {
		this.correctCacheBonus = correctCacheBonus;
	}

	public void setDefaultPollFrequencyMs(final long defaultPollFrequencyMs) {
		this.defaultPollFrequencyMs = defaultPollFrequencyMs;
	}
}
