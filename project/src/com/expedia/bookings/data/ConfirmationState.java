package com.expedia.bookings.data;

import java.io.File;

import org.json.JSONObject;

import android.content.Context;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;

/**
 * Saves a confirmation state, which allows us to reproduce a confirmation activity.
 */
public class ConfirmationState {

	private static final String CONFIRMATION_DATA_FILE = "confirmation.dat";

	private static final String CONFIRMATION_DATA_VERSION_FILE = "confirmation-version.dat";

	public enum Type {
		HOTEL,
		FLIGHT,
	}

	private Context mContext;
	private Type mType;

	public ConfirmationState(Context context, Type type) {
		mContext = context;
		mType = type;
	}

	public boolean save(SearchParams searchParams, Property property, Rate rate, BillingInfo billingInfo,
			BookingResponse bookingResponse, Rate discountRate) {
		Log.i("Saving confirmation data...");

		try {
			JSONObject data = new JSONObject();
			data.put(Codes.SEARCH_PARAMS, searchParams.toJson());
			data.put(Codes.PROPERTY, property.toJson());
			data.put(Codes.RATE, rate.toJson());
			data.put(Codes.BILLING_INFO, billingInfo.toJson());
			data.put(Codes.BOOKING_RESPONSE, bookingResponse.toJson());
			if (discountRate != null) {
				data.put(Codes.DISCOUNT_RATE, discountRate.toJson());
			}

			IoUtils.writeStringToFile(CONFIRMATION_DATA_VERSION_FILE,
					Integer.toString(AndroidUtils.getAppCode(mContext)), mContext);
			IoUtils.writeStringToFile(CONFIRMATION_DATA_FILE, data.toString(0), mContext);

			return true;
		}
		catch (Exception e) {
			Log.e("Could not save hotel confirmation data state.", e);
			return false;
		}
	}

	public boolean load() {
		Log.i("Loading saved confirmation data...");
		try {
			JSONObject data = new JSONObject(IoUtils.readStringFromFile(CONFIRMATION_DATA_FILE,
					mContext));
			Db.setSearchParams((SearchParams) JSONUtils.getJSONable(data, Codes.SEARCH_PARAMS, SearchParams.class));
			Db.setSelectedProperty((Property) JSONUtils.getJSONable(data, Codes.PROPERTY, Property.class));
			Db.setSelectedRate((Rate) JSONUtils.getJSONable(data, Codes.RATE, Rate.class));
			Db.setBillingInfo((BillingInfo) JSONUtils.getJSONable(data, Codes.BILLING_INFO, BillingInfo.class));
			Db.setBookingResponse((BookingResponse) JSONUtils.getJSONable(data, Codes.BOOKING_RESPONSE,
					BookingResponse.class));
			return true;
		}
		catch (Exception e) {
			Log.e("Could not load hotel confirmation data state.", e);
			return false;
		}
	}

	public boolean delete() {
		Log.i("Deleting saved confirmation data.");
		File savedConfResults = mContext.getFileStreamPath(CONFIRMATION_DATA_FILE);
		return savedConfResults.delete();
	}

	public boolean hasSavedData() {
		File savedConfResults = mContext.getFileStreamPath(CONFIRMATION_DATA_FILE);
		return savedConfResults.exists();
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience methods

	public static boolean hasSavedData(Context context, Type type) {
		ConfirmationState state = new ConfirmationState(context, type);
		return state.hasSavedData();
	}
}
