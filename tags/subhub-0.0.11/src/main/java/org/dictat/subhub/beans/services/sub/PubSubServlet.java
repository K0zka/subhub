package org.dictat.subhub.beans.services.sub;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dictat.rsscut.RssCut;
import org.dictat.scala4j.IterableAdapter;
import org.dictat.subhub.beans.PushSubscription;
import org.dictat.subhub.beans.services.EventQueue;
import org.dictat.subhub.beans.services.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebServlet(urlPatterns = { "/subs" })
public class PubSubServlet extends HttpServlet {

	private static final long serialVersionUID = -3012384220776305761L;

	private static final Logger logger = LoggerFactory
			.getLogger(PubSubServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String mode = req.getParameter(SubHub.mode);
		if (SubHub.modeSubscribe.equals(mode)) {
			final String topic = req.getParameter(SubHub.topic);
			final String token = req.getParameter(SubHub.verifyToken);
			if (verifySubscription(topic, token)) {
				logger.info("succesful verification for {}", topic);
				resp.setContentType("text/plain");
				resp.getWriter().print(req.getParameter(SubHub.challenge));
			} else {
				logger.warn("failed verification for {} with token {}", topic,
						token);
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				resp.getWriter().println("rejected");
			}
		}
	}

	private boolean verifySubscription(String url, String verification) {
		logger.info("requested verification for {} with code {}", url,
				verification);
		PushSubscription subscription = (PushSubscription) getRepository()
				.getByUrl(url);
		if (subscription == null) {
			return false;
		}
		return verification != null
				&& verification.equals(subscription.getVerifyToken());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		final String post = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
		for(final String news : new IterableAdapter<String>(RssCut.cut(post))) {
			getEventQueue().onPublish(IOUtils.toInputStream(news));
		}
	}

	//spring-related utility functions
	//because I was lazy and I did not want to use MVC
	
	final WebApplicationContext getContext() {
		return WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());
	}

	protected SubscriptionRepository getRepository() {
		return (SubscriptionRepository) getContext().getBean(
						"repo");
	}

	protected SubHub getSubHub() {
		return (SubHub) getContext().getBean(
						"subhub");
	}

	protected EventQueue getEventQueue() {
		return (EventQueue) getContext().getBean(
						"eventQueue");
	}

}
