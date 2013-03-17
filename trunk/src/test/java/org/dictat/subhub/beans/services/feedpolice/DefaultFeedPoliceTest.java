package org.dictat.subhub.beans.services.feedpolice;

import org.junit.Test;

public class DefaultFeedPoliceTest {
	@Test
	public void checkFeed() {
		new DefaultFeedPolice().checkFeed("http://iwillworkforfood.blogspot.com/feeds/posts/default?alt=atom");
		new DefaultFeedPolice().checkFeed("http://index.hu/24ora/rss/");
	}
}
