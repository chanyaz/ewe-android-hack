package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class HotelAvailability implements JSONable {
	private HotelOffersResponse mHotelOffersResponse;
	private Rate mSelectedRate;

	// Do not persist
	private Map<String, Rate> mRateMap;

	public void setHotelOffersResponse(HotelOffersResponse response) {
		mHotelOffersResponse = response;

		mRateMap = new HashMap<String, Rate>();
		for (Rate rate : response.getRates()) {
			mRateMap.put(rate.getRateKey(), rate);
		}
	}

	public HotelOffersResponse getHotelOffersResponse() {
		return mHotelOffersResponse;
	}

	public List<Rate> getRates() {
		return mHotelOffersResponse.getRates();
	}

	public int getRateCount() {
		return mHotelOffersResponse.getRates().size();
	}

	public Rate getRate(String id) {
		return mRateMap.get(id);
	}

	public void setSelectedRate(Rate rate) {
		mSelectedRate = rate;
	}

	public Rate getSelectedRate() {
		return mSelectedRate;
	}

	public void removeRate(String rateKey) {
		Rate deadRate = mRateMap.get(rateKey);
		mRateMap.remove(rateKey);
		mHotelOffersResponse.removeRate(deadRate);
	}

	public void updateFrom(String rateKey, Rate newRate) {
		if (mHotelOffersResponse != null) {
			Rate oldRate = Strings.isEmpty(rateKey) ? null : getRate(rateKey);

			if (oldRate == null) {
				mHotelOffersResponse.addRate(newRate);
			}
			else {
				// Need to copy these because the api does not return with hotel CreateTrip
				newRate.setValueAdds(oldRate.getValueAdds());

				mHotelOffersResponse.replaceRate(oldRate, newRate);
				mRateMap.remove(oldRate.getRateKey());
				mRateMap.put(newRate.getRateKey(), newRate);
			}
		}
	}

	public HotelAvailability clone() {
		HotelAvailability availability = new HotelAvailability();
		JSONObject jsonObject = this.toJson();
		availability.fromJson(jsonObject);
		return availability;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "hotelOffersResponse", mHotelOffersResponse);

			if (mSelectedRate != null) {
				String selectedRateId = mSelectedRate.getRateKey();
				obj.putOpt("selectedRateId", selectedRateId);
			}

			return obj;
		}
		catch (JSONException e) {
			Log.e("HotelSearch toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		setHotelOffersResponse(JSONUtils.getJSONable(obj, "hotelOffersResponse", HotelOffersResponse.class));

		String selectedRateId = obj.optString("selectedRateId", null);
		if (!TextUtils.isEmpty(selectedRateId)) {
			Rate rate = getRate(selectedRateId);
			setSelectedRate(rate);
		}

		return true;
	}
}

