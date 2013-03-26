package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Html;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.FormatUtils.Conjunction;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class AvailabilityResponse extends Response {
	private List<Rate> mRates;
	private Map<String, Rate> mRateMap;

	private Property mProperty;

	private SummarizedRoomRates mSummarizedRoomRates;

	public void addRate(Rate rate) {
		if (mRates == null) {
			mRates = new ArrayList<Rate>();
		}
		mRates.add(rate);

		// If ratemap is already initialized, add it here as well
		if (mRateMap != null) {
			mRateMap.put(rate.getRateKey(), rate);
		}
	}

	public void updateRate(String rateKey, Rate newRate) {
		Rate oldRate = getRate(rateKey);
		if (oldRate == null) {
			addRate(newRate);
		}
		else {
			mRates.set(mRates.indexOf(oldRate), newRate);

			mRateMap.remove(oldRate.getRateKey());
			mRateMap.put(newRate.getRateKey(), newRate);
		}
	}

	public void removeRate(String rateKey) {
		Rate rate = getRate(rateKey);
		if (mRateMap != null) {
			mRateMap.remove(rateKey);
		}
		if (rate != null) {
			mRates.remove(rate);
		}
	}

	public int getRateCount() {
		if (mRates == null) {
			return 0;
		}
		return mRates.size();
	}

	public Rate getRate(int index) {
		if (mRates == null) {
			return null;
		}
		return mRates.get(index);
	}

	public Rate getRate(String rateKey) {
		if (rateKey == null || rateKey.length() == 0) {
			return null;
		}

		// We don't bother initializing this until it's used, since it's
		// just a different data representation of the rate list.
		if (mRateMap == null) {
			mRateMap = new HashMap<String, Rate>();

			if (mRates != null) {
				for (Rate rate : mRates) {
					mRateMap.put(rate.getRateKey(), rate);
				}
			}
		}

		return mRateMap.get(rateKey);
	}

	public List<Rate> getRates() {
		return mRates;
	}

	public SummarizedRoomRates getSummarizedRoomRates() {
		if (mSummarizedRoomRates == null) {
			mSummarizedRoomRates = new SummarizedRoomRates();
			mSummarizedRoomRates.updateSummarizedRoomRates(this);
		}

		return mSummarizedRoomRates;
	}

	public void setProperty(Property property) {
		mProperty = property;
	}

	public Property getProperty() {
		return mProperty;
	}

	/**
	 * Gathers the list of common value adds between all Rates contained within.
	 *
	 * @return the list of common value adds between all Rates.  Null if there are none.
	 */
	public List<String> getCommonValueAdds() {
		int size = getRateCount();
		if (size > 0) {
			List<String> commonValueAdds = new ArrayList<String>(mRates.get(0).getValueAdds());

			for (int a = 1; a < size; a++) {
				commonValueAdds.retainAll(mRates.get(a).getValueAdds());
			}

			if (commonValueAdds.size() > 0) {
				return commonValueAdds;
			}
		}

		return null;
	}

	public CharSequence getCommonValueAddsString(Context context) {
		List<String> commonValueAdds = getCommonValueAdds();
		if (commonValueAdds == null || commonValueAdds.size() == 0) {
			return null;
		}

		return Html.fromHtml(context.getString(R.string.common_value_add_template,
				FormatUtils.series(context, commonValueAdds, ",", Conjunction.AND)));
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONableList(obj, "rates", mRates);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert AvailabilityResponse object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mRates = JSONUtils.getJSONableList(obj, "rates", Rate.class);

		return true;
	}
}
