package com.expedia.bookings.model;

import java.io.File;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TravelerCommitResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.IoUtils;

public class WorkingTravelerManager {

	private static final String WORKING_TRAVELER_FILE_NAME = "working_traveler";
	private static final String WORKING_TRAVELER_TAG = "WORKING_TRAVELER_TAG";
	private static final String BASE_TRAVELER_TAG = "BASE_TRAVELER_TAG";

	private static final int COMMIT_TRAVELER_TO_ACCOUNT_RETRY_MAX = 2;

	public WorkingTravelerManager() {
	}

	private Traveler mWorkingTraveler; //The traveler we want to use
	private Traveler mBaseTraveler; //The traveler the working traveler was copied from... this is to give us an idea of what changed...
	private Semaphore mTravelerSaveSemaphore;
	private Semaphore mCommitTravelerSem;
	private boolean mAttemptLoadFromDisk = true;//By default yes, because after a crash we do want to attempt to load from disk

	public boolean hasTravelerOnDisk(Context context) {
		File file = context.getFileStreamPath(WORKING_TRAVELER_FILE_NAME);
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
	 * If there is a working traveler stored on disk, load it in
	 */
	public void loadWorkingTravelerFromDisk(final Context context) {
		Runnable loadTravalerFromDisk = new Runnable() {
			@Override
			public void run() {
				try {
					if (hasTravelerOnDisk(context)) {
						JSONObject obj = new JSONObject(IoUtils.readStringFromFile(WORKING_TRAVELER_FILE_NAME, context));
						if (obj.has(WORKING_TRAVELER_TAG)) {
							mWorkingTraveler = JSONUtils.getJSONable(obj, WORKING_TRAVELER_TAG, Traveler.class);
						}

						if (obj.has(BASE_TRAVELER_TAG)) {
							mBaseTraveler = JSONUtils.getJSONable(obj, BASE_TRAVELER_TAG, Traveler.class);
						}
					}
				}
				catch (Exception ex) {
					Log.e("Exception loading saved traveler file.", ex);
				}
				finally {
					mTravelerSaveSemaphore.release();
					mAttemptLoadFromDisk = false;//We tried for better or worse
				}
			}
		};

		if (tryAquireSaveTravelerSemaphore()) {
			Thread loadSavedTravelerThread = new Thread(loadTravalerFromDisk);
			loadSavedTravelerThread.start();
			//Block until our operation is done.
			mTravelerSaveSemaphore.acquireUninterruptibly();
			mTravelerSaveSemaphore.release();
		}
	}

	/**
	 * Set the current "working" traveler to be a copy of the traveler argument and set it's base traveler to be the same
	 * @param traveler
	 */
	public void setWorkingTravelerAndBase(Traveler traveler) {
		mWorkingTraveler = new Traveler();
		mBaseTraveler = new Traveler();
		if (traveler != null) {
			JSONObject json = traveler.toJson();
			mWorkingTraveler.fromJson(json);
			mBaseTraveler.fromJson(json);
		}
		mAttemptLoadFromDisk = false;
	}

	/**
	 * Set the working traveler to be the traveler argument but keep the current working traveler and set it as the base traveler
	 * @param traveler
	 */
	public void shiftWorkingTraveler(Traveler traveler) {
		if (mBaseTraveler == null) {
			mBaseTraveler = new Traveler();
		}
		mBaseTraveler.fromJson(getWorkingTraveler().toJson());
		mWorkingTraveler = new Traveler();
		mWorkingTraveler.fromJson(traveler.toJson());
		mAttemptLoadFromDisk = false;
	}

	/**
	 * Get a working traveler object. This will be a persistant traveler object that can be used to manipulate
	 * @return
	 */
	public Traveler getWorkingTraveler() {
		if (mWorkingTraveler == null) {
			mWorkingTraveler = new Traveler();
		}
		return mWorkingTraveler;
	}

	/**
	 * If the current traveler was created by calling rebaseWorkingTraveler we store a copy of the argument traveler.
	 * We can then use origin traveler to compare to our working traveler and figure out what has changed.
	 * @return
	 */
	public Traveler getBaseTraveler() {
		return mBaseTraveler;
	}

	/**
	 * Save the current working traveler to the Db object effectively commiting the changes locally.
	 * We also kick off a save call to the server (which runs in the background)
	 * @param travelerNumber (0 indexed)
	 */
	public Traveler commitWorkingTravelerToDB(int travelerNumber, Context context) {
		while (travelerNumber >= Db.getTravelers().size()) {
			Db.getTravelers().add(new Traveler());
		}
		Traveler commitTrav = new Traveler();
		commitTrav.fromJson(getWorkingTraveler().toJson());
		Db.getTravelers().set(travelerNumber, commitTrav);
		Db.setTravelersAreDirty(true);
		return commitTrav;
	}

	/**
	 * This commits the traveler to the user account.
	 * @param context
	 * @param trav - the traveler to commit
	 * @param wait - do we block until the call finishes?
	 * @return the traveler passed in (and it will be updated if wait is set to true)
	 */
	public Traveler commitTravelerToAccount(Context context, Traveler trav, boolean wait,
			ITravelerUpdateListener listener) {
		commitTravelerToAccount(context, trav, listener);
		if (wait) {
			try {
				mCommitTravelerSem.acquire();
				mCommitTravelerSem.release();
			}
			catch (Exception ex) {
				Log.e("Exception waiting for semaphore", ex);
			}
		}
		return trav;
	}

	public interface ITravelerUpdateListener {
		public void onTravelerUpdateFinished();

		public void onTravelerUpdateFailed();
	}

	public boolean isCommittingTravelerToAccount() {
		if (mCommitTravelerSem == null) {
			return false;
		}
		if (mCommitTravelerSem.tryAcquire()) {
			mCommitTravelerSem.release();
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Start a thread, and try to commit the traveler changes. We use a semaphore, so only one request will run at a time.
	 *
	 * @param context
	 * @param trav
	 */
	private void commitTravelerToAccount(final Context context, final Traveler trav,
			final ITravelerUpdateListener listener) {
		if (User.isLoggedIn(context)) {
			if (mCommitTravelerSem == null) {
				mCommitTravelerSem = new Semaphore(1);
			}

			Runnable commitTravelerRunner = new Runnable() {
				@Override
				public void run() {
					boolean semGot = false;
					boolean success = false;
					try {
						mCommitTravelerSem.acquire();
						int tryNum = 0;
						while (tryNum < COMMIT_TRAVELER_TO_ACCOUNT_RETRY_MAX && !success) {
							Log.i("Attempting to save traveler - attempt #" + tryNum);
							tryNum++;
							semGot = true;
							ExpediaServices services = new ExpediaServices(context);
							TravelerCommitResponse resp = services.commitTraveler(trav);
							success = resp.isSucceeded();
							Log.i("Commit traveler succeeded:" + success);
							if (success) {
								if (!TextUtils.isEmpty(resp.getTuid())) {
									if (!trav.hasTuid() && User.isLoggedIn(context) && Db.getUser() != null) {
										//If the traveler we sent didn't have a tuid, and the response does, then we set the tuid and add it to the users travelers
										//However currently the api doesn't currently return the tuid for new travelers 10/30/2012
										Traveler tTrav = new Traveler();
										tTrav.fromJson(trav.toJson());
										tTrav.setTuid(Long.getLong(resp.getTuid(),0L));
										Db.getUser().addAssociatedTraveler(tTrav);
									}
								}
								break;
							}
						}
					}
					catch (Exception ex) {
						Log.e("Exception commiting the traveler.", ex);
					}
					finally {
						if (semGot) {
							mCommitTravelerSem.release();
						}
						if (listener != null) {
							if (success) {
								listener.onTravelerUpdateFinished();
							}
							else {
								listener.onTravelerUpdateFailed();
							}
						}
					}
				}
			};

			Thread commitTravelerThread = new Thread(commitTravelerRunner);
			commitTravelerThread.setPriority(Thread.MIN_PRIORITY);
			commitTravelerThread.start();
		}
		else {
			if (listener != null) {
				listener.onTravelerUpdateFinished();
			}
		}
	}

	/**
	 * Call tryAquireState on the mTravelerSAveSempahore
	 * @return
	 */
	public boolean tryAquireSaveTravelerSemaphore() {
		if (mTravelerSaveSemaphore == null) {
			mTravelerSaveSemaphore = new Semaphore(1);
		}
		return mTravelerSaveSemaphore.tryAcquire();
	}

	/**
	 * Wait for the TravelerSaveSemaphore
	 */
	public void aquireSaveTravelerSemaphore() {
		if (mTravelerSaveSemaphore == null) {
			mTravelerSaveSemaphore = new Semaphore(1);
		}
		try {
			mTravelerSaveSemaphore.acquire();
		}
		catch (InterruptedException e) {
			Log.e("Thread interrupted while waiting to aquire the semaphore", e);
		}
	}

	/**
	 * This attempts to save the working traveler to a file in it's own thread.
	 * If another save operation is currently being performed this will be skipped.
	 *
	 * @param context
	 * @param force - If true we wait to aquire the semaphore. If false we only run if the semaphore is available.
	 * Basically if we are saving progress this should always be false, because we assume it will be called again after another change.
	 * However, if the traveler is in a state that we want to make sure the save gets written to the disk, this should be set to true.
	 */
	public void attemptWorkingTravelerSave(final Context context, boolean force) {

		Runnable saveTempTraveler = new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					if (mWorkingTraveler != null) {
						JSONUtils.putJSONable(obj, WORKING_TRAVELER_TAG, mWorkingTraveler);
					}
					if (mBaseTraveler != null) {
						JSONUtils.putJSONable(obj, BASE_TRAVELER_TAG, mBaseTraveler);
					}
					String json = obj.toString();
					IoUtils.writeStringToFile(WORKING_TRAVELER_FILE_NAME, json, context);
				}
				catch (Exception e) {
					Log.e("Error saving working traveler.", e);
				}
				finally {
					mTravelerSaveSemaphore.release();
				}
			}
		};

		if (force) {
			aquireSaveTravelerSemaphore();
			Thread saveThread = new Thread(saveTempTraveler);
			saveThread.start();
		}
		else if (tryAquireSaveTravelerSemaphore()) {
			Thread saveThread = new Thread(saveTempTraveler);
			saveThread.start();
		}
	}

	/**
	 * Clear out the working traveler
	 */
	public void clearWorkingTraveler(Context context) {
		mWorkingTraveler = null;
		mBaseTraveler = null;
		context.deleteFile(WORKING_TRAVELER_FILE_NAME);
		mAttemptLoadFromDisk = false;
	}

	/**
	 * Delete working traveler file (useful if we know the file is stale)
	 */
	public void deleteWorkingTravelerFile(final Context context) {
		Runnable deleteWorkingTavelerRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (hasTravelerOnDisk(context)) {
						context.deleteFile(WORKING_TRAVELER_FILE_NAME);
					}
				}
				catch (Exception ex) {
					Log.e("Exception deleting saved traveler file.", ex);
				}
				finally {
					mTravelerSaveSemaphore.release();
				}
			}
		};

		if (tryAquireSaveTravelerSemaphore()) {
			Thread deleteSavedTravelerThread = new Thread(deleteWorkingTavelerRunnable);
			deleteSavedTravelerThread.start();
			//Block until our operation is done.
			mTravelerSaveSemaphore.acquireUninterruptibly();
			mTravelerSaveSemaphore.release();
		}

	}
}
