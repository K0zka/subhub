package org.dictat.subhub.beans.services.poll;

import java.io.IOException;

import org.junit.Test;
import org.todomap.feed.HttpClientReader;

public class PollSchedulerTest {
	@Test
	public void testGetPollFrequency() throws IOException {
		long pollFrequency = new PollScheduler().getPollFrequency(HttpClientReader.read("http://tifyty.wordpress.com/feed/"));
		System.out.println(pollFrequency / (60000) + " minutes");
		pollFrequency = new PollScheduler().getPollFrequency(HttpClientReader.read("http://index.hu/24ora/rss/"));
		System.out.println(pollFrequency / (1000) + " seconds");
		pollFrequency = new PollScheduler().getPollFrequency(HttpClientReader.read("http://origo.hu/contentpartner/rss/hircentrum/origo.xml"));
		System.out.println(pollFrequency / (60000) + " minutes");
	}
}
