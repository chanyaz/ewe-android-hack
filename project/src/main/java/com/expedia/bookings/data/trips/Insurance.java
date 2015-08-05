package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Insurance implements JSONable {

	public enum InsuranceLineOfBusiness {
		AIR, UNKNOWN
	}

	private InsuranceLineOfBusiness mLineOfBusiness;
	private String mPolicyName;
	private String mTermsUrl;

	public String getPolicyName() {
		return mPolicyName;
	}

	public String getTermsUrl() {
		return mTermsUrl;
	}

	public InsuranceLineOfBusiness getLineOfBusiness() {
		return mLineOfBusiness;
	}

	public void setPolicyName(String name) {
		mPolicyName = name;
	}

	public void setTermsUrl(String url) {
		mTermsUrl = url;
	}

	public void setInsuranceLineOfBusiness(String lineOfBusiness) {
		if (lineOfBusiness.compareToIgnoreCase(InsuranceLineOfBusiness.AIR.name()) == 0) {
			mLineOfBusiness = InsuranceLineOfBusiness.AIR;
		}
		else {
			mLineOfBusiness = InsuranceLineOfBusiness.UNKNOWN;
		}
	}

	public void setInsuraceLineOfBusiness(InsuranceLineOfBusiness lineOfBusiness) {
		mLineOfBusiness = lineOfBusiness;
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		try {
			json.putOpt("displayName", mPolicyName);
			json.putOpt("termsUrl", mTermsUrl);
			JSONUtils.putEnum(json, "lineOfBusiness", mLineOfBusiness);
		}
		catch (JSONException e) {
			Log.e("Exception in toJson()", e);
		}

		return json;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mPolicyName = obj.optString("displayName");
		mTermsUrl = obj.optString("termsUrl");
		mLineOfBusiness = JSONUtils.getEnum(obj, "lineOfBusiness", InsuranceLineOfBusiness.class);
		return true;
	}

}
