package com.expedia.bookings.utils;

public class HtmlUtils {
	// Wraps raw html in head and body tags
	// It adds the viewport configuration for a WebView to work properly
	public static String wrapInHeadAndBody(String html) {
		StringBuilder sb = new StringBuilder();
		sb.append("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\"></head>");
		sb.append("<body>");
		sb.append(html);
		sb.append("</body>");
		return sb.toString();
	}
}

