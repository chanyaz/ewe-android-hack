package com.expedia.bookings.data;

import java.util.List;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.text.TextUtils;

import com.mobiata.android.Log;
import com.mobiata.android.util.TimingLogger;

/**
 * A singleton class for loading checkout data from disk ( in the background )
 *
 * We were loading checkout data from disk in both the hotels and flights flows, and will likely do so for
 * future products (cars, cruises, etc) it makes sense to move the functionality to a common place.
 *
 * Checkout data currently includes: BillingInfo and Travelers
 *
 */
public class CheckoutDataLoader {

	///////////////////////////////////////////////////////////////////////////////////////
	// Static methods
	///////////////////////////////////////////////////////////////////////////////////////

	private static CheckoutDataLoader mInstance;

	/**
	 * Get the instance ( or create one )
	 * @return
	 */
	public static CheckoutDataLoader getInstance() {
		if (mInstance == null) {
			mInstance = new CheckoutDataLoader();
		}
		return mInstance;
	}

	/**
	 * Remove the reference to the instance
	 */
	public static void destroyInstance() {
		mInstance = null;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Members
	///////////////////////////////////////////////////////////////////////////////////////
	private Semaphore mLoadCachedDataSem = new Semaphore(1);
	private boolean mLastLoadWasSuccessful = true;

	///////////////////////////////////////////////////////////////////////////////////////
	//Constructor
	///////////////////////////////////////////////////////////////////////////////////////

	private CheckoutDataLoader() {

	}

	///////////////////////////////////////////////////////////////////////////////////////
	//Public
	///////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Load checkout data from disk on a background thread.
	 * Note if this method is called but a thread is already running, we will just wait for that thread to finish and then fire the listener
	 * This is not meant to be called millions of times, but rather when a user first enters the checkout portion of the app
	 *
	 * @param context - context to use for our loading and disk operations
	 * @param loadBillingInfo - do we want to load billing info
	 * @param loadTravelers - do we want to load traveler data
	 * @param listener - fire this after we finish loading ( null ok ). Note: This will be fired from a background thread
	 * ui work should make sure to run itself in a runnable being invoked by runOnUiThread
	 * @param wait - do we want to wait for the thread to finish? - if this is true we block until complete
	 */
	public void loadCheckoutData(final Context context, final boolean loadBillingInfo, final boolean loadTravelers,
			final CheckoutDataLoadedListener listener, boolean wait) {

		Runnable loadDataRunner = new Runnable() {
			@Override
			public void run() {
				mLastLoadWasSuccessful = loadCachedData(context, loadBillingInfo, loadTravelers);
				if (listener != null) {
					listener.onCheckoutDataLoaded(mLastLoadWasSuccessful);
				}
			}
		};

		Thread loadDataThread = new Thread(loadDataRunner);
		loadDataThread.start();

		if (wait) {
			boolean semGot = false;
			try {
				mLoadCachedDataSem.acquire();
				semGot = true;
			}
			catch (Exception ex) {
				Log.e("Exception waiting for loadCheckoutData() semaphore", ex);
			}
			finally {
				if (semGot) {
					mLoadCachedDataSem.release();
				}
			}
		}

	}

	/**
	 * Are we currently loading from disk?
	 * @return
	 */
	public boolean isLoading() {
		boolean aquired = mLoadCachedDataSem.tryAcquire();
		if (aquired) {
			mLoadCachedDataSem.release();
		}
		return !aquired;
	}

	/**
	 * This blocks until the thread is finished loading in the background.
	 * @return this will be false if we were interrupted while waiting for the semaphore, otherwise true
	 */
	public boolean waitForCurrentThreadToFinish() {
		boolean retVal = true;
		boolean semGot = false;
		try {
			mLoadCachedDataSem.acquire();
			semGot = true;
		}
		catch (Exception ex) {
			Log.e("Exception waiting for semaphore", ex);
			retVal = false;
		}
		finally {
			if (semGot) {
				mLoadCachedDataSem.release();
			}
			else {
				retVal = false;
			}
		}
		return retVal;

	}

	///////////////////////////////////////////////////////////////////////////////////////
	//Private
	///////////////////////////////////////////////////////////////////////////////////////

	private boolean loadCachedData(Context context, boolean loadBillingInfo, boolean loadTravelers) {
		boolean semGot = false;
		try {
			TimingLogger timer = new TimingLogger("ExpediaBookings", "CheckoutDataLoader.loadCachedData");
			if (mLoadCachedDataSem.tryAcquire()) {
				semGot = true;
				timer.addSplit("Semeaphore aquired");

				if (loadBillingInfo) {
					BillingInfo billingInfo = Db.getBillingInfo();

					//Ensure stored card is valid
					if (TextUtils.isEmpty(billingInfo.getNumber())) {
						StoredCreditCard stored = billingInfo.getStoredCard();
						if (stored != null) {
							if (User.isLoggedIn(context)) {
								if (Db.getUser() == null) {
									Db.loadUser(context);
								}
								List<StoredCreditCard> usrCards = Db.getUser().getStoredCreditCards();
								boolean cardFound = false;
								for (int i = 0; i < usrCards.size(); i++) {
									if (stored.getId().compareTo(usrCards.get(i).getId()) == 0) {
										cardFound = true;
										break;
									}
								}
								//If the storedcard is not part of the user's collection of stored cards, we can't use it
								if (!cardFound) {
									Db.resetBillingInfo();
								}
							}
							else {
								//If we have an expedia account card, but we aren't logged in, we get rid of it
								Db.resetBillingInfo();
							}
						}
					}
					timer.addSplit("BillingInfo loaded.");
				}

				if (loadTravelers) {
					//Load traveler info (only if we don't have traveler info already)
					if (Db.getTravelers() == null || Db.getTravelers().size() == 0
							|| !Db.getTravelers().get(0).hasName()) {
						Db.loadTravelers(context);
						List<Traveler> travelers = Db.getTravelers();
						if (travelers != null && travelers.size() > 0) {
							if (User.isLoggedIn(context)) {
								//If we are logged in, we need to ensure that any expedia account users are associated with the currently logged in account
								if (Db.getUser() == null) {
									Db.loadUser(context);
								}
								List<Traveler> userTravelers = Db.getUser().getAssociatedTravelers();
								for (int i = 0; i < travelers.size(); i++) {
									Traveler trav = travelers.get(i);
									if (trav.hasTuid()) {
										boolean travFound = false;
										for (int j = 0; j < userTravelers.size(); j++) {
											Traveler usrTrav = userTravelers.get(j);
											if (usrTrav.getTuid().compareTo(trav.getTuid()) == 0) {
												travFound = true;
												break;
											}
										}
										if (!travFound) {
											travelers.set(i, new Traveler());
										}
									}
								}
							}
							else {
								//Remove logged in travelers (because the user is not logged in)
								for (int i = 0; i < travelers.size(); i++) {
									Traveler trav = travelers.get(i);
									if (trav.hasTuid()) {
										travelers.set(i, new Traveler());
									}
								}
							}
						}
					}
					timer.addSplit("Travelers loaded.");
				}
				timer.addSplit("Loading complete");
			}
			else {
				//We wait for the semaphore
				mLoadCachedDataSem.acquire();
				semGot = true;
				timer.addSplit("Finished waiting for other thread.");
			}
			timer.dumpToLog();
			return true;
		}
		catch (Exception ex) {
			Log.e("Exception loading data..", ex);
			return false;
		}
		finally {
			if (semGot) {
				mLoadCachedDataSem.release();
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	//Interface
	///////////////////////////////////////////////////////////////////////////////////////
	public interface CheckoutDataLoadedListener {
		public void onCheckoutDataLoaded(boolean wasSuccessful);
	}

}
