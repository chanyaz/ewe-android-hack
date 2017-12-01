package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class Rule implements JSONable {

	private String mName;
	private String mText;
	private String mUrl;
	private String mTextAndURL;

	public Rule() {
		// Default constructor for JSONable
	}

	public String getTextAndURL() {
		return mTextAndURL;
	}

	public void setTextAndURL(String mTextAndURL) {
		this.mTextAndURL = mTextAndURL;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		mText = text;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	@Override
	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			obj.putOpt("name", mName);
			obj.putOpt("text", mText);
			obj.putOpt("url", mUrl);
			obj.putOpt("textAndURL", mTextAndURL);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return obj.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("name", mName);
			obj.putOpt("text", mText);
			obj.putOpt("url", mUrl);
			obj.putOpt("textAndURL", mTextAndURL);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mName = obj.optString("name", null);
		mText = obj.optString("text", null);
		mUrl = obj.optString("url", null);
		mTextAndURL = obj.optString("textAndURL",null);
		return true;
	}

}
