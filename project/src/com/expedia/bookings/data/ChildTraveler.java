package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

/**
 * A class for encapsulating both a child's age and whether or not he
 * is going to use an airplane seat.
 */
public class ChildTraveler implements JSONable {

	private int mAge;
	private boolean mUsingSeat;

	public ChildTraveler() {
		new ChildTraveler(0, false);
	}

	public ChildTraveler(int age, boolean usingSeat) {
		mAge = age;
		mUsingSeat = usingSeat;
		if (age > 1) {
			mUsingSeat = true;
		}
	}

	public int getAge() {
		return mAge;
	}

	public void setAge(int age) {
		mAge = age;
	}

	public boolean usingSeat() {
		return mUsingSeat;
	}

	public void setSeatUse(boolean usingSeat) {
		mUsingSeat = usingSeat;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("age", mAge);
			obj.put("usingSeat", mUsingSeat);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		try {
			mUsingSeat = obj.getBoolean("usingSeat");
			mAge = obj.getInt("age");
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

}
