package org.dictat.subhub.beans.services.feedpolice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebServlet(urlPatterns="/check")
public class FeedPoliceServlet extends HttpServlet {

	private static final long serialVersionUID = -5755840981793029803L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		final String url = URLDecoder.decode(req.getParameter("url"), "UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		resp.setContentType("application/json");
		if (isOk(url)) {
			FeedPolice feedPolice = (FeedPolice) getContext().getBean(
					"feedPolice");
			mapper.writer().writeValue(resp.getOutputStream(),
					feedPolice.checkFeed(url));
		} else {
			mapper.writer().writeValue(resp.getOutputStream(), "nope");
		}
	}

	static boolean isOk(String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		try {
			URL u = new URL(url);
			if (!"http".equals(u.getProtocol())
					&& !"https".equals(u.getProtocol())) {
				return false;
			}
			InetAddress address = InetAddress.getByName(u.getHost());
			if(address.isLoopbackAddress()) {
				return false;
			}
			address.isLinkLocalAddress();
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (UnknownHostException e) {
			return false;
		}
	}

	final WebApplicationContext getContext() {
		return WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());
	}

}
