package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.validation.ValidationError;

public class Response implements JSONable {
	private List<ServerError> mErrors;

	// Represents roughly when the response was created.  Should be used as a
	// general guideline, but not an exact figure.
	private long mTimestamp = Calendar.getInstance().getTimeInMillis();

	public boolean isSuccess() {
		return !hasErrors();
	}

	public void addError(ServerError error) {
		if (error == null) {
			return;
		}
		if (mErrors == null) {
			mErrors = new ArrayList<ServerError>();
		}
		mErrors.add(error);
	}

	public void addErrors(List<ServerError> errors) {
		if (errors == null) {
			return;
		}
		for (ServerError error : errors) {
			addError(error);
		}
	}

	public List<ServerError> getErrors() {
		return mErrors;
	}

	public boolean hasErrors() {
		if (mErrors == null) {
			return false;
		}
		return (mErrors.size() > 0);
	}

	public String gatherErrorMessage(Context context) {
		if (mErrors == null) {
			return null;
		}

		TreeSet<String> errorStrings = new TreeSet<String>();
		for (ServerError error : mErrors) {
			String msg = error.getPresentableMessage(context);
			if (!TextUtils.isEmpty(msg)) {
				errorStrings.add(msg);
			}
		}

		if (errorStrings.size() == 0) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		for (String msg : errorStrings) {
			builder.append(msg);
			builder.append("\n");
		}

		return builder.substring(0, builder.length() - 1);
	}

	/**
	 * Special handling for certain errors that contain a field name (for known fields).
	 * @param activity
	 * @return
	 */
	public List<ValidationError> checkForInvalidFields(Window parent, boolean isStoredCreditCard) {
		if (parent == null) {
			Log.d("Window parent is null");
			return null;
		}
		ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
		for (ServerError error : mErrors) {
			String field = error.getExtra("field");
			if (field != null) {
				if ("cvv".equals(field)) {
					View v = parent.findViewById(R.id.security_code_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
				}
				else if (!isStoredCreditCard && "creditCardNumber".equals(field)) {
					View v = parent.findViewById(R.id.card_number_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
				}
				else if (!isStoredCreditCard && "expirationDate".equals(field)) {
					View v = parent.findViewById(R.id.expiration_month_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
					v = parent.findViewById(R.id.expiration_year_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
				}
				else if (!isStoredCreditCard && "streetAddress".equals(field)) {
					View v = parent.findViewById(R.id.address1_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
				}
				else if (!isStoredCreditCard && "city".equals(field)) {
					View v = parent.findViewById(R.id.city_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
				}
				else if (!isStoredCreditCard && "state".equals(field)) {
					View v = parent.findViewById(R.id.state_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
				}
				else if (!isStoredCreditCard && "postalCode".equals(field)) {
					View v = parent.findViewById(R.id.postal_code_edit_text);
					errors.add(new ValidationError(v, ValidationError.ERROR_DATA_INVALID));
				}
			}
		}
		return errors;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "errors", mErrors);
			obj.putOpt("timestamp", mTimestamp);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Response to JSON", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean fromJson(JSONObject obj) {
		mErrors = (List<ServerError>) JSONUtils.getJSONableList(obj, "errors", ServerError.class);
		mTimestamp = obj.optLong("timestamp");
		return true;
	}
}
