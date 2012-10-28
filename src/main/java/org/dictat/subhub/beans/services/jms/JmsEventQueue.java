package org.dictat.subhub.beans.services.jms;

import java.io.IOException;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.dictat.subhub.beans.services.EventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class JmsEventQueue implements EventQueue {

	public JmsEventQueue(JmsTemplate jmsTemplate) {
		super();
		this.jmsTemplate = jmsTemplate;
	}

	final JmsTemplate jmsTemplate;
	private final static Logger logger = LoggerFactory
			.getLogger(JmsEventQueue.class);

	public void onPublish(final InputStream input) {
		logger.info("sending newsitem to queue");
		jmsTemplate.send(new MessageCreator() {

			public Message createMessage(Session session) throws JMSException {
				BytesMessage msg = session.createBytesMessage();
				try {
					byte[] buffer = new byte[4096];
					int cnt = input.read(buffer);
					while (cnt != -1) {
						if(cnt > 0) {
							msg.writeBytes(buffer, 0, cnt);
						}
						cnt = input.read(buffer);
					}
				} catch (IOException e) {
					logger.error("could not copy update", e);
				}
				return msg;
			}
		});
		logger.info("done");
	}

}
