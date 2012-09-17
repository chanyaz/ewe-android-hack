package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.Context;

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
		// See MOHotelDescription.m
		final String bullet = "<br/>" + mContext.getString(R.string.bullet_point) + " ";
		final String justBullet = mContext.getString(R.string.bullet_point) + " ";

		// Reset sections
		mSections = new ArrayList<DescriptionSection>();

		DescriptionSection firstSection = null;
		DescriptionSection amenitiesSection = null;
		DescriptionSection policiesSection = null;
		DescriptionSection feesSection = null;

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
						if (firstSection == null) {
							firstSection = new DescriptionSection(sectionString, str.toString().trim());
						}
						else if (SectionStrings.isValidPropertyAmenitiesString(sectionString)) {
							amenitiesSection = new DescriptionSection(sectionString, str.toString().trim());
						}
						else if (SectionStrings.isValidPoliciesString(sectionString)) {
							policiesSection = new DescriptionSection(sectionString, str.toString().trim());
						}
						else if (SectionStrings.isValidFeesString(sectionString)) {
							feesSection = new DescriptionSection(sectionString, str.toString().trim());
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
					} else if (html.substring(i).startsWith("<B>") || html.substring(i).startsWith("<b>")) {
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
			if (firstSection == null) {
				firstSection = new DescriptionSection(sectionString, str.toString().trim());
			}
			else if (SectionStrings.isValidPropertyAmenitiesString(sectionString)) {
				amenitiesSection = new DescriptionSection(sectionString, str.toString().trim());
			}
			else if (SectionStrings.isValidPoliciesString(sectionString)) {
				policiesSection = new DescriptionSection(sectionString, str.toString().trim());
			}
			else if (SectionStrings.isValidFeesString(sectionString)) {
				feesSection = new DescriptionSection(sectionString, str.toString().trim());
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
