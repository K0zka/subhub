package org.dictat.subhub.beans.services.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
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

	public MongoSubscriptionRepository(final Mongo mongo) {
		this.mongo = mongo;
	}

	final Mongo mongo;
	private String dbname = "subhub";
	private String collection = "subs";
	private final static Logger logger = LoggerFactory
			.getLogger(MongoSubscriptionRepository.class);

	public void init() {
		BasicDBObject keys = new BasicDBObject();
		keys.append("url", 1);
		getColl().createIndex(keys);
	}

	private DBCollection getColl() {
		return getDataBase().getCollection(collection);
	}

	public Subscription getByUrl(String url) {
		final BasicDBObject query = new BasicDBObject();
		query.append("u", url);
		DBObject res = getColl().findOne(query);
		if (res == null) {
			return null;
		}
		final Subscription subscription = dbObjToSubscription(res);
		return subscription;
	}

	private Subscription dbObjToSubscription(DBObject res) {
		final Subscription subscription = new Subscription();
		subscription.setId(stringOrNull(res, "_id"));
		subscription.setHub(stringOrNull(res, "h"));
		subscription.setUrl(stringOrNull(res, "u"));
		subscription.setLease((Integer) (res.get("l")));
		subscription.setLastResubscribe((Date) res.get("lr"));
		subscription.setSubscribed((Date) res.get("s"));
		subscription.setVerifyToken(stringOrNull(res, "v"));
		subscription.setStatus( StatusUtils.toStatus( stringOrNull(res, "st")));
		subscription.setStatusChange((Date) res.get("stc"));
		return subscription;
	}

	private String stringOrNull(DBObject res, String key) {
		return res.get(key) == null ? null : res.get(key).toString();
	}

	private DB getDataBase() {
		return mongo.getDB(dbname);
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public void save(Subscription subscription) {
		BasicDBObject obj = new BasicDBObject();
		obj.append("h", subscription.getHub())
				.append("v", subscription.getVerifyToken())
				.append("s", subscription.getSubscribed())
				.append("lr", subscription.getLastResubscribe())
				.append("l", subscription.getLease())
				.append("u", subscription.getUrl())
				.append("st", StatusUtils.toLetter(subscription.getStatus()))
				.append("stc", subscription.getStatusChange());
		if (subscription.getId() != null) {
			obj.append("_id", new ObjectId(subscription.getId()));
		}
		logger.debug("subscription saved to repo: {}", subscription.getUrl());
		getColl().save(obj);
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public List<Subscription> findExpring(Date time) {
		ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
		BasicDBObject query = new BasicDBObject();
		BasicDBObject condition = new BasicDBObject();
		condition.append("$lt", time);
		query.append("lr", condition);
		addSubs(subscriptions, query);

		BasicDBObject nullQuery = new BasicDBObject();
		nullQuery.append("lr", null);
		addSubs(subscriptions, nullQuery);

		return subscriptions;
	}

	private void addSubs(List<Subscription> subscriptions, BasicDBObject query) {
		try (DBCursor cursor = getColl().find(query)) {
			while (cursor.hasNext()) {
				subscriptions.add(dbObjToSubscription(cursor.next()));
			}
		}
	}

}
