package org.dictat.subhub;

import javax.servlet.http.HttpServlet;

import org.dictat.subhub.beans.services.EventQueue;
import org.dictat.subhub.beans.services.SubHub;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public abstract class BaseServlet extends HttpServlet {
	private static final long serialVersionUID = 7250937984773922291L;

	protected SubscriptionRepository getSubscriptionRepository() {
		return (SubscriptionRepository) getContect().getBean(
						"repo");
	}

	final WebApplicationContext getContect() {
		return WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());
	}

	protected SubHub getSubHub() {
		return (SubHub) getContect().getBean(
						"subhub");
	}

	protected EventQueue getEventQueue() {
		return (EventQueue) getContect().getBean(
						"eventQueue");
	}

}
