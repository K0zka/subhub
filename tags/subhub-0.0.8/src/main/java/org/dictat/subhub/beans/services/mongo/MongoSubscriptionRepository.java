package org.dictat.subhub.beans.services.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.dictat.subhub.beans.PollSubscription;
import org.dictat.subhub.beans.PushSubscription;
import org.dictat.subhub.beans.Subscription;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.dictat.subhub.utils.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSubscriptionRepository implements SubscriptionRepository {

	private final static Logger logger = LoggerFactory
			.getLogger(MongoSubscriptionRepository.class);

	static Subscription dbObjToSubscription(final DBObject res) {
		Subscription subscription;
		if (stringOrNull(res, "h") == null) {
			// no hub, this is a poll subscription
			subscription = new PollSubscription();
			((PollSubscription) subscription).setEtag(stringOrNull(res, "e"));
			((PollSubscription) subscription).setLastupdate(stringOrNull(res,
					"lu"));
			((PollSubscription) subscription).setNextPoll((Date) res.get("np"));
			((PollSubscription) subscription).setInterval((Long) res.get("iv"));
			((PollSubscription) subscription).setLastGuid(stringOrNull(res,
					"lg"));
		} else {
			subscription = new PushSubscription();
			((PushSubscription) subscription).setHub(stringOrNull(res, "h"));
			((PushSubscription) subscription)
					.setLease((Integer) (res.get("l")));
			((PushSubscription) subscription).setLastResubscribe((Date) res
					.get("lr"));
			((PushSubscription) subscription).setVerifyToken(stringOrNull(res,
					"v"));
		}
		subscription.setId(stringOrNull(res, "_id"));
		subscription.setUrl(stringOrNull(res, "u"));
		subscription.setSubscribed((Date) res.get("s"));
		subscription.setStatus(StatusUtils.toStatus(stringOrNull(res, "st")));
		subscription.setStatusChange((Date) res.get("stc"));
		return subscription;
	}

	static String stringOrNull(final DBObject res, final String key) {
		return res.get(key) == null ? null : res.get(key).toString();
	}

	static BasicDBObject subscriptionToDbObj(final Subscription subscription) {
		final BasicDBObject obj = new BasicDBObject();
		if (subscription.getId() != null) {
			obj.append("_id", new ObjectId(subscription.getId()));
		}
		if (subscription instanceof PushSubscription) {
			obj.append("h", ((PushSubscription) subscription).getHub())
					.append("v",
							((PushSubscription) subscription).getVerifyToken())
					.append("s",
							((PushSubscription) subscription).getSubscribed())
					.append("lr",
							((PushSubscription) subscription)
									.getLastResubscribe())
					.append("l", ((PushSubscription) subscription).getLease());
		} else if (subscription instanceof PollSubscription) {
			obj.append("i", ((PollSubscription) subscription).getInterval())
					.append("e", ((PollSubscription) subscription).getEtag())
					.append("lu",
							((PollSubscription) subscription).getLastupdate())
					.append("np",
							((PollSubscription) subscription).getNextPoll())
					.append("iv",
							((PollSubscription) subscription).getInterval())
					.append("lg",
							((PollSubscription) subscription).getLastGuid());
		}
		obj.append("u", subscription.getUrl())
				.append("st", StatusUtils.toLetter(subscription.getStatus()))
				.append("stc", subscription.getStatusChange());
		return obj;
	}

	private String collection = "subs";

	private String dbname = "subhub";

	final Mongo mongo;

	public MongoSubscriptionRepository(final Mongo mongo) {
		this.mongo = mongo;
	}

	private void addSubs(final List<PushSubscription> subscriptions,
			final BasicDBObject query) {
		try (DBCursor cursor = getColl().find(query)) {
			while (cursor.hasNext()) {
				subscriptions.add((PushSubscription) dbObjToSubscription(cursor
						.next()));
			}
		}
	}

	@Override
	public List<PushSubscription> findExpring(final Date time) {
		final ArrayList<PushSubscription> subscriptions = new ArrayList<PushSubscription>();
		final BasicDBObject query = new BasicDBObject();
		query.append("lr", new BasicDBObject("$lt", time));
		query.append("h", new BasicDBObject("$ne", null));
		// filter failing
		query.append("st", new BasicDBObject("$ne", "f"));
		addSubs(subscriptions, query);

		BasicDBObject noLastResub = new BasicDBObject("lr", null).append("h",
				new BasicDBObject("$ne", null)).append("st",
				new BasicDBObject("$ne", "f"));

		addSubs(subscriptions, noLastResub);

		return subscriptions;
	}

	@Override
	public List<PollSubscription> findPolling(final Date time) {
		BasicDBObject query = new BasicDBObject();
		query.append("h", null);
		query.append("st", "s");
		query.append("np", new BasicDBObject("$lt", new Date()));
		ArrayList<PollSubscription> result = new ArrayList<>();
		DBCursor cursor = getColl().find(query);
		while (cursor.hasNext()) {
			result.add((PollSubscription) dbObjToSubscription(cursor.next()));
		}

		return result;
	}

	@Override
	public Subscription getByUrl(final String url) {
		final BasicDBObject query = new BasicDBObject();
		query.append("u", url);
		final DBObject res = getColl().findOne(query);
		if (res == null) {
			return null;
		}
		final Subscription subscription = dbObjToSubscription(res);
		return subscription;
	}

	private DBCollection getColl() {
		return getDataBase().getCollection(collection);
	}

	public String getCollection() {
		return collection;
	}

	private DB getDataBase() {
		return mongo.getDB(dbname);
	}

	public String getDbname() {
		return dbname;
	}

	public void init() {
		getColl().createIndex(new BasicDBObject("u", 1));
		BasicDBObject index = new BasicDBObject("h", 1);
		index.append("lr", 1).append("st", 1);
		getColl().createIndex(index);
	}

	@Override
	public void save(final Subscription subscription) {
		final BasicDBObject obj = subscriptionToDbObj(subscription);
		logger.debug("subscription saved to repo: {}", subscription.getUrl());
		getColl().save(obj);
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

	public void setDbname(final String dbname) {
		this.dbname = dbname;
	}

}
