package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.text.HtmlCompat;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.FormatUtils.Conjunction;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class HotelOffersResponse extends Response {
	private List<Rate> mRates;

	private Property mProperty;

	public HotelOffersResponse() {
		mRates = new ArrayList<Rate>();
	}

	/**
	 * Shortcut for determining if there are simply no rooms available at this hotel
	 * right now.  It's technically an error, but often times we want to handle
	 * this particular error differently.
	 * 
	 * Note that this returning "true" does NOT mean that there are rooms available;
	 * only that we didn't get the specific error that there are simply no hotel
	 * rooms available.
	 */
	public boolean isHotelUnavailable() {
		if (hasErrors()) {
			List<ServerError> errors = getErrors();
			if (errors.size() == 1) {
				ServerError error = errors.get(0);
				ErrorCode errorCode = error.getErrorCode();
				if (errorCode == ErrorCode.HOTEL_OFFER_UNAVAILABLE || errorCode == ErrorCode.HOTEL_ROOM_UNAVAILABLE) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean hasAtLeastOnFreeCancellationRate() {
		if (isHotelUnavailable() || getRates().size() == 0) {
			return false;
		}
		for (Rate rate : getRates()) {
			if (rate.hasFreeCancellation()) {
				return true;
			}
		}
		return false;
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

		return HtmlCompat.fromHtml(context.getString(R.string.common_value_add_template,
				FormatUtils.series(context, commonValueAdds, ",", Conjunction.AND).toLowerCase(Locale.getDefault())));
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
