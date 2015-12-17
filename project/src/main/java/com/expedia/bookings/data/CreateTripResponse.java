package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.enums.MerchandiseSpam;
import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class CreateTripResponse extends Response implements JSONable {

	private String mTripId;
	private String mUserId;
	private Rate mOriginalRate;
	private Rate mNewRate;
	private Rate mAirAttachRate;
	private String mTealeafId;
	private String mRewardsPoints;
	private String mSupplierType;
	private List<ValidPayment> mValidPayments;
	private MerchandiseSpam mMerchandiseSpam;

	@Override
	public boolean isSuccess() {
		return !hasErrors();
	}

	public void setTripId(String tripId) {
		mTripId = tripId;
	}

	public String getTripId() {
		return mTripId;
	}

	public void setUserId(String userId) {
		mUserId = userId;
	}

	public String getUserId() {
		return mUserId;
	}

	public void setOriginalRate(Rate rate) {
		mOriginalRate = rate;
	}

	public Rate getOriginalRate() {
		return mOriginalRate;
	}

	public void setNewRate(Rate rate) {
		mNewRate = rate;
	}

	public Rate getNewRate() {
		return mNewRate;
	}

	public void setAirAttachRate(Rate rate) {
		mAirAttachRate = rate;
	}

	public Rate getAirAttachRate() {
		return mAirAttachRate;
	}

	public void setTealeafId(String id) {
		mTealeafId = id;
	}

	public String getTealeafId() {
		return mTealeafId;
	}

	public void setValidPayments(List<ValidPayment> validPayments) {
		mValidPayments = validPayments;
	}

	public void setRewardsPoints(String rewardsPoints) {
		mRewardsPoints = rewardsPoints;
	}

	public String getRewardsPoints() {
		return mRewardsPoints;
	}

	public void setSupplierType(String supplierType) {
		mSupplierType = supplierType;
	}

	public String getSupplierType() {
		return mSupplierType;
	}

	public List<ValidPayment> getValidPayments() {
		return mValidPayments;
	}

	public MerchandiseSpam getMerchandiseSpam() {
		return mMerchandiseSpam;
	}

	public void setMerchandiseSpam(MerchandiseSpam merchandiseSpam) {
		this.mMerchandiseSpam = merchandiseSpam;
	}

	public CreateTripResponse clone() {
		CreateTripResponse response = new CreateTripResponse();
		JSONObject json = toJson();
		response.fromJson(json);
		return response;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			obj.put("tripId", mTripId);
			obj.put("userId", mUserId);
			obj.put("tealeafId", mTealeafId);
			obj.putOpt("rewardsPoints", mRewardsPoints);
			JSONUtils.putJSONable(obj, "newRate", mNewRate);
			JSONUtils.putJSONable(obj, "originalRate", mOriginalRate);
			GsonUtil.putListForJsonable(obj, "validPayments", mValidPayments);
			JSONUtils.putJSONable(obj, "airAttachRate", mAirAttachRate);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert HotelSearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mTripId = obj.optString("tripId", null);
		mUserId = obj.optString("userId", null);
		mTealeafId = obj.optString("tealeafId", null);
		mRewardsPoints = obj.optString("rewardsPoints");
		mNewRate = JSONUtils.getJSONable(obj, "newRate", Rate.class);
		mOriginalRate = JSONUtils.getJSONable(obj, "originalRate", Rate.class);
		mValidPayments = GsonUtil.getListForJsonable(obj, "validPayments", ValidPayment.gsonListTypeToken);
		mAirAttachRate = JSONUtils.getJSONable(obj, "airAttachRate", Rate.class);
		return true;
	}
}
