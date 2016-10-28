package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.text.HtmlCompat;
import com.mobiata.android.json.JSONable;

public class HotelTextSection implements JSONable {

	private String mName;

	private String mContent;

	public HotelTextSection() {
		// Default constructor
	}

	public HotelTextSection(String name, String content) {
		mName = name;
		mContent = content;
	}

	public String getName() {
		return mName;
	}

	public String getNameWithoutHtml() {
		if (mName != null) {
			return HtmlCompat.stripHtml(mName);
		}
		return null;
	}

	public String getContent() {
		return mContent;
	}

	public String getContentFormatted(Context context) {
		final String bullet = "<br/>" + context.getString(R.string.bullet_point) + " ";
		final String justBullet = context.getString(R.string.bullet_point) + " ";

		// We strive to reformat the bullet points
		StringBuilder str = new StringBuilder(2048);
		String tag;
		int length = mContent.length();
		int i = 0;
		int start = -1;
		int end = -1;

		while (i < length) {
			start = mContent.indexOf('<', i);
			if (start < 0) {
				if (end == -1) {
					// Special case - there are no tags - append *all* content
					str.append(mContent);
				}
				else if (end + 1 < length) {
					// Append the rest of the content after the last tag
					str.append(mContent.substring(end + 1));
				}

				break;
			}
			end = mContent.indexOf('>', start);
			if (start > i) {
				str.append(mContent.substring(i, start));
			}
			i = end + 1;
			tag = mContent.substring(start + 1, end);

			if (tag.length() == 0) {
				continue; // drop the tag, it is empty
			}

			switch (tag.charAt(0)) {
			case 'l': // li
				if ('i' == tag.charAt(1) && tag.length() == 2) {
					if (!mContent.substring(i).startsWith("<ul>")) {
						if (str.length() > 0) {
							str.append(bullet);
						}
						else {
							str.append(justBullet);
						}
					}
				}
				break;
			case 'u': // ul
				if ('l' == tag.charAt(1) && tag.length() == 2) {
					if (mContent.substring(i).startsWith("</ul>")) {
						// Skip this noise
						i += 5;
					}
					else if (str.length() > 0) {
						str.append("<br/>");
					}
				}
				break;
			case '/':
				if (tag.equals("/ul")) {
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

		return str.toString().trim();
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("name", mName);
			obj.putOpt("content", mContent);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean fromJson(JSONObject obj) {
		mName = obj.optString("name", null);
		mContent = obj.optString("content", null);
		return true;
	}
}
