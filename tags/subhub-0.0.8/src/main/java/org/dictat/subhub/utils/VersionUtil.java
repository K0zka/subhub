package org.dictat.subhub.utils;

public class VersionUtil {
	private static final String versionUrl = "$HeadURL$";

	public final static String getVersion() {
		final int srcRootPos = versionUrl.indexOf("/src/", 0);
		final int lastSlash = versionUrl.lastIndexOf("/", srcRootPos - 1);
		return versionUrl.substring(lastSlash + 1, srcRootPos);
	}
}
