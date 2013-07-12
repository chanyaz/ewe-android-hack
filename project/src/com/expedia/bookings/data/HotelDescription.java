package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.R;

public class HotelDescription {

	private ArrayList<HotelTextSection> mSections = new ArrayList<HotelTextSection>();
	private Context mContext;

	public static class SectionStrings {
		private static HashSet<String> sPropertyAmenitiesStrings;
		private static HashSet<String> sFeesStrings;
		private static HashSet<String> sPoliciesStrings;

		public static void initSectionStrings(Context context) {
			if (sPropertyAmenitiesStrings == null) {
				sPropertyAmenitiesStrings = new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.section_strings_property_amenities)));
			}
			if (sFeesStrings == null) {
				sFeesStrings = new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.section_strings_fees)));
			}
			if (sPoliciesStrings == null) {
				sPoliciesStrings = new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.section_strings_policies)));
			}
		}

		public static boolean isValidPropertyAmenitiesString(String str) {
			return sPropertyAmenitiesStrings == null ? false : sPropertyAmenitiesStrings.contains(str);
		}
		public static boolean isValidFeesString(String str) {
			return sFeesStrings == null ? false : sFeesStrings.contains(str);
		}
		public static boolean isValidPoliciesString(String str) {
			return sPoliciesStrings == null ? false : sPoliciesStrings.contains(str);
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
	 * FIXME: May have out-of-bounds exceptions with the indexOf operations.
	 */
	public void parseDescription(String html) {
		// Reset sections
		mSections = new ArrayList<HotelTextSection>();

		HotelTextSection firstSection = null;
		HotelTextSection amenitiesSection = null;
		HotelTextSection policiesSection = null;
		HotelTextSection feesSection = null;

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

			if (tag.length() == 0) {
				continue; // drop the tag, it is empty
			}

			switch (tag.charAt(0)) {
			case 'l': // li
				str.append("<li>");
				break;
			case 'u': // ul
				str.append("<ul>");
				break;
			case 's': // strong
				if (tag.equals("strong")) {
					if (sectionString != null && str.length() > 0) {
						if (firstSection == null) {
							firstSection = new HotelTextSection(sectionString, str.toString().trim());
						}
						else if (SectionStrings.isValidPropertyAmenitiesString(sectionString)) {
							amenitiesSection = new HotelTextSection(sectionString, str.toString().trim());
						}
						else if (SectionStrings.isValidPoliciesString(sectionString)) {
							policiesSection = new HotelTextSection(sectionString, str.toString().trim());
						}
						else if (SectionStrings.isValidFeesString(sectionString)) {
							feesSection = new HotelTextSection(sectionString, str.toString().trim());
						}

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
					}
					else if (html.substring(i).startsWith("<B>") || html.substring(i).startsWith("<b>")) {
						// Parse section
						i += 3;
						start = html.indexOf('<', i);
						sectionString = html.substring(i, start);
						end = html.indexOf('<', start + 1);
						end = html.indexOf('>', end + 1);
						i = end + 1;
					}
					else {
						start = html.indexOf('<', i);
						end = html.indexOf('>', start);
						// #1126. Hotel details section title with a ":" skip it, don't show.
						if (html.substring(i, start).equals(":")) {
							i++;
						}
						sectionString = html.substring(i, start);
						i = end + 1;
					}
				}
				break;
			case '/':
				if (tag.equals("/ul")) {
					str.append("</ul>");
				}
				else if (tag.equals("/li")) {
					str.append("</li>");
				}
				break;
			default:
				// drop the tag
				break;
			}
		}

		if (sectionString != null && str.length() > 0) {
			if (firstSection == null) {
				firstSection = new HotelTextSection(sectionString, str.toString().trim());
			}
			else if (SectionStrings.isValidPropertyAmenitiesString(sectionString)) {
				amenitiesSection = new HotelTextSection(sectionString, str.toString().trim());
			}
			else if (SectionStrings.isValidPoliciesString(sectionString)) {
				policiesSection = new HotelTextSection(sectionString, str.toString().trim());
			}
			else if (SectionStrings.isValidFeesString(sectionString)) {
				feesSection = new HotelTextSection(sectionString, str.toString().trim());
			}
		}

		if (firstSection != null) {
			mSections.add(firstSection);
		}
		if (amenitiesSection != null) {
			mSections.add(amenitiesSection);
		}
		if (policiesSection != null) {
			mSections.add(policiesSection);
		}
		if (feesSection != null) {
			mSections.add(feesSection);
		}
	}

	public List<HotelTextSection> getSections() {
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
