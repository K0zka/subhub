package org.dictat.subhub.beans.services.sub;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.dictat.subhub.beans.PushSubscription;
import org.dictat.subhub.beans.SubscriptionStatus;
import org.dictat.subhub.beans.services.Hub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.utils.NewsFeedUtils;

public class SubHub implements Hub<PushSubscription> {
	public final static String callback = "hub.callback";
	public final static String mode = "hub.mode";
	public final static String topic = "hub.topic";
	public final static String verify = "hub.verify";
	public final static String verifyToken = "hub.verify_token";
	public final static String challenge = "hub.challenge";
	public final static String contentUrl = "hub.url";

	public final static String modeSubscribe = "subscribe";
	public final static String modeUnsubscribe = "unsubscribe";
	public final static String modePublish = "publish";

	public final static String verifySync = "sync";
	public final static String verifyAsync = "async";

	private final static Logger logger = LoggerFactory.getLogger(SubHub.class);

	String subhubUrl = "http://www.dictat.org/subhubdev";

	private HttpClient getHttpClient() {
		return new DecompressingHttpClient(new AutoRetryHttpClient(
				new SystemDefaultHttpClient()));
	}

	public String getSubhubUrl() {
		return subhubUrl;
	}

	public void setSubhubUrl(String subhubUrl) {
		this.subhubUrl = subhubUrl;
	}

	@Override
	public PushSubscription subscribe(NewsFeed feed, String feedUrl) {
		PushSubscription sub = new PushSubscription();
		sub.setHub(NewsFeedUtils.getPubSubHub(feed));
		sub.setStatus(SubscriptionStatus.Initiating);
		sub.setStatusChange(new Date());
		final String verificationCode = UUID.randomUUID().toString();
		logger.info(
				"new subscribe request to {} for {} verification code is {}",
				new Object[] { sub.getHub(), feedUrl, verificationCode });
		sub.setVerifyToken(verificationCode);
		return sub;
	}

	@Override
	public PushSubscription postSubscribe(PushSubscription sub)
			throws IOException {
		final HttpClient client = getHttpClient();
		final HttpPost post = new HttpPost(sub.getHub());
		post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
				new BasicNameValuePair(callback, subhubUrl + "/subs"),
				new BasicNameValuePair(topic, sub.getUrl()),
				new BasicNameValuePair(verify, verifySync),
				new BasicNameValuePair(mode, modeSubscribe),
				new BasicNameValuePair(verifyToken, ((PushSubscription) sub)
						.getVerifyToken()))));
		HttpResponse response = client.execute(post);
		logger.info(
				"response: {} {}",
				response.getStatusLine().getStatusCode(),
				response.getEntity() == null ? "" : IOUtils.toString(response
						.getEntity().getContent()));
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			sub.setStatus(SubscriptionStatus.Subscribed);
		} else {
			sub.setStatus(SubscriptionStatus.Failing);
		}
		return sub;
	}

	@Override
	public PushSubscription unsubscribe(PushSubscription sub) {
		sub.setStatus(SubscriptionStatus.PendingUnsubscription);
		return sub;
	}

	@Override
	public PushSubscription postUnsubscribe(PushSubscription subscription)
			throws IOException {
		// TODO Auto-generated method stub
		final HttpPost post = new HttpPost(subscription.getHub());
		post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
				new BasicNameValuePair(callback, subhubUrl + "/subs"),
				new BasicNameValuePair(topic, subscription.getUrl()),
				new BasicNameValuePair(verify, verifySync),
				new BasicNameValuePair(mode, modeUnsubscribe))));
		final HttpResponse response = getHttpClient().execute(post);
		logger.info(
				"response: %s %s",
				response.getStatusLine().getStatusCode(),
				response.getEntity() == null ? "" : IOUtils.toString(response
						.getEntity().getContent()));
		return subscription;
	}

	@Override
	public boolean accepts(NewsFeed feed) {
		// accept if there is a pubsubhub url
		return NewsFeedUtils.getPubSubHub(feed) != null;
	}

}
