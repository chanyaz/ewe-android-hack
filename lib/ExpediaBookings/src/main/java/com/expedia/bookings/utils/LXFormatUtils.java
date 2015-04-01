package com.expedia.bookings.utils;

import java.util.List;

public class LXFormatUtils {

	public static final String HTML_TAGS_REGEX = "<[^>]*>";
	public static final String FULLSTOP_SPACE = ".\u0020";

	public static String formatHighlights(List<String> highlights) {
		StringBuilder sb = new StringBuilder();
		for (String highlight : highlights) {
			sb.append(stripHTMLTags(highlight)).append(FULLSTOP_SPACE);
		}
		return sb.toString();
	}

	public static String stripHTMLTags(String htmlContent) {
		return htmlContent.replaceAll(HTML_TAGS_REGEX, "");
	}

}
