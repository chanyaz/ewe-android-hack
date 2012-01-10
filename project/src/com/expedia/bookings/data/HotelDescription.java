package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

public class HotelDescription {

	private ArrayList<DescriptionSection> mSections = new ArrayList<DescriptionSection>();
	private Context mContext;

	public static class DescriptionSection {
		public String title;
		public String description;

		public DescriptionSection(String title, String body) {
			this.title = new String(title);
			this.description = new String(body);
		}
	}

	public HotelDescription(Context context) {
		mContext = context;
	}

	public void parseDescription(String html) {
		// See MOHotelDescription.m
		String bullet = mContext.getString(R.string.bullet_point);

        // fix up notifications html, otherwise some gets cut off
		html = html.replace("<br>", "\n");
		html = html.replace("<br />", "\n");
		html = html.replace("<p>", "\n");
		html = html.replace("</p>", "\n");

		// list support
		html = html.replace("<ul>", "\n\n");
		html = html.replace("</li>", "\n");
		html = html.replace("</ul>", "\n");
		html = html.replace("<li>", bullet + " ");

		// sometimes section headers are wrapped in <b></b> instead of <strong></strong>
		html = html.replace("<b>", "<strong>");
		html = html.replace("</b>", "</strong>");
		html = html.replace("<B>", "<strong>");
		html = html.replace("</B>", "</strong>");

		int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
		Pattern sectionPattern = Pattern.compile("<strong>(.*?)</strong>(.*?)(?=\\z|<strong>)", flags);
		Matcher sectionMatcher = sectionPattern.matcher(html);

		Pattern anyTag = Pattern.compile("<.+?>", flags);

		String title, body;
		while (sectionMatcher.find()) {

			// Parse title
			title = sectionMatcher.group(1);
			title = title.replaceAll(anyTag.pattern(), "");
			title = title.trim();
			if (title.endsWith(".") || title.endsWith(":")) {
				title = title.substring(0, title.length() - 1);
			}

			// Parse body
			body = sectionMatcher.group(2);
			body = body.trim();
			body = body.replaceAll(bullet + "\\s*" + bullet, bullet);
			//body = body.replace("\n" + bullet, "\n<br />" + bullet);
			body = body.replaceAll("^\\s*:\\s*</strong>\\s*", "");

			if (isBlank(title) || isBlank(body)) {
				continue;
			}

			mSections.add(new DescriptionSection(title, body));
		}
	}

	public List<DescriptionSection> getSections() {
		return mSections;
	}

	/*
	 * This method returns true if the string is blank.
	 * Ideally, I'd use the StringUtils.isBlank method but didnt want 
	 * to pull in the commons jar just for this. 
	 */
	private boolean isBlank(String str) {
		for (char a : str.toCharArray()) {
			if (a != ' ' && a != '\n') {
				return false;
			}
		}
		return true;
	}
}
