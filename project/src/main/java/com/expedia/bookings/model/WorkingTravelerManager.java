package com.expedia.bookings.model;

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

public class WorkingTravelerManager {

	private static final int COMMIT_TRAVELER_TO_ACCOUNT_RETRY_MAX = 2;

	private Traveler mWorkingTraveler; //The traveler we want to use
	private Traveler mBaseTraveler; //The traveler the working traveler was copied from... this is to give us an idea of what changed...
	private Semaphore mCommitTravelerSem;

	/**
	 * Set the current "working" traveler to be a copy of the traveler argument and set it's base traveler to be the same
	 *
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
	}

	/**
	 * Set the working traveler to be the traveler argument but keep the current working traveler and set it as the base traveler
	 *
	 * @param traveler
	 */
	public void shiftWorkingTraveler(Traveler traveler) {
		if (mBaseTraveler == null) {
			mBaseTraveler = new Traveler();
		}
		mBaseTraveler.fromJson(getWorkingTraveler().toJson());
		mWorkingTraveler = new Traveler();
		mWorkingTraveler.fromJson(traveler.toJson());
	}

	/**
	 * Get a working traveler object. This will be a persistant traveler object that can be used to manipulate
	 *
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
	 *
	 * @return
	 */
	public Traveler getBaseTraveler() {
		return mBaseTraveler;
	}

	/**
	 * Save the current working traveler to the Db object effectively commiting the changes locally.
	 * We also kick off a save call to the server (which runs in the background)
	 *
	 * @param travelerNumber (0 indexed)
	 */
	public Traveler commitWorkingTravelerToDB(int travelerNumber) {
		while (travelerNumber >= Db.getTravelers().size()) {
			Db.getTravelers().add(new Traveler());
		}
		Traveler commitTrav = new Traveler();
		commitTrav.fromJson(getWorkingTraveler().toJson());
		//If we are saving a newly entered traveler let's reset it's isNew status, so TravelerButtonFragment can bind accordingly.
		if (commitTrav.isNew()) {
			commitTrav.setIsNew(false);
		}
		Db.getTravelers().set(travelerNumber, commitTrav);
		Db.setTravelersAreDirty(true);
		return commitTrav;
	}

	/**
	 * This commits the traveler to the user account.
	 *
	 * @param context
	 * @param trav    - the traveler to commit
	 * @param wait    - do we block until the call finishes?
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

	public boolean workingTravelerNameDiffersFromBase() {
		if (getBaseTraveler() != null) {
			Traveler base = getBaseTraveler();
			Traveler working = getWorkingTraveler();

			if (TextUtils.isEmpty(base.getFirstName()) != TextUtils.isEmpty(working.getFirstName())) {
				return true;
			}
			else if (base.getFirstName() != null && working.getFirstName() != null
				&& base.getFirstName().trim().compareTo(working.getFirstName().trim()) != 0) {
				return true;
			}
			else if (TextUtils.isEmpty(base.getLastName()) != TextUtils.isEmpty(working.getLastName())) {
				return true;
			}
			else if (base.getLastName() != null && working.getLastName() != null
				&& base.getLastName().trim().compareTo(working.getLastName().trim()) != 0) {
				return true;
			}
		}
		return false;
	}

	public boolean workingTravelerDiffersFromBase() {
		if (getBaseTraveler() != null && getWorkingTraveler() != null) {
			return getWorkingTraveler().compareTo(getBaseTraveler()) != 0;
		}
		return false;
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
									if (User.isLoggedIn(context) && Db.getUser() != null) {
										//If the traveler we sent didn't have a tuid, and the response does, then we set the tuid and add it to the users travelers
										//However currently the api doesn't currently return the tuid for new travelers 10/30/2012
										Traveler tTrav = new Traveler();
										tTrav.fromJson(trav.toJson());
										tTrav.setTuid(Long.valueOf(resp.getTuid()));
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
	 * Clear out the working traveler
	 */
	public void clearWorkingTraveler() {
		mWorkingTraveler = null;
		mBaseTraveler = null;
	}

}
