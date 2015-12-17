package com.expedia.bookings.data;

public class AutocompleteSuggestion {
	public int mIcon;
	public String mText;
	public String mSearchJson;

	public void setIcon(int icon) {
		mIcon = icon;
	}

	public void setText(String text) {
		mText = text;
	}

	public void setSearchJson(String searchJson) {
		mSearchJson = searchJson;
	}

	public int getIcon() {
		return mIcon;
	}

	public String getText() {
		return mText;
	}

	public String getSearchJson() {
		return mSearchJson;
	}

}
