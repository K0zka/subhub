package org.dictat.subhub.beans.services.feedpolice;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.todomap.feed.HttpClientReader;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.beans.NewsItem;
import org.todomap.feed.utils.NewsFeedUtils;

public class DefaultFeedPolice implements FeedPolice {

	@Override
	public FeedProfile checkFeed(String url) {
		final FeedProfile profile = new FeedProfile();
		try {
			NewsFeed feed = HttpClientReader.read(url);
			profile.setPubsubhubbub(NewsFeedUtils.getPubSubHub(feed) != null);
			profile.setCompressing(feed.isTransportCompressed());
			profile.setCaching(feed.getCacheControl() != null
					&& !feed.getCacheControl().isEmpty());
			NewsFeed rereadFeed = HttpClientReader.read(url,
					feed.getCacheControl());
			profile.setRespectCache(rereadFeed == null);

			rereadFeed = HttpClientReader.read(url);
			profile.setCorrectUuid(correctUuids(feed, rereadFeed));
			profile.setCorrectTimestamp(correctTimestamp(feed, rereadFeed));
		} catch (IOException e) {
			return null;
		}
		return profile;
	}

	static boolean correctTimestamp(NewsFeed feed, NewsFeed rereadFeed) {
		Set<Date> published = new HashSet<>();
		for(NewsItem item : feed.getNewsItems()) {
			if(item.getPublished() == null) {
				return false;
			}
			if(published.contains(item.getPublished())) {
				return false;
			}
			published.add(item.getPublished());
		}
		return true;
	}

	static boolean correctUuids(NewsFeed feed, NewsFeed rereadFeed) {
		Set<String> guids = new HashSet<>();
		for(NewsItem item : feed.getNewsItems()) {
			if(item.getGuid() == null) {
				return false;
			}
			if(guids.contains(item.getGuid())) {
				return false;
			}
			guids.add(item.getGuid());
		}
		return true;
	}

}
