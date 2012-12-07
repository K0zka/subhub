package org.dictat.subhub.beans.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.dictat.subhub.beans.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.todomap.feed.Reader;
import org.todomap.feed.beans.NewsFeed;
import org.todomap.feed.utils.NewsFeedUtils;

public class SubHub {
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

	public SubHub(SubscriptionRepository repository, EventQueue eventQueue) {
		this.repository = repository;
		this.eventQueue = eventQueue;
	}

	final SubscriptionRepository repository;
	final EventQueue eventQueue;
	private final static Logger logger = LoggerFactory.getLogger(SubHub.class);

	String defaulthub = "https://pubsubhubbub.appspot.com/";
	String subhubUrl = "http://www.dictat.org/subhubdev";

	public void subscribe(String feedUrl) throws IOException {
		logger.info("requested suscribe to {}", feedUrl);
		final String pubSubHub = getFeedHub(Reader.read(feedUrl));

		logger.info("hub is {}", pubSubHub);

		Subscription subscription = repository.getByUrl(feedUrl);
		if (subscription == null) {
			final String verificationCode = UUID.randomUUID().toString();
			logger.info("new request, verification code is {}",
					verificationCode);
			subscription = new Subscription();
			subscription.setHub(pubSubHub);
			subscription.setVerifyToken(verificationCode);
			subscription.setUrl(feedUrl);
			subscription.setSubscribed(new Date());
			repository.save(subscription);
		}

		final HttpClient client = getHttpClient();
		final HttpPost post = new HttpPost(pubSubHub);
		post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
				new BasicNameValuePair(callback, subhubUrl + "/subs"),
				new BasicNameValuePair(topic, feedUrl), new BasicNameValuePair(
						verify, verifySync), new BasicNameValuePair(mode,
						modeSubscribe), new BasicNameValuePair(verifyToken,
						subscription.getVerifyToken()))));
		HttpResponse response = client.execute(post);
		logger.info(
				"response: {} {}",
				response.getStatusLine().getStatusCode(),
				response.getEntity() == null ? "" : IOUtils.toString(response
						.getEntity().getContent()));
	}

	private String getFeedHub(NewsFeed feed) {
		final String pubSubHub = NewsFeedUtils.getPubSubHub(feed);
		return pubSubHub == null ? defaulthub : pubSubHub;
	}

	public boolean verifySubscription(String url, String verification) {
		logger.info("requested verification for {} with code {}", url,
				verification);
		Subscription subscription = repository.getByUrl(url);
		return subscription != null && verification != null
				&& verification.equals(subscription.getVerifyToken());
	}

	private HttpClient getHttpClient() {
		return new DecompressingHttpClient(new AutoRetryHttpClient(
				new SystemDefaultHttpClient()));
	}

	public void unsubscribe(String feedUrl) throws IOException {
		Subscription subscription = repository.getByUrl(feedUrl);
		final HttpClient client = getHttpClient();
		final HttpPost post = new HttpPost(subscription.getHub());
		post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
				new BasicNameValuePair(callback, subhubUrl + "/subs"),
				new BasicNameValuePair(topic, feedUrl), new BasicNameValuePair(
						verify, verifySync), new BasicNameValuePair(mode,
						modeUnsubscribe))));
		final HttpResponse response = client.execute(post);
		logger.info(
				"response: %s %s",
				response.getStatusLine().getStatusCode(),
				response.getEntity() == null ? "" : IOUtils.toString(response
						.getEntity().getContent()));
	}

	public boolean isSubscribed(String feedUrl) {
		return repository.getByUrl(feedUrl) != null;
	}

	public String getDefaulthub() {
		return defaulthub;
	}

	public void setDefaulthub(String defaulthub) {
		this.defaulthub = defaulthub;
	}

	public String getSubhubUrl() {
		return subhubUrl;
	}

	public void setSubhubUrl(String subhubUrl) {
		this.subhubUrl = subhubUrl;
	}
}
