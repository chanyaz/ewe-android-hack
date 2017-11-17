package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class TravelerName {
	private String firstName;
	private String middleName;
	private String lastName;
	private String fullName;

	public TravelerName() {
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFullName() {
		if (TextUtils.isEmpty(fullName)) {
			String fullName = "";
			if (!TextUtils.isEmpty(firstName)) {
				fullName += firstName;
			}
			if (!TextUtils.isEmpty(middleName)) {
				fullName += " " + middleName;
			}
			if (!TextUtils.isEmpty(lastName)) {
				fullName += " " + lastName;
			}
			return fullName.trim();
		}
		return fullName;
	}

	public String getReversedFullName() {
		String fullName = "";
		if (!TextUtils.isEmpty(lastName)) {
			fullName += lastName;
		}
		if (!TextUtils.isEmpty(middleName)) {
			fullName += " " + middleName;
		}
		if (!TextUtils.isEmpty(firstName)) {
			fullName += " " +  firstName;
		}
		return fullName.trim();
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public boolean isEmpty() {
		return TextUtils.isEmpty(firstName) && TextUtils.isEmpty(middleName) && TextUtils.isEmpty(lastName);
	}

	public void toJson(JSONObject obj) throws JSONException {
		obj.putOpt("firstName", firstName);
		obj.putOpt("middleName", middleName);
		obj.putOpt("lastName", lastName);
		obj.putOpt("fullName", fullName);
	}

	public void fromJson(JSONObject obj) {
		firstName = obj.optString("firstName", null);
		middleName = obj.optString("middleName", null);
		lastName = obj.optString("lastName", null);
		fullName = obj.optString("fullName", null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TravelerName that = (TravelerName) o;
		if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) {
			return false;
		}
		if (middleName != null ? !middleName.equals(that.middleName) : that.middleName != null) {
			return false;
		}
		return lastName != null ? lastName.equals(that.lastName) : that.lastName == null;

	}

	@Override
	public int hashCode() {
		int result = firstName != null ? firstName.hashCode() : 0;
		result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
		result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
		return result;
	}
}
