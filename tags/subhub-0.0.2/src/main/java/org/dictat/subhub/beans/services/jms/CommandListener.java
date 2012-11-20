package org.dictat.subhub.beans.services.jms;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.dictat.subhub.beans.services.SubHub;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandListener implements MessageListener {

	private final static Logger logger = LoggerFactory
			.getLogger(CommandListener.class);

	public CommandListener(SubHub subHub) {
		super();
		this.subHub = subHub;
	}

	final SubHub subHub;

	public void onMessage(final Message message) {
		if (!(message instanceof TextMessage)) {
			logger.warn("message not TextMessage, ignoring");
			// ignore stupid messages
			return;
		}
		try {
			String msg = ((TextMessage) message).getText();
			if (msg.startsWith("+")) {
				subHub.subscribe(msg.substring(1));
			} else if (msg.startsWith("-")) {
				subHub.unsubscribe(msg.substring(1));
			} else if (msg.startsWith("!")) {
				for (String url : discover(msg.substring(1))) {
					subHub.subscribe(url);
				}
			}
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static Collection<String> discover(String domain) {
		HashSet<String> ret = new HashSet<String>();
		StringBuilder addrbuilder = new StringBuilder();
		if (!(domain.startsWith("http://") || domain.startsWith("https://"))) {
			addrbuilder.append("http://");
		}
		addrbuilder.append(domain);
		try {
			Document document = Jsoup.connect(addrbuilder.toString()).get();
			for (Element elem : document.select("link[rel]")) {
				String type = elem.attr("type");
				if ("application/atom+xml".equals(type)
						|| "application/rss+xml".equals(type)) {
					String hrefAttr = elem.attr("href");
					if (hrefAttr != null && hrefAttr.startsWith("http://")
							|| hrefAttr.startsWith("https://")) {
						ret.add(hrefAttr);
					} else {
						ret.add("http://" + domain + "/" + hrefAttr);
					}
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return ret;
	}

}
