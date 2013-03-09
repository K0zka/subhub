package org.dictat.subhub.utils;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.dictat.subhub.beans.SubscriptionStatus;
import org.junit.Test;

public class StatusUtilsTest {
	@Test
	public void testToStatus() {
		Assert.assertEquals(StatusUtils.toStatus(null), SubscriptionStatus.Subscribed);
		Assert.assertEquals(StatusUtils.toStatus("s"), SubscriptionStatus.Subscribed);
	}
	@Test
	public void testToLetter() {
		Set<String> vals = new HashSet<String>();
		for(SubscriptionStatus status : SubscriptionStatus.values()) {
			String letter = StatusUtils.toLetter(status);
			Assert.assertFalse(vals.contains(letter));
			vals.add(letter);
		}
	}
}
