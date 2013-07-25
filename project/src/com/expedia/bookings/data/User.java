package com.expedia.bookings.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.server.ExpediaServices;
import com.facebook.Session;
import com.mobiata.android.FileCipher;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.TimingLogger;

public class User implements JSONable {

	// Version of this User
	private static final int VERSION = 2;

	static final String SAVED_INFO_FILENAME = "user.dat";

	// For backwards compatibility - not being used in the future
	static final String IS_USER_LOGGED_IN_FILE = "is_user_logged_in.boolean";

	// Kind of pointless when this is just stored as a static field, but at least protects
	// against someone getting the plaintext file but not the app itself.
	static final String PASSWORD = "M2MBDdEjbFTXTgNynBY2uvMPcUd8g3k9";

	//The lock used to synchronize the cleanup operation
	private static Object sSignOutCleanupLock = new Object();

	private Traveler mPrimaryTraveler;
	private List<Traveler> mAssociatedTravelers = new ArrayList<Traveler>();

	private List<StoredCreditCard> mStoredCreditCards = new ArrayList<StoredCreditCard>();

	private static final String[] ADDRESS_LINE_KEYS = new String[] { "firstAddressLine", "secondAddressLine" };

	public User() {
		// Default constructor
	}

	public User(Context context) {
		load(context);
	}

	public User(JSONObject obj) {
		this.fromJson(obj);
	}

	public void setPrimaryTraveler(Traveler traveler) {
		mPrimaryTraveler = traveler;
	}

	public Traveler getPrimaryTraveler() {
		return mPrimaryTraveler;
	}

	public void addStoredCreditCard(StoredCreditCard cc) {
		mStoredCreditCards.add(cc);
	}

	public boolean hasStoredCreditCards() {
		return mStoredCreditCards.size() > 0;
	}

	public List<StoredCreditCard> getStoredCreditCards() {
		return mStoredCreditCards;
	}

	public void addAssociatedTraveler(Traveler traveler) {
		mAssociatedTravelers.add(traveler);
	}

	public List<Traveler> getAssociatedTravelers() {
		return mAssociatedTravelers;
	}

	public boolean isRewardsUser() {
		if (mPrimaryTraveler != null) {
			return !TextUtils.isEmpty(mPrimaryTraveler.getLoyaltyMembershipNumber());
		}
		return false;
	}

	public String getTuidString() {
		if (this.getPrimaryTraveler() != null && this.getPrimaryTraveler().getTuid() != null
				&& this.getPrimaryTraveler().getTuid() >= 0) {
			return "" + this.getPrimaryTraveler().getTuid();
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Logging in/out

	/**
	 * Do we have a currently logged in user?
	 * @param context
	 * @return true if a user is logged in, false otherwise.
	 */
	public static boolean isLoggedIn(Context context) {
		if (isLoggedInOnDisk(context)) {
			return isLoggedInToAccountManager(context);
		}
		return false;
	}

	/**
	 * Does the app think we are logged in (regardless of AccountManager state)?
	 * 
	 * NOTE: This is not for general use, please use User.isLoggedIn();
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isLoggedInOnDisk(Context context) {
		boolean isLoggedIn = false;

		// Existence of the saved info indicates being logged in
		isLoggedIn |= context.getFileStreamPath(SAVED_INFO_FILENAME).exists();

		// Backwards compatible method for checking logged in state
		isLoggedIn |= context.getFileStreamPath(IS_USER_LOGGED_IN_FILE).exists();

		return isLoggedIn;
	}

	/**
	 * Does the account manager think we are logged in (regardless of App state)?
	 * 
	 * NOTE: This is not for general use, please use User.isLoggedIn();
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isLoggedInToAccountManager(Context context) {
		boolean isLoggedIn = false;
		String accountType = context.getString(R.string.expedia_account_type_identifier);
		String tokenType = context.getString(R.string.expedia_account_token_type_tuid_identifier);
		AccountManager manager = AccountManager.get(context);
		Account[] accounts = manager.getAccountsByType(accountType);
		if (accounts != null && accounts.length >= 1) {
			String token = manager.peekAuthToken(accounts[0], tokenType);
			isLoggedIn = !TextUtils.isEmpty(token);
		}
		return isLoggedIn;
	}

	/**
	 * Log out the current user and clean up user related state
	 * 
	 * @param context
	 */
	public static void signOut(Context context) {
		TimingLogger logger = new TimingLogger("ExpediaBookings", "User.signOut");

		//Do the actual sign out
		performSignOutCriticalActions(context);
		logger.addSplit("performSignOutCriticalActions");

		//perform the rest of the clean up.
		performSignOutCleanupActions(context);
		logger.addSplit("performSignOutCleanupActions");

		logger.dumpToLog();
	}

	/**
	 * Log out the current user and clean up user related state.
	 * 
	 * After this method returns User.isLoggedIn will return false,
	 * however cleanup happens in a background thread and may still
	 * be working.
	 * 
	 * @param context
	 */
	public static void signOutAsync(final Context context) {
		TimingLogger logger = new TimingLogger("ExpediaBookings", "User.signOut");

		//Do the actual sign out
		performSignOutCriticalActions(context);
		logger.addSplit("performSignOutCriticalActions");

		//perform the rest of the clean up (in a background thread)
		Runnable cleanupRunnable = new Runnable() {
			@Override
			public void run() {
				performSignOutCleanupActions(context);
			}
		};
		Thread cleanupThread = new Thread(cleanupRunnable);
		cleanupThread.start();
		logger.addSplit("performSignOutCleanupActions thread initialized and started");

		logger.dumpToLog();
	}

	/**
	 * Clear all User state that indicates the user is in some way logged in.
	 * 
	 * @param context
	 */
	private static void performSignOutCriticalActions(Context context) {
		TimingLogger logger = new TimingLogger("ExpediaBookings", "User.performCriticalSignOutActions");

		// Delete User (after this point User.isSignedIn will return false)
		delete(context);
		logger.addSplit("delete()");

		//AccountManager
		User.removeUserFromAccountManager(context, Db.getUser());
		logger.addSplit("removeUserFromAccountManager()");

		// Clear User from Db
		Db.setUser(null);
		logger.addSplit("Db.setUser(null)");

		//Remove the login cookies
		ExpediaServices.removeUserLoginCookies(context);
		logger.addSplit("ExpediaServices.removeUserLoginCookies(context)");

		//Facebook log out
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().closeAndClearTokenInformation();
		}
		logger.addSplit("Facebook Session Closed");

		logger.dumpToLog();
	}

	/**
	 * Clear all (global) data that depends on the User being logged in.
	 * 
	 * @param context
	 */
	private static void performSignOutCleanupActions(Context context) {
		TimingLogger logger = new TimingLogger("ExpediaBookings", "User.performSignOutCleanupActions");
		// NOTE: Synchronization could be improved here. We are relying on the sign in flow taking longer than it takes to call this method.
		synchronized (sSignOutCleanupLock) {
			logger.addSplit("sSignOutCleanupLock aquired");

			//Itinerary Manager
			ItineraryManager.getInstance().clear();
			logger.addSplit("ItineraryManager.getInstance().clear();");

			//Delete all Notifications
			Notification.deleteAll(context);
			logger.addSplit("Notification.deleteAll(context);");
		}
		logger.dumpToLog();
	}

	/**
	 * This method signs us into an expedia account. It uses the AccountManager and ensures
	 * that all of our User data is set up correctly. This will open the login GUI (if need be).
	 * 
	 * @param context - Must be an activity
	 * @param options - Typically this should be a Bundle instance generated by LoginActivity.createArgumentsBundle(...);
	 */
	public static void signIn(android.app.Activity context, Bundle options) {
		AccountManager manager = AccountManager.get(context);
		String accountType = context.getString(R.string.expedia_account_type_identifier);
		String tokenType = context.getString(R.string.expedia_account_token_type_tuid_identifier);
		Account[] accounts = manager.getAccountsByType(accountType);
		Account activeAccount = null;
		if (accounts == null || accounts.length == 0) {
			manager.addAccount(accountType, tokenType, null, options, context, null, null);
		}
		else if (accounts != null && accounts.length == 1) {
			activeAccount = accounts[0];
		}
		else {
			throw new RuntimeException("There should never be more than one expedia user in AccountManager!");
		}
		if (activeAccount != null) {
			manager.getAuthToken(activeAccount, accountType, options, context, null, null);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Add remove user to account manager.

	/**
	 * This will remove the Expedia account from AccountManager, and will invalidate the token
	 * for the supplied user.
	 * 
	 * @param context
	 */
	private static void removeUserFromAccountManager(Context context, User usr) {
		if (context != null) {
			String accountType = context.getString(R.string.expedia_account_type_identifier);
			String contentAuthority = context.getString(R.string.expedia_account_sync_adapter_content_authority);
			AccountManager manager = AccountManager.get(context);
			Account[] accounts = manager.getAccountsByType(accountType);
			if (accounts.length > 0) {
				Account account = accounts[0];
				ContentResolver.removePeriodicSync(account, contentAuthority, new Bundle());
				manager.removeAccount(account, null, null);
			}
			if (usr != null) {
				manager.invalidateAuthToken(accountType, usr.getTuidString());
			}
		}
	}

	/**
	 * This method is important. This is the method that adds the account to AccountManager
	 * and sets up syncing. If we log in and this doesn't get called, User.isLoggedIn() will
	 * still return false, and no data will sync.
	 */
	public static void addUserToAccountManager(Context context, User usr) {
		if (context != null && usr != null) {
			//Add the account to the account manager
			String accountType = context.getString(R.string.expedia_account_type_identifier);
			String tokenType = context.getString(R.string.expedia_account_token_type_tuid_identifier);
			AccountManager manager = AccountManager.get(context);
			final Account account = new Account(usr.getPrimaryTraveler().getEmail(), accountType);
			manager.addAccountExplicitly(account, usr.getTuidString(), null);
			manager.setAuthToken(account, tokenType, usr.getTuidString());

			//Start syncing data!
			String contentAuthority = context.getString(R.string.expedia_account_sync_adapter_content_authority);
			int syncInterval = context.getResources().getInteger(R.integer.account_sync_interval);
			ContentResolver.setIsSyncable(account, contentAuthority, 1);
			ContentResolver.setSyncAutomatically(account, contentAuthority, true);
			ContentResolver.addPeriodicSync(account, contentAuthority, new Bundle(), syncInterval);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Save/load

	public boolean save(Context context) {
		Log.d("Saving user.");

		// Initialize a cipher
		FileCipher fileCipher = new FileCipher(PASSWORD);

		if (!fileCipher.isInitialized()) {
			return false;
		}

		JSONObject data = toJson();

		return fileCipher.saveSecureData(context.getFileStreamPath(SAVED_INFO_FILENAME), data.toString());
	}

	public boolean load(Context context) {
		Log.d("Loading saved user.");

		// Check that the saved billing info file exists
		File f = context.getFileStreamPath(SAVED_INFO_FILENAME);
		if (!f.exists()) {
			return false;
		}

		// Initialize a cipher
		FileCipher fileCipher = new FileCipher(PASSWORD);
		if (!fileCipher.isInitialized()) {
			return false;
		}

		String results = fileCipher.loadSecureData(f);
		if (results == null || results.length() == 0) {
			return false;
		}

		try {
			fromJson(new JSONObject(results));
			return true;
		}
		catch (JSONException e) {
			Log.e("Could not restore saved billing info.", e);
			return false;
		}
	}

	/**
	 * Deletes the saved User file.  Make sure to clear out other references
	 * to the User after doing this call.
	 * @param context the context
	 * @return true if successfully deleted
	 */
	public static boolean delete(Context context) {
		boolean success = true;

		// Backwards compatible delete
		File fOld = context.getFileStreamPath(IS_USER_LOGGED_IN_FILE);
		if (fOld.exists()) {
			success &= fOld.delete();
		}

		// Check that the saved user file exists before trying to delete
		File f = context.getFileStreamPath(SAVED_INFO_FILENAME);
		if (f.exists()) {
			success &= f.delete();
		}

		return success;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.put("version", VERSION);
			JSONUtils.putJSONable(obj, "primaryTraveler", mPrimaryTraveler);
			JSONUtils.putJSONableList(obj, "storedCreditCards", mStoredCreditCards);
			JSONUtils.putJSONableList(obj, "associatedTravelers", mAssociatedTravelers);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert User to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		int version = obj.optInt("version");

		// Backwards-compatible version
		if (version < 2) {
			mPrimaryTraveler = new Traveler();

			mPrimaryTraveler.setTuid(obj.optLong("tuid"));
			mPrimaryTraveler.setLoyaltyMembershipNumber(obj.optString("loyaltyMembershipNumber", null));

			mPrimaryTraveler.setFirstName(obj.optString("firstName", null));
			mPrimaryTraveler.setMiddleName(obj.optString("middleName", null));
			mPrimaryTraveler.setLastName(obj.optString("lastName", null));
			mPrimaryTraveler.setEmail(obj.optString("email", null));

			List<Phone> phoneNumbers = JSONUtils.getJSONableList(obj, "phoneNumbers", Phone.class);
			if (phoneNumbers != null) {
				for (Phone phoneNumber : phoneNumbers) {
					mPrimaryTraveler.addPhoneNumber(phoneNumber);
				}
			}

			JSONObject addr = obj.optJSONObject("homeAddress");
			if (addr != null) {
				Location loc = new Location();

				loc.setCity(addr.optString("city", null));
				loc.setStateCode(addr.optString("province", null));
				loc.setPostalCode(addr.optString("postalCode", null));
				loc.setCountryCode(addr.optString("countryAlpha3Code", null));

				List<String> addrLines = new ArrayList<String>();
				for (String key : ADDRESS_LINE_KEYS) {
					String line = addr.optString(key, null);
					if (line != null) {
						addrLines.add(line);
					}
				}
				loc.setStreetAddress(addrLines);

				mPrimaryTraveler.setHomeAddress(loc);
			}

			mPrimaryTraveler.setSmokingPreferred(obj.optBoolean("isSmokingPreferred", false));
		}
		else {
			mPrimaryTraveler = JSONUtils.getJSONable(obj, "primaryTraveler", Traveler.class);
		}

		mStoredCreditCards = JSONUtils.getJSONableList(obj, "storedCreditCards", StoredCreditCard.class);

		mAssociatedTravelers = JSONUtils.getJSONableList(obj, "associatedTravelers", Traveler.class);

		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}
}
