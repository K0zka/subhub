package org.dictat.subhub.beans.services.dummy;

import java.io.InputStream;

import org.dictat.subhub.beans.services.EventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyEventQueue implements EventQueue {

	private final static Logger logger = LoggerFactory
			.getLogger(DummyEventQueue.class);

	@Override
	public void onPublish(InputStream inputStream) {
		logger.info(".");
	}

}
