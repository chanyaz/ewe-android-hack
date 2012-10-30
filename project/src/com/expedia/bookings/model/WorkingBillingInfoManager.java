package com.expedia.bookings.model;

import java.io.File;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.IoUtils;

public class WorkingBillingInfoManager {

	private static final String WORKING_BILLING_INFO_FILE_NAME = "working_billing_info";
	private static final String WORKING_BILLING_INFO_TAG = "WORKING_BILLING_INFO_TAG";
	private static final String BASE_BILLING_INFO_TAG = "BASE_BILLING_INFO_TAG";

	private BillingInfo mWorkingBillingInfo; //The traveler we want to use
	private BillingInfo mBaseBillingInfo; //The traveler the working traveler was copied from... this is to give us an idea of what changed...
	private Semaphore mBilingInfoSaveSemaphore;
	private boolean mAttemptLoadFromDisk = true;//By default yes, because after a crash we do want to attempt to load from disk

	public WorkingBillingInfoManager() {
	}

	public boolean hasBillingInfoOnDisk(Context context) {
		File file = context.getFileStreamPath(WORKING_BILLING_INFO_FILE_NAME);
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	public boolean getAttemptToLoadFromDisk() {
		return mAttemptLoadFromDisk;
	}

	public void setAttemptToLoadFromDisk(boolean attemptToLoad) {
		mAttemptLoadFromDisk = attemptToLoad;
	}

	/**
	 * If there is a working BillingInfo stored on disk, load it in
	 */
	public void loadWorkingBillingInfoFromDisk(final Context context) {
		Runnable loadTravalerFromDisk = new Runnable() {
			@Override
			public void run() {
				try {
					if (hasBillingInfoOnDisk(context)) {
						JSONObject obj = new JSONObject(IoUtils.readStringFromFile(WORKING_BILLING_INFO_FILE_NAME,
								context));
						if (obj.has(WORKING_BILLING_INFO_TAG)) {
							mWorkingBillingInfo = JSONUtils.getJSONable(obj, WORKING_BILLING_INFO_TAG,
									BillingInfo.class);

							Log.i("Loaded working billingInfo:" + mWorkingBillingInfo.toJson().toString());
						}

						if (obj.has(BASE_BILLING_INFO_TAG)) {
							mBaseBillingInfo = JSONUtils.getJSONable(obj, BASE_BILLING_INFO_TAG, BillingInfo.class);
						}
					}
				}
				catch (Exception ex) {
					Log.e("Exception loading saved BillingInfo file.", ex);
				}
				finally {
					mBilingInfoSaveSemaphore.release();
					mAttemptLoadFromDisk = false;//We tried for better or worse
				}
			}
		};

		if (tryAquireSaveBillingInfoSemaphore()) {
			Thread loadSavedBillingInfoThread = new Thread(loadTravalerFromDisk);
			loadSavedBillingInfoThread.start();
			//Block until our operation is done.
			mBilingInfoSaveSemaphore.acquireUninterruptibly();
			mBilingInfoSaveSemaphore.release();
		}
	}

	/**
	 * Set the current "working" BillingInfo to be a copy of the BillingInfo argument and set it's base BillingInfo to be the same
	 * @param BillingInfo
	 */
	public void setWorkingBillingInfoAndBase(BillingInfo billingInfo) {
		mWorkingBillingInfo = new BillingInfo(billingInfo);
		mBaseBillingInfo = new BillingInfo(billingInfo);
		mAttemptLoadFromDisk = false;
	}

	/**
	 * Set the working travelBillingInfo the BillingInfo argument but keep the current working BillingInfo and set it as the base BillingInfo
	 * @param BillingInfo
	 */
	public void shiftWorkingBillingInfo(BillingInfo billingInfo) {
		mBaseBillingInfo = mWorkingBillingInfo == null ? new BillingInfo() : new BillingInfo(mWorkingBillingInfo);
		mWorkingBillingInfo = new BillingInfo(billingInfo);
		mAttemptLoadFromDisk = false;
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
		Db.setBillingInfoIsDirty(true);
	}

	/**
	 * Call tryAquireState on the mBillingInfoSAveSempahore
	 * @return
	 */
	private boolean tryAquireSaveBillingInfoSemaphore() {
		if (mBilingInfoSaveSemaphore == null) {
			mBilingInfoSaveSemaphore = new Semaphore(1);
		}
		return mBilingInfoSaveSemaphore.tryAcquire();
	}

	/**
	 * Wait for the BillingInfoSaveSemaphore
	 */
	private void aquireSaveBillingInfoSemaphore() {
		if (mBilingInfoSaveSemaphore == null) {
			mBilingInfoSaveSemaphore = new Semaphore(1);
		}
		try {
			mBilingInfoSaveSemaphore.acquire();
		}
		catch (InterruptedException e) {
			Log.e("Thread interrupted while waiting to aquire the semaphore", e);
		}
	}

	/**
	 * This attempts to save the working BillingInfo to a file in it's own thread.
	 * If another save operation is currently being performed this will be skipped.
	 * 
	 * @param context
	 * @param force - If true we wait to aquire the semaphore. If false we only run if the semaphore is available. 
	 * Basically if we are saving progress this should always be false, because we assume it will be called again after another change. 
	 * However, if the BillingInfo is in a state that we want to make sure the save gets written to the disk, this should be set to true.
	 */
	public void attemptWorkingBillingInfoSave(final Context context, boolean force) {

		Runnable saveTempBillingInfo = new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					if (mWorkingBillingInfo != null) {
						JSONObject json = mWorkingBillingInfo.toJson();

						Log.i("workingBillingInfo before removing stuff:" + json.toString());

						// Remove sensitive data
						json.remove("number");
						json.remove("securityCode");

						Log.i("workingBillingInfo after removing stuff:" + json.toString());

						obj.put(WORKING_BILLING_INFO_TAG, json);
					}
					if (mBaseBillingInfo != null) {
						JSONObject json = mBaseBillingInfo.toJson();

						// Remove sensitive data
						json.remove("number");
						json.remove("securityCode");

						obj.put(BASE_BILLING_INFO_TAG, json);
					}

					String json = obj.toString();
					IoUtils.writeStringToFile(WORKING_BILLING_INFO_FILE_NAME, json, context);
				}
				catch (Exception e) {
					Log.e("Error saving working BillingInfo.", e);
				}
				finally {
					mBilingInfoSaveSemaphore.release();
				}
			}
		};

		if (force) {
			aquireSaveBillingInfoSemaphore();
			Thread saveThread = new Thread(saveTempBillingInfo);
			saveThread.start();
		}
		else if (tryAquireSaveBillingInfoSemaphore()) {
			Thread saveThread = new Thread(saveTempBillingInfo);
			saveThread.start();
		}
	}

	/**
	 * Clear out the working BillingInfo
	 */
	public void clearWorkingBillingInfo(Context context) {
		mWorkingBillingInfo = null;
		mBaseBillingInfo = null;
		context.deleteFile(WORKING_BILLING_INFO_FILE_NAME);
		mAttemptLoadFromDisk = false;
	}

	/**
	 * Delete working BillingInfo file (useful if we know the file is stale)
	 */
	public void deleteWorkingBillingInfoFile(final Context context) {
		Runnable deleteWorkingTavelerRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (hasBillingInfoOnDisk(context)) {
						context.deleteFile(WORKING_BILLING_INFO_FILE_NAME);
					}
				}
				catch (Exception ex) {
					Log.e("Exception deleting saved BillingInfo file.", ex);
				}
				finally {
					mBilingInfoSaveSemaphore.release();
				}
			}
		};

		if (tryAquireSaveBillingInfoSemaphore()) {
			Thread deleteSavedBillingInfoThread = new Thread(deleteWorkingTavelerRunnable);
			deleteSavedBillingInfoThread.start();
			//Block until our operation is done.
			mBilingInfoSaveSemaphore.acquireUninterruptibly();
			mBilingInfoSaveSemaphore.release();
		}

	}
}
