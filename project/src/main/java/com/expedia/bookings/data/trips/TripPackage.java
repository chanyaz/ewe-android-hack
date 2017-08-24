package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.GsonUtil;

public class TripPackage extends TripComponent {

	private final List<TripComponent> mTripComponents = new ArrayList<TripComponent>();

	private Money mTotal;

	public TripPackage() {
		super(Type.PACKAGE);
	}

	public void addTripComponents(List<TripComponent> tripComponents) {
		mTripComponents.addAll(tripComponents);
		associatePackageWithComponents();
	}

	public List<TripComponent> getTripComponents() {
		return mTripComponents;
	}

	private void associatePackageWithComponents() {
		for (TripComponent component : mTripComponents) {
			component.setParentPackage(this);
		}
	}

	public void setTotal(Money total) {
		mTotal = total;
	}

	public Money getTotal() {
		return mTotal;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			TripUtils.putTripComponents(obj, mTripComponents);
			GsonUtil.putForJsonable(obj, "total", mTotal);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mTripComponents.clear();
		mTripComponents.addAll(TripUtils.getTripComponents(obj));
		associatePackageWithComponents();

		mTotal = GsonUtil.getForJsonable(obj, "total", Money.class);

		return true;
	}
}
