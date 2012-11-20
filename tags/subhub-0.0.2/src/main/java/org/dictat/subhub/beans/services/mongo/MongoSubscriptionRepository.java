package org.dictat.subhub.beans.services.mongo;

import java.util.Date;

import org.dictat.subhub.beans.Subscription;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
		final Subscription subscription = new Subscription();
		subscription.setHub(res.get("h").toString());
		subscription.setUrl(res.get("u").toString());
		subscription.setLease((Integer) (res.get("l")));
		subscription.setLastResubscribe((Date) res.get("lr"));
		subscription.setSubscribed((Date) res.get("s"));
		subscription.setVerifyToken((String) res.get("v"));
		return subscription;
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
				.append("u", subscription.getUrl());
		logger.debug("subscription saved to repo: {}", subscription.getUrl());
		getColl().save(obj);
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

}
