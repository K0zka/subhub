package org.dictat.subhub.beans.services.poll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.ObjectUtils;
import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.services.EventQueue;
import org.dictat.subhub.beans.services.Prioritized;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.todomap.feed.HttpClientReader;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.beans.NewsItem;
import org.todomap.feed.beans.transport.EtagCacheControl;
import org.todomap.feed.beans.transport.LastModifiedCacheControl;
import org.todomap.feed.beans.transport.TransportCacheControl;

/**
 * Poll the hub-less subscriptions.
 */
public class PollJob implements Runnable {

	class PollTask implements Runnable, Prioritized, Comparable<Prioritized> {
		private final PollSubscription poll;

		PollTask(PollSubscription poll) {
			this.poll = poll;
		}

		@Override
		public void run() {
			try {
				logger.debug("{} - fetching...", poll.getUrl());
				NewsFeed feed = HttpClientReader.read(poll.getUrl(), getCacheControls(poll));
				boolean first = true;
				for(NewsItem item : feed.getNewsItems()) {
					if(first) {
						poll.setLastGuid(item.getGuid());
						first = false;
					}
					if(ObjectUtils.equals(item.getGuid(), poll.getLastGuid())) {
						break;
					}
				}
				PollHub.updateCacheData(poll, feed);
				repository.save(poll);
			} catch (IOException e) {
				logger.error("{} - poll failing", poll.getUrl(), e);
				//TODO
			}
		}

		@Override
		public int compareTo(Prioritized o) {
			return (int) (getPriority() - o.getPriority());
		}

		@Override
		public double getPriority() {
			return 1;
		}
	}

	public PollJob(SubscriptionRepository repository, EventQueue queue,
			ExecutorService executor) {
		super();
		this.repository = repository;
		this.queue = queue;
		this.executor = executor;
	}

	final SubscriptionRepository repository;
	final EventQueue queue;
	final ExecutorService executor;
	private final static Logger logger = LoggerFactory.getLogger(PollJob.class);

	static List<TransportCacheControl> getCacheControls(PollSubscription poll) {
		ArrayList<TransportCacheControl> ret = new ArrayList<>(2);
		if(poll.getEtag() != null) {
			ret.add(new EtagCacheControl(poll.getEtag()));
		}
		if(poll.getLastupdate() != null) {
			ret.add(new LastModifiedCacheControl(poll.getLastupdate()));
		}
		return ret;
	}

	@Override
	public void run() {
		List<PollSubscription> polls = repository.findPolling(new Date());
		for (final PollSubscription poll : polls) {
			executor.execute(new PollTask(poll));
		}
		// TODO Auto-generated method stub
	}

}
