package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Money;
import com.mobiata.android.json.JSONUtils;

public class TripPackage extends TripComponent {

	private List<TripComponent> mTripComponents = new ArrayList<TripComponent>();

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
			JSONUtils.putJSONable(obj, "total", mTotal);
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

		mTotal = JSONUtils.getJSONable(obj, "total", Money.class);

		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Static methods

	/**
	 * This merges two packages resulting in a TripPackage that reflects the top level data
	 * of the newPackage argument, but combines tripcomponent list of the old with the new
	 * favoring new if there is conflict.
	 * 
	 * @param oldPackage - the older of the packages to merge (this packages values will be squashed except for trip components)
	 * @param newPackage - the newer of the pacakges to merge - this will be the basis for the returned package
	 * @return a new Package that has mostly values from the newPackage argument, but may also contain tripcomponents from the old package arg
	 */
	public static TripPackage mergePackages(TripPackage oldPackage, TripPackage newPackage) {

		//Init based on the new package, to have the most recent package level data
		TripPackage retPack = new TripPackage();
		retPack.fromJson(newPackage.toJson());

		//Add the trip components that are in the oldPackage and are missing from the newPackage
		ArrayList<TripComponent> copyComps = new ArrayList<TripComponent>();
		boolean packFound = false;
		for (TripComponent oldComp : oldPackage.getTripComponents()) {
			packFound = false;
			for (TripComponent newComp : retPack.getTripComponents()) {
				if (oldComp.compareTo(newComp) == 0) {
					packFound = true;
					break;
				}
			}
			if (!packFound) {
				copyComps.add(oldComp);
			}
		}
		retPack.addTripComponents(copyComps);

		return retPack;

	}
}
