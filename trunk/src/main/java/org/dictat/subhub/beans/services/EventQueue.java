package org.dictat.subhub.beans.services;

import java.io.InputStream;

public interface EventQueue {
	public void onPublish(InputStream inputStream);
}
