package org.dictat.subhub.beans.services.poll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.SubscriptionStatus;
import org.dictat.subhub.beans.services.AbstractJob;
import org.dictat.subhub.beans.services.EventQueue;
import org.dictat.subhub.beans.services.Prioritized;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.todomap.feed.HttpClientReader;
import org.todomap.feed.Writer;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.beans.NewsItem;
import org.todomap.feed.beans.transport.EtagCacheControl;
import org.todomap.feed.beans.transport.LastModifiedCacheControl;
import org.todomap.feed.beans.transport.TransportCacheControl;

/**
 * Poll the hub-less subscriptions.
 */
public class PollJob extends AbstractJob {
	final PollScheduler pollScheduler;

	class PollTask implements Runnable, Prioritized, Comparable<Prioritized> {
		private final PollSubscription poll;

		PollTask(PollSubscription poll) {
			this.poll = poll;
		}

		@Override
		public void run() {
			try {
				logger.info("{} - fetching...", poll.getUrl());
				NewsFeed feed = HttpClientReader.read(poll.getUrl(),
						getCacheControls(poll));
				if (feed == null) {
					// this means no change
					logger.info("{} - no new content", poll.getUrl());
					updateNextPoll(poll, feed, false);
					repository.save(poll);
					return;
				}
				boolean sentAny = false;
				if (feed.getNewsItems() != null) {
					sentAny = sendToQueue(feed, poll);
				}
				updateNextPoll(poll, feed, !sentAny);
				PollHub.updateCacheData(poll, feed);
				repository.save(poll);
			} catch (IOException e) {
				logger.error("{} - poll failing", poll.getUrl(), e);
				poll.setStatus(SubscriptionStatus.Failing);
				poll.setStatusChange(new Date());
				repository.save(poll);
			}
		}

		private boolean sendToQueue(NewsFeed feed, PollSubscription poll) {
			boolean first = true;
			boolean sentAny = false;
			logger.info("{} - checking new items", poll.getUrl());
			String lastGuid = null;
			for (NewsItem item : feed.getNewsItems()) {
				if (first) {
					lastGuid = getGuid(item);
					first = false;
				}
				if (poll.getLastGuid() != null
						&& ObjectUtils
								.equals(getGuid(item), poll.getLastGuid())) {
					break;
				}
				// send item to queue
				sendToQueue(feed, item);
				sentAny = true;
			}
			if (!sentAny && poll.isHttpCache()) {
				logger.warn("{} - may be a cheater, ignored cache",
						poll.getUrl());
			}

			poll.setLastGuid(lastGuid);
			return sentAny;
		}

		private String getGuid(NewsItem item) {
			if (item.getGuid() != null) {
				return item.getGuid();
			} else if (item.getUrl() != null) {
				return item.getUrl();
			} else if (item.getPublished() != null) {
				return item.getPublished().toString();
			} else
				return null;
		}

		private void sendToQueue(NewsFeed feed, NewsItem item) {
			try {
				logger.info("sending to queue: {}", getGuid(item));
				NewsFeed cloneFeed = (NewsFeed) BeanUtils.cloneBean(feed);
				// replace with a single item (this needs some care)
				BeanUtils.setProperty(cloneFeed, "items",
						Collections.singletonList(item));
				BeanUtils.setProperty(cloneFeed, "entries",
						Collections.singletonList(item));
				// serialize
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Writer.write(cloneFeed, out);
				queue.onPublish(new ByteArrayInputStream(out.toByteArray()));
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				logger.error(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public int compareTo(Prioritized o) {
			return (int) (o.getPriority() - getPriority());
		}

		@Override
		public double getPriority() {
			return 2;
		}
	}

	public PollJob(SubscriptionRepository repository, EventQueue queue,
			ExecutorService executor, PollScheduler pollScheduler) {
		super(repository, executor);
		this.queue = queue;
		this.pollScheduler = pollScheduler;
	}

	final EventQueue queue;
	private final static Logger logger = LoggerFactory.getLogger(PollJob.class);

	public static List<TransportCacheControl> getCacheControls(
			PollSubscription poll) {
		ArrayList<TransportCacheControl> ret = new ArrayList<>(2);
		if (poll.getEtag() != null) {
			ret.add(new EtagCacheControl(poll.getEtag()));
		}
		if (poll.getLastupdate() != null) {
			ret.add(new LastModifiedCacheControl(poll.getLastupdate()));
		}
		return ret;
	}

	@Override
	public void run() {
		List<PollSubscription> polls = repository.findPolling(new Date());
		ArrayList<Future<?>> futures = new ArrayList<>();
		logger.info("Polling {} sources.", polls.size());
		for (final PollSubscription poll : polls) {
			futures.add(executor.submit(new PollTask(poll)));
		}
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				logger.warn("interrupted while waiting for task", e);
			} catch (ExecutionException e) {
				logger.warn("task failed", e);
			}
		}
	}

	void updateNextPoll(PollSubscription poll, NewsFeed feed, boolean cheater) {
		if (cheater) {
			poll.setInterval(Math.max(pollScheduler.getPollFrequency(feed) * 100, 300000));
			logger.warn("{} - is misbehaving poll frequency updated to {}",
					poll.getUrl(), poll.getInterval());
		} else if (poll.getInterval() == null) {
			poll.setInterval(pollScheduler.getPollFrequency(feed));
		}
		if (poll.getNextPoll() == null) {
			poll.setNextPoll(new Date());
		} else {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date()); // TODO: not calculate it from the
											// date, but from now, maybe this is
											// not perfect
			calendar.add(Calendar.MILLISECOND, poll.getInterval().intValue());
			poll.setNextPoll(calendar.getTime());
			logger.info("{} - next poll {}", poll.getUrl(), poll.getNextPoll());
		}
	}

}
