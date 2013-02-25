package org.dictat.subhub.beans.services;

import java.io.IOException;

import junit.framework.Assert;

import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.PushSubscription;
import org.junit.Test;
import org.mockito.Mockito;
import org.todomap.feed.beans.NewsFeed;

public class HubHubTest {
	@Test
	public void testSubscribePushAtom() throws IOException {
		subscribePush("http://todomap.googlecode.com/svn/feed/tags/feed-0.0.4/src/test/resources/iwillworkforfood-atom.xml");
	}

	private void subscribePush(String url) throws IOException {
		@SuppressWarnings("unchecked")
		Hub<PushSubscription> subHub = Mockito.mock(Hub.class);
		Mockito.when(
				subHub.subscribe(Mockito.any(NewsFeed.class),
						Mockito.anyString()))
				.thenReturn(new PushSubscription());
		Mockito.when(subHub.postSubscribe(Mockito.any(PushSubscription.class)))
				.thenReturn(new PushSubscription());
		@SuppressWarnings("unchecked")
		Hub<PollSubscription> pollHub = Mockito.mock(Hub.class);
		Mockito.doThrow(new RuntimeException("poll hub should not be called"))
				.when(pollHub)
				.subscribe(Mockito.any(NewsFeed.class), Mockito.anyString());
		SubscriptionRepository repository = Mockito
				.mock(SubscriptionRepository.class);
		HubHub hubHub = new HubHub(pollHub, subHub, repository);
		hubHub.subscribe(url);
	}

	@Test(expected = IOException.class)
	public void testSubscribeNotExisting() throws IOException {
		@SuppressWarnings("unchecked")
		Hub<PushSubscription> subHub = Mockito.mock(Hub.class);
		@SuppressWarnings("unchecked")
		Hub<PollSubscription> pollHub = Mockito.mock(Hub.class);
		SubscriptionRepository repository = Mockito
				.mock(SubscriptionRepository.class);
		HubHub hubHub = new HubHub(pollHub, subHub, repository);
		hubHub.subscribe("http://example.com/intentionallynotexisting.rss");
	}

	@Test
	public void testSubscribePoll() throws IOException {
		@SuppressWarnings("unchecked")
		Hub<PushSubscription> subHub = Mockito.mock(Hub.class);
		Mockito.doThrow(new RuntimeException("sub hub should not be called"))
				.when(subHub)
				.subscribe(Mockito.any(NewsFeed.class), Mockito.anyString());
		@SuppressWarnings("unchecked")
		Hub<PollSubscription> pollHub = Mockito.mock(Hub.class);
		PollSubscription sub = new PollSubscription();
		Mockito.when(
				pollHub.subscribe(Mockito.any(NewsFeed.class),
						Mockito.anyString())).thenReturn(sub);
		Mockito.when(pollHub.postSubscribe(sub)).thenReturn(sub);
		SubscriptionRepository repository = Mockito
				.mock(SubscriptionRepository.class);
		HubHub hubHub = new HubHub(pollHub, subHub, repository);
		hubHub.subscribe("http://todomap.googlecode.com/svn/feed/tags/feed-0.0.4/src/test/resources/iwillworkforfood-rss.xml");
		Assert.assertEquals(
				"http://todomap.googlecode.com/svn/feed/tags/feed-0.0.4/src/test/resources/iwillworkforfood-rss.xml",
				sub.getUrl());
		Assert.assertNotNull(sub.getSubscribed());
	}

}
