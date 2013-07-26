package com.expedia.bookings.utils;

public class HtmlUtils {
	// Wraps raw html in head and body tags
	// It adds the viewport configuration for a WebView to work properly
	public static String wrapInHeadAndBody(String html) {
		String noMargin = "0px";
		return wrapInHeadAndBodyWithMargins(html, noMargin, noMargin, noMargin, noMargin);
	}

	/**
	 * Wraps raw html in head and body tags that includes 10% margins on left and right, and a 20pt margin on top.
	 * It adds the viewport configuration for a WebView to work properly
	 * 
	 * @param html
	 * @return
	 */
	public static String wrapInHeadAndBodyWithStandardTabletMargins(String html) {
		String sideMargin = "10%";
		String topMargin = "20pt";
		String bottomMargin = "0pt";
		return HtmlUtils.wrapInHeadAndBodyWithMargins(html, topMargin, sideMargin, bottomMargin, sideMargin);
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

	// android.text.Html.escapeHtml was not introduced until API-16
	// So it is duplicated here.
	public static String escape(CharSequence text) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (c == '<') {
				sb.append("&lt;");
			}
			else if (c == '>') {
				sb.append("&gt;");
			}
			else if (c == '&') {
				sb.append("&amp;");
			}
			else if (c > 0x7E || c < ' ') {
				sb.append("&#" + ((int) c) + ";");
			}
			else if (c == ' ') {
				while (i + 1 < text.length() && text.charAt(i + 1) == ' ') {
					sb.append("&nbsp;");
					i++;
				}

				sb.append(' ');
			}
			else {
				sb.append(c);
			}
		}

		return sb.toString();
	}
}
