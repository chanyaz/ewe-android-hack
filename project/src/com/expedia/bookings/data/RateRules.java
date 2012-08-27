package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class RateRules implements JSONable {

	private Rate mRate;

	private List<CreditCardBrand> mAcceptedCreditCardBrands;

	private List<Policy> mPolicies;

	// Unique to HotelPal
	private String mServiceFeesText;
	private String mTaxesText;

	public Rate getRate() {
		return mRate;
	}

	public void setRate(Rate rate) {
		this.mRate = rate;
	}

	public String getServiceFeesText() {
		return mServiceFeesText;
	}

	public void setServiceFeesText(String serviceFeesText) {
		this.mServiceFeesText = serviceFeesText;
	}

	public String getTaxesText() {
		return mTaxesText;
	}

	public void setTaxesText(String taxesText) {
		this.mTaxesText = taxesText;
	}

	public void addAcceptedCreditCardBrand(CreditCardBrand brand) {
		if (mAcceptedCreditCardBrands == null) {
			mAcceptedCreditCardBrands = new ArrayList<CreditCardBrand>();
		}
		mAcceptedCreditCardBrands.add(brand);
	}

	public List<CreditCardBrand> getAcceptedCreditCardBrands() {
		return mAcceptedCreditCardBrands;
	}

	public void addPolicy(Policy policy) {
		if (mPolicies == null) {
			mPolicies = new ArrayList<Policy>();
		}
		mPolicies.add(policy);
	}

	public List<Policy> getPolicies() {
		return mPolicies;
	}

	public Policy getPolicy(int policyType) {
		if (mPolicies != null) {
			for (Policy policy : mPolicies) {
				if (policy.getType() == policyType) {
					return policy;
				}
			}
		}

		return null;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("serviceFeesText", mServiceFeesText);
			obj.putOpt("taxesText", mTaxesText);
			JSONUtils.putJSONableList(obj, "acceptedCreditCardBrands", mAcceptedCreditCardBrands);
			JSONUtils.putJSONableList(obj, "policies", mPolicies);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert RateRules object to JSON.", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean fromJson(JSONObject obj) {
		mServiceFeesText = obj.optString("serviceFeesText", null);
		mTaxesText = obj.optString("taxesText", null);

		mAcceptedCreditCardBrands = (List<CreditCardBrand>) JSONUtils.getJSONableList(obj, "acceptedCreditCardBrands",
				CreditCardBrand.class);
		mPolicies = (List<Policy>) JSONUtils.getJSONableList(obj, "policies", Policy.class);

		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}
}
