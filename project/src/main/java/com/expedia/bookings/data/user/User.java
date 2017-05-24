package com.expedia.bookings.data.user;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.expedia.account.AccountService;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.RestrictedProfileActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.StoredPointsCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.mobiata.android.FileCipher;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.TimingLogger;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User implements JSONable {

	// Version of this User
	private static final int VERSION = 2;

	static final String SAVED_INFO_FILENAME = "user.dat";

	// Kind of pointless when this is just stored as a static field, but at least protects
	// against someone getting the plaintext file but not the app itself.
	static final String PASSWORD = "M2MBDdEjbFTXTgNynBY2uvMPcUd8g3k9";

	private Traveler mPrimaryTraveler;
	private List<Traveler> mAssociatedTravelers = new ArrayList<>();

	private List<StoredCreditCard> mStoredCreditCards = new ArrayList<>();
	private List<StoredPointsCard> mStoredPointsCards = new ArrayList<>();

	private String mRewardsMembershipId;
	private UserLoyaltyMembershipInformation loyaltyMembershipInformation;

	private static final String[] ADDRESS_LINE_KEYS = new String[] { "firstAddressLine", "secondAddressLine" };

	public User() {
		// Default constructor
	}

	public User(Context context) {
		load(context);
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

	public void addStoredPointsCard(StoredPointsCard storedPointsCard) {
		mStoredPointsCards.add(storedPointsCard);
	}

	public List<StoredCreditCard> getStoredCreditCards() {
		if (mStoredCreditCards != null) {
			List<StoredCreditCard> creditCards = new ArrayList<>(mStoredCreditCards);
			List<StoredCreditCard> expiredCreditCards = new ArrayList<>();
			for (StoredCreditCard creditCard : creditCards) {
				if (creditCard.isExpired()) {
					expiredCreditCards.add(creditCard);
				}
			}
			creditCards.removeAll(expiredCreditCards);
			return creditCards;
		}
		return Collections.emptyList();
	}


	public List<StoredPointsCard> getStoredPointsCards() {
		return mStoredPointsCards;
	}

	public void addAssociatedTraveler(Traveler traveler) {
		mAssociatedTravelers.add(traveler);
	}

	public List<Traveler> getAssociatedTravelers() {
		return mAssociatedTravelers;
	}

	public String getTuidString() {
		if (this.getPrimaryTraveler() != null && this.getPrimaryTraveler().getTuid() != null
				&& this.getPrimaryTraveler().getTuid() >= 0) {
			return "" + this.getPrimaryTraveler().getTuid();
		}
		return null;
	}

	public String getExpediaUserId() {
		if (mPrimaryTraveler != null && mPrimaryTraveler.getExpediaUserId() != null
			&& mPrimaryTraveler.getExpediaUserId() >= 0) {
			return mPrimaryTraveler.getExpediaUserId().toString();
		}
		return null;
	}

	public String getRewardsMembershipId() {
		return mRewardsMembershipId;
	}

	public void setRewardsMembershipId(String rewardsMembershipId) {
		this.mRewardsMembershipId = rewardsMembershipId;
	}

	public void setLoyaltyMembershipInformation(UserLoyaltyMembershipInformation loyaltyMembershipInformation) {
		this.loyaltyMembershipInformation = loyaltyMembershipInformation;
	}

	@Nullable
	public UserLoyaltyMembershipInformation getLoyaltyMembershipInformation() {
		return loyaltyMembershipInformation;
	}
	//////////////////////////////////////////////////////////////////////////
	// Logging in/out

	/**
	 * Do we have a currently logged in user?
	 * @return true if a user is logged in, false otherwise.
	 */
	static boolean isLoggedIn(Context context) {
		boolean isLoggedIn = isLoggedInOnDisk(context) && isLoggedInToAccountManager(context);
		if (isLoggedIn) {
			if (Db.getUser() == null) {
				Db.loadUser(context);
			}
		}
		return isLoggedIn;
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
		File file = context.getFileStreamPath(SAVED_INFO_FILENAME);
		return file != null && file.exists();
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
		signOut(context, true);
	}

	public static void signOut(Context context, boolean clearCookies) {
		TimingLogger logger = new TimingLogger("ExpediaBookings", "User.signOut");

		//Do the actual sign out
		performSignOutCriticalActions(context, clearCookies);
		logger.addSplit("performSignOutCriticalActions");

		//perform the rest of the clean up.
		performSignOutCleanupActions(context);
		logger.addSplit("performSignOutCleanupActions");

		logger.dumpToLog();
	}
	
	/**
	 * Clear all User state that indicates the user is in some way logged in.
	 *
	 * @param context
	 * @param clearCookies
	 */
	private static void performSignOutCriticalActions(Context context, boolean clearCookies) {
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

		if (clearCookies) {
			//Remove the login cookies
			ExpediaServices.removeUserLoginCookies(context);
			logger.addSplit("ExpediaServices.removeUserLoginCookies(context)");
		}

		//Facebook log out
		AccountService.facebookLogOut();
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

		//Itinerary Manager
		ItineraryManager.getInstance().clear();
		logger.addSplit("ItineraryManager.getInstance().clear();");

		//Delete all Notifications
		Notification.deleteAll(context);
		logger.addSplit("Notification.deleteAll(context);");

		//If the data has already been populated in memory, we should clear that....
		if (Db.getWorkingBillingInfoManager() != null) {
			Db.getWorkingBillingInfoManager().clearWorkingBillingInfo();
		}

		if (Db.getWorkingTravelerManager() != null) {
			Db.getWorkingTravelerManager().clearWorkingTraveler();
		}

		// Trip Bucket
		if (Db.getTripBucket() != null && Db.getTripBucket().isUserAirAttachQualified()) {
			Db.getTripBucket().clearAirAttach();
			Db.saveTripBucket(context);
		}

		Db.resetBillingInfo();
		Db.getTravelers().clear();
		logger.addSplit("User billing and traveler info deletion.");
		logger.dumpToLog();
	}

	/**
	 * This method signs us into an expedia account. It uses the AccountManager and ensures
	 * that all of our User data is set up correctly. This will open the login GUI (if need be).
	 *
	 * @param activityContext - Must be an activity
	 * @param options - Typically this should be a Bundle instance generated by LoginActivity.createArgumentsBundle(...);
	 */
	public static void signIn(Activity activityContext, Bundle options) {
		if (AndroidUtils.isRestrictedProfile(activityContext)) {
			Intent restrictedProfileIntent = RestrictedProfileActivity.createIntent(activityContext);
			activityContext.startActivity(restrictedProfileIntent);
		}
		else {
			AccountManager manager = AccountManager.get(activityContext);
			String accountType = activityContext.getString(R.string.expedia_account_type_identifier);
			String tokenType = activityContext.getString(R.string.expedia_account_token_type_tuid_identifier);
			Account[] accounts = manager.getAccountsByType(accountType);
			if (accounts == null || accounts.length == 0) {
				manager.addAccount(accountType, tokenType, null, options, activityContext, null, null);
			}
			else if (accounts.length >= 1) {
				Account activeAccount = accounts[0];
				if (activeAccount != null) {
					manager.getAuthToken(activeAccount, accountType, options, activityContext, null, null);
				}
				else {
					manager.addAccount(accountType, tokenType, null, options, activityContext, null, null);
				}
			}
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
		String accountType = context.getString(R.string.expedia_account_type_identifier);
		String contentAuthority = context.getString(R.string.authority_account_sync);
		AccountManager manager = AccountManager.get(context);
		Account[] accounts = manager.getAccountsByType(accountType);
		if (accounts.length > 0) {
			Account account = accounts[0];
			ContentResolver.setIsSyncable(account, contentAuthority, 0);
			manager.removeAccount(account, null, null);
		}
		if (usr != null) {
			manager.invalidateAuthToken(accountType, usr.getTuidString());
		}
	}

	/**
	 * This method is important. This is the method that adds the account to AccountManager.
	 * If we log in and this doesn't get called, User.isLoggedIn() will
	 * still return false, and user data will not be allowed to sync.
	 */
	public static void addUserToAccountManager(Context context, User usr) {
		if (context != null && usr != null && Strings.isNotEmpty(usr.getPrimaryTraveler().getEmail())) {
			//Add the account to the account manager
			String accountType = context.getString(R.string.expedia_account_type_identifier);
			String tokenType = context.getString(R.string.expedia_account_token_type_tuid_identifier);
			AccountManager manager = AccountManager.get(context);

			boolean accountAlreadyExists = false;

			//We are adding a new user to account manager, so we clobber ALL old accountmanager expedia accounts.
			Account[] accounts = manager.getAccountsByType(accountType);
			if (accounts != null && accounts.length > 0) {
				for (Account account : accounts) {
					if (isItThisUsersAccount(usr, account)) {
						accountAlreadyExists = true;
					}
					else {
						manager.removeAccount(account, null, null);
					}
				}
			}

			//Add the new account
			if (!accountAlreadyExists) {
				final Account account = new Account(usr.getPrimaryTraveler().getEmail(), accountType);
				manager.addAccountExplicitly(account, usr.getTuidString(), null);
				manager.setAuthToken(account, tokenType, usr.getTuidString());
				//Set data syncing enabled/disabled
				String contentAuthority = context.getString(R.string.authority_account_sync);
				ContentResolver.setSyncAutomatically(account, contentAuthority, false);
			}


		}
	}

	private static boolean isItThisUsersAccount(User user, Account account) {
		return account.name.equals(user.getPrimaryTraveler().getEmail());
	}

	//////////////////////////////////////////////////////////////////////////
	// Save/load

	public boolean save(Context context) {
		Log.d("Saving user.");

		JSONObject data = toJson();

		boolean isFileSaved;

		File pathToSave =  context.getFileStreamPath(SAVED_INFO_FILENAME);
		if (!ExpediaBookingApp.isRobolectric()) {
			// Initialize a cipher
			FileCipher fileCipher = new FileCipher(PASSWORD);

			if (!fileCipher.isInitialized()) {
				return false;
			}
			isFileSaved = fileCipher.saveSecureData(pathToSave, data.toString());
		}
		else {
			try {
				IoUtils.writeStringToFile(SAVED_INFO_FILENAME, data.toString(), context);
				isFileSaved = true;
			}
			catch (Exception e) {
				throw new IllegalStateException("Unable to save temp user.dat file");
			}
		}
		return isFileSaved;
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
			Log.e("Could not restore saved user info.", e);
			return false;
		}
	}

	/**
	 * Deletes the saved User file.  Make sure to clear out other references
	 * to the User after doing this call.
	 * @param context the context
	 * @return true if successfully deleted
	 */
	private static boolean delete(Context context) {
		File file = context.getFileStreamPath(SAVED_INFO_FILENAME);
		return file.exists() && file.delete();
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
			JSONUtils.putJSONableList(obj, "storedPointsCards", mStoredPointsCards);
			JSONUtils.putJSONableList(obj, "associatedTravelers", mAssociatedTravelers);
			JSONUtils.putJSONable(obj, "loyaltyMembershipInformation", loyaltyMembershipInformation);
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
			mPrimaryTraveler.setExpediaUserId(obj.optLong("expUserId"));

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

				List<String> addrLines = new ArrayList<>();
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
		mStoredPointsCards = JSONUtils.getJSONableList(obj, "storedPointsCards", StoredPointsCard.class);

		mAssociatedTravelers = JSONUtils.getJSONableList(obj, "associatedTravelers", Traveler.class);
		mRewardsMembershipId = obj.optString("loyaltyAccountNumber");

		loyaltyMembershipInformation = JSONUtils.getJSONable(obj, "loyaltyMembershipInformation", UserLoyaltyMembershipInformation.class);
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

	/**
	 * Returns the logged in user's LoyaltyMembershipTier. If the user is not logged in, this
	 * will return LoyaltyMembershipTier.NONE
	 * @param context
	 * @return
	 */
	public static LoyaltyMembershipTier getLoggedInLoyaltyMembershipTier(Context context) {
		if (User.isLoggedIn(context)) {

			if (Db.getUser() == null) {
				Db.loadUser(context);
			}
			if (Db.getUser().getPrimaryTraveler() != null) {
				return Db.getUser().getPrimaryTraveler().getLoyaltyMembershipTier();
			}
		}

		return LoyaltyMembershipTier.NONE;
	}

	public StoredPointsCard getStoredPointsCard(PaymentType paymentType) {
		paymentType.assertIsPoints();
		if (CollectionUtils.isNotEmpty(mStoredPointsCards)) {
			for (StoredPointsCard storedPointsCard : mStoredPointsCards) {
				if (storedPointsCard.getPaymentType().equals(paymentType)) {
					return storedPointsCard;
				}
			}
		}
		return null;
	}

	public static void loadUser(Context context, UserAccountRefresher.IUserAccountRefreshListener listener) {
		UserAccountRefresher userAccountRefresher = new UserAccountRefresher(context, LineOfBusiness.NONE, listener);
		userAccountRefresher.forceAccountRefresh();
	}

}
