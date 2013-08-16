package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

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

public class HotelOffersResponse extends Response {
	private List<Rate> mRates;

	private Property mProperty;

	private SummarizedRoomRates mSummarizedRoomRates;

	public HotelOffersResponse() {
		mRates = new ArrayList<Rate>();
	}

	public void addRate(Rate rate) {
		mRates.add(rate);
	}

	public void replaceRate(Rate oldRate, Rate newRate) {
		mRates.set(mRates.indexOf(oldRate), newRate);
	}

	public void removeRate(Rate rate) {
		if (rate != null) {
			mRates.remove(rate);
		}
	}

	public int getRateCount() {
		return mRates.size();
	}

	public Rate getRate(int index) {
		return mRates.get(index);
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
			JSONUtils.putJSONable(obj, "property", mProperty);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert HotelOffersResponse object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mRates = JSONUtils.getJSONableList(obj, "rates", Rate.class);
		mProperty = JSONUtils.getJSONable(obj, "property", Property.class);

		return true;
	}
}
