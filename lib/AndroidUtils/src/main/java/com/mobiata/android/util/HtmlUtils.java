package com.mobiata.android.util;

public class HtmlUtils {
	// Wraps raw html in head and body tags
	// It adds the viewport configuration for a WebView to work properly
	public static String wrapInHeadAndBody(String html) {
		String noMargin = "0px";
		return wrapInHeadAndBodyWithMargins(html, noMargin, noMargin, noMargin, noMargin);
	}

	/**
	 * Wraps raw html in head and body tags
	 * It adds the viewport configuration for a WebView to work properly
	 * 
	 * We add html margin styles to the body container.
	 * 
	 * @param html
	 * @param marginTop
	 * @param marginRight
	 * @param marginBottom
	 * @param marginLeft
	 * @return
	 */
	public static String wrapInHeadAndBodyWithMargins(String html, String marginTop, String marginRight,
			String marginBottom, String marginLeft) {
		StringBuilder sb = new StringBuilder();
		sb.append("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\"></head>");
		sb.append("<body style='margin:" + marginTop + " " + marginRight + " " + marginBottom + " " + marginLeft
				+ ";'>");
		sb.append(html);
		sb.append("</body>");
		return sb.toString();
	}
}
