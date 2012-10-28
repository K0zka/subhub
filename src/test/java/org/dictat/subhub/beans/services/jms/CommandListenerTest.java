package org.dictat.subhub.beans.services.jms;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

public class CommandListenerTest {
	@Test
	public void testDiscover() {
		Collection<String> feedUrls = CommandListener.discover("iwillworkforfood.blogspot.com");
		Assert.assertNotNull(feedUrls);
		Assert.assertFalse(feedUrls.isEmpty());
	}
}
