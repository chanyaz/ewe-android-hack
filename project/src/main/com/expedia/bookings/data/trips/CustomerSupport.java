package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class CustomerSupport implements JSONable {

	private static final String JSON_SUPPORT_URL = "customerSupportURL";
	private static final String JSON_SUPPORT_PHONE_INFO = "customerSupportPhoneInfo";
	private static final String JSON_SUPPORT_PHONE_DOMESTIC = "customerSupportPhoneNumberDomestic";
	private static final String JSON_SUPPORT_PHONE_INTERNATIONAL = "customerSupportPhoneNumberInternational";

	private String mCustomerSupportUrl;
	private String mCustomerSupportPhoneInfo;
	private String mCustomerSupportPhoneNumberDomestic;
	private String mCustomerSupportPhoneNumberInternational;

	public CustomerSupport() {

	}

	public void setSupportUrl(String url) {
		mCustomerSupportUrl = url;
	}

	public String getSupportUrl() {
		return mCustomerSupportUrl;
	}

	public void setSupportPhoneInfo(String phoneInfo) {
		mCustomerSupportPhoneInfo = phoneInfo;
	}

	public String getSupportPhoneInfo() {
		return mCustomerSupportPhoneInfo;
	}

	public void setSupportPhoneNumberDomestic(String number) {
		mCustomerSupportPhoneNumberDomestic = number;
	}

	public String getSupportPhoneNumberDomestic() {
		return mCustomerSupportPhoneNumberDomestic;
	}

	public void setSupportPhoneNumberInternational(String number) {
		mCustomerSupportPhoneNumberInternational = number;
	}

	public String getSupportPhoneNumberInternational() {
		return mCustomerSupportPhoneNumberInternational;
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		try {
			json.putOpt(JSON_SUPPORT_URL, mCustomerSupportUrl);
			json.putOpt(JSON_SUPPORT_PHONE_INFO, mCustomerSupportPhoneInfo);
			json.putOpt(JSON_SUPPORT_PHONE_DOMESTIC, mCustomerSupportPhoneNumberDomestic);
			json.putOpt(JSON_SUPPORT_PHONE_INTERNATIONAL, mCustomerSupportPhoneNumberInternational);
		}
		catch (JSONException ex) {
			Log.e("JSONException", ex);
		}
		return json;
	}

	@Override
	public boolean fromJson(JSONObject json) {
		mCustomerSupportUrl = json.optString(JSON_SUPPORT_URL, null);
		mCustomerSupportPhoneInfo = json.optString(JSON_SUPPORT_PHONE_INFO, null);
		mCustomerSupportPhoneNumberDomestic = json.optString(JSON_SUPPORT_PHONE_DOMESTIC, null);
		mCustomerSupportPhoneNumberInternational = json.optString(JSON_SUPPORT_PHONE_INTERNATIONAL, null);
		return true;
	}

}
