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

	/* Preliminary version of this parser. To get it to be a little more
	 * general and easy to use (read slower) add a stack of StringBuilders for
	 * when you dig deeper into nested tags. Allows pruning of empty leaves
	 * easily as well.
	 *
	 * FIXME: May have out-of-bounds issues with the indexOf operations.
	 */
	public void parseDescription(String html) {
		// See MOHotelDescription.m
		String bullet = "<br/>" + mContext.getString(R.string.bullet_point) + " ";
		String justBullet = mContext.getString(R.string.bullet_point) + " ";

		// Reset sections
		mSections = new ArrayList<DescriptionSection>();

		StringBuilder str = new StringBuilder(2048);
		String tag;
		String sectionString = null;
		int length = html.length();
		int i = 0;
		int start, end;

		while (i < length) {
			start = html.indexOf('<', i);
			if (start < 0) {
				break;
			}
			end = html.indexOf('>', start);
			if (start > i) {
				str.append(html.substring(i, start));
			}
			i = end + 1;
			tag = html.substring(start + 1, end);

			switch(tag.charAt(0)){
			case 'l': // li
				if ('i' == tag.charAt(1) && tag.length() == 2) {
					if (! html.substring(i).startsWith("<ul>")){
						if (str.length() > 0) {
							str.append(bullet);
						} else {
							str.append(justBullet);
						}
					}
				}
				break;
			case 'u': // ul
				if ('l' == tag.charAt(1) && tag.length() == 2) {
					if (html.substring(i).startsWith("</ul>")) {
						// Skip this noise
						i += 5;
					} else if (str.length() > 0) {
						str.append("<br/>");
					}
				}
				break;
			case 's': // strong
				if (tag.equals("strong")) {
					if (sectionString != null && str.length() > 0) {
						mSections.add(new DescriptionSection(sectionString, str.toString().trim()));
						str = str.delete(0, str.length());
						sectionString = null;
					}
					if (html.substring(i).startsWith("<strong>")) {
						// Parse section
						i += 8;
						start = html.indexOf('<', i);
						sectionString = html.substring(i, start);
						end = html.indexOf('<', start + 1);
						end = html.indexOf('>', end + 1);
						i = end + 1;
					} else if (html.substring(i).startsWith("<B>")) {
						// Parse section
						i += 3;
						start = html.indexOf('<', i);
						sectionString = html.substring(i, start);
						end = html.indexOf('<', start + 1);
						end = html.indexOf('>', end + 1);
						i = end + 1;
					} else {
						start = html.indexOf('<', i);
						end = html.indexOf('>', start);
						sectionString = html.substring(i, start);
						i = end + 1;
					}
				}
				break;
			case '/':
				if(tag.equals("/ul")){
					if ('l' == tag.charAt(2) && tag.length() == 3) {
						str.append("<br/>");
					}
				}
				break;
			default:
				// drop the tag
				break;
			}
		}

		if (sectionString != null && str.length() > 0) {
			mSections.add(new DescriptionSection(sectionString, str.toString().trim()));
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
