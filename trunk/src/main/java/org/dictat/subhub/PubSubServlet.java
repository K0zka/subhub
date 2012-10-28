package org.dictat.subhub;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dictat.subhub.beans.services.SubHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = { "/subs" })
public class PubSubServlet extends BaseServlet {

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
			if (getSubHub().verifySubscription(topic, token)) {
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		getEventQueue().onPublish(req.getInputStream());
	}

}
