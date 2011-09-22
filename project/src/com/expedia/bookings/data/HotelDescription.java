package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;

import com.expedia.bookings.R;

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

	public void parseDescription(String description) {
		// List support
		description = description.replace("<ul>", "\n\n");
		description = description.replace("</ul>", "\n");
		description = description.replace("<li>", mContext.getString(R.string.bullet_point) + " ");
		description = description.replace("</li>", "\n");
		description = description.replace("<strong><strong>", "<strong>");
		description = description.replace("</strong>:</strong>", "</strong>");
		description = description.replace("<p></p>", "");

		int len = description.length();
		int index = 0;
		String title = null;
		while (index < len && index >= 0) {
			int nextSection = description.indexOf("<p>", index);
			int endSection = description.indexOf("</p>", nextSection);

			if (nextSection != -1 && endSection > nextSection) {
				int nextTitle = description.indexOf("<strong>", index);
				int endTitle = description.indexOf("</strong>", nextTitle);

				if (nextTitle != -1 && endTitle > nextTitle && endTitle < endSection) {
					title = description.substring(nextTitle + 8, endTitle).trim();
					if (title.endsWith(".")) {
						title = title.substring(0, title.length() - 1);
					}

					// Crazy hacks for the description.  Should be rewritten someday
					String body = description.substring(endTitle + 9, endSection).trim().replace("\n", "<br />");
					if (body.startsWith("<br>")) {
						body = body.substring(4);
					}
					if (body.startsWith("<br /><br />")) {
						body = body.substring(12);
					}
					body = body.replace("<p>", "<br />");

					if (title.length() > 0 && body.length() > 0) {
						mSections.add(new DescriptionSection(title, body));
						title = null;
					}
				}
				else {
					String body = description.substring(nextSection + 3, endSection).trim().replace("\n", "<br />");
					if (title != null && body.length() > 0) {
						mSections.add(new DescriptionSection(title, body));
						title = null;
					}
				}

				// Iterate
				index = endSection + 4;
			}
			else {
				// If there's something mysteriously at the end we can't parse, just append it
				String body = description.substring(index);

				// ensure not to add a string that is blank to the hote desription. This is possible
				// if the end of the description is padded with whitespaces
				if (isBlank(body)) {
					break;
				}

				mSections.add(new DescriptionSection("", body));
				break;
			}
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
