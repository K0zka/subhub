package org.dictat.subhub.beans.services.feedpolice;

import junit.framework.Assert;

import org.junit.Test;

public class FeedPoliceServletTest {
	@Test
	public void testIsOk() {
		Assert.assertFalse(FeedPoliceServlet.isOk("http://127.0.0.1/"));
		Assert.assertFalse(FeedPoliceServlet.isOk("http://127.0.0.2/"));
		Assert.assertFalse(FeedPoliceServlet.isOk("http://::1/"));
		Assert.assertFalse(FeedPoliceServlet.isOk("http://localhost:1234/"));
		Assert.assertFalse(FeedPoliceServlet.isOk("http://localhost:1234/"));
		Assert.assertTrue(FeedPoliceServlet.isOk("http://hup.hu/"));
		Assert.assertTrue(FeedPoliceServlet.isOk("http://192.168.1.1/"));
		
	}
}
