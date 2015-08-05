package com.expedia.bookings.model;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;

public class WorkingBillingInfoManager {

	private BillingInfo mWorkingBillingInfo; //The traveler we want to use
	private BillingInfo mBaseBillingInfo; //The traveler the working traveler was copied from... this is to give us an idea of what changed...

	/**
	 * Set the current "working" BillingInfo to be a copy of the BillingInfo argument and set it's base BillingInfo to be the same
	 * @param BillingInfo
	 */
	public void setWorkingBillingInfoAndBase(BillingInfo billingInfo) {
		mWorkingBillingInfo = new BillingInfo(billingInfo);
		mBaseBillingInfo = new BillingInfo(billingInfo);
	}

	/**
	 * Set the working travelBillingInfo the BillingInfo argument but keep the current working BillingInfo and set it as the base BillingInfo
	 * @param BillingInfo
	 */
	public void shiftWorkingBillingInfo(BillingInfo billingInfo) {
		mBaseBillingInfo = mWorkingBillingInfo == null ? new BillingInfo() : new BillingInfo(mWorkingBillingInfo);
		mWorkingBillingInfo = new BillingInfo(billingInfo);
	}

	/**
	 * Get a working BillingInfo object. This will be a persistant BillingInfo object that can be used to manipulate
	 * @return
	 */
	public BillingInfo getWorkingBillingInfo() {
		if (mWorkingBillingInfo == null) {
			mWorkingBillingInfo = new BillingInfo();
		}
		if (mWorkingBillingInfo.getLocation() == null) {
			mWorkingBillingInfo.setLocation(new Location());
		}
		return mWorkingBillingInfo;
	}

	/**
	 * If the current BillingInfo was created by calling rebaseWorkingBillingInfo we store a copy of the argument BillingInfo.
	 * We can then use origin BillingInfo to compare to our working BillingInfo and figure out what has changed.
	 * @return
	 */
	public BillingInfo getBaseBillingInfo() {
		return mBaseBillingInfo;
	}

	/**
	 * Save the current working BillingInfo to the Db object effectively commiting the changes locally.
	 * @param BillingInfoNumber (0 indexed)
	 */
	public void commitWorkingBillingInfoToDB() {
		BillingInfo commitBillingInfo = new BillingInfo(mWorkingBillingInfo);
		Db.setBillingInfo(commitBillingInfo);
	}

	/**
	 * Clear out the working BillingInfo
	 */
	public void clearWorkingBillingInfo() {
		mWorkingBillingInfo = null;
		mBaseBillingInfo = null;
	}
}
