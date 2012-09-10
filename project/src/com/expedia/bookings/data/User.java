package com.expedia.bookings.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.FileCipher;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class User implements JSONable {

	static final String SAVED_INFO_FILENAME = "user.dat";

	// For backwards compatibility - not being used in the future
	static final String IS_USER_LOGGED_IN_FILE = "is_user_logged_in.boolean";

	// Kind of pointless when this is just stored as a static field, but at least protects
	// against someone getting the plaintext file but not the app itself.
	static final String PASSWORD = "M2MBDdEjbFTXTgNynBY2uvMPcUd8g3k9";

	private Long mTuid;
	private String mEmail;

	private String mFirstName;
	private String mMiddleName;
	private String mLastName;

	private ArrayList<Phone> mPhoneNumbers;
	private Location mHomeAddress;
	private ArrayList<StoredCreditCard> mStoredCreditCards;
	private ArrayList<Traveler> mAssociatedTravelers;

	private String mLoyaltyMembershipNumber;
	private boolean mIsSmokingPreferred;

	private static final String[] ADDRESS_LINE_KEYS = new String[] { "firstAddressLine", "secondAddressLine" };

	public User(Context context) {
		load(context);
	}

	public User(JSONObject obj) {
		this.fromJson(obj);
	}

	public Long getTuid() {
		return mTuid;
	}

	public String getEmail() {
		return mEmail;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public String getMiddleName() {
		return mMiddleName;
	}

	public String getLastName() {
		return mLastName;
	}

	public String getLoyaltyMembershipNumber() {
		if (mLoyaltyMembershipNumber == null || mLoyaltyMembershipNumber.length() == 0) {
			return null;
		}
		else {
			return mLoyaltyMembershipNumber;
		}
	}

	public boolean hasStoredCreditCards() {
		return mStoredCreditCards.size() > 0;
	}

	public List<StoredCreditCard> getStoredCreditCards() {
		return mStoredCreditCards;
	}
	
	public List<Traveler> getAssociatedTravelers(){
		return mAssociatedTravelers;
	}

	//////////////////////////////////////////////////////////////////////////
	// Dump out a BillingInfo object for use on the booking screen

	public BillingInfo toBillingInfo() {
		BillingInfo b = new BillingInfo();
		b.setFirstName(mFirstName);
		b.setLastName(mLastName);
		for (Phone p : mPhoneNumbers) {
			if (p.getCategory() == UserPreference.Category.PRIMARY) {
				b.setTelephoneCountryCode(p.getCountryCode());
				b.setTelephone(p.getAreaCode() + p.getNumber());
				break;
			}
		}
		b.setEmail(mEmail);
		b.setLocation(mHomeAddress);

		// TODO: CC info

		return b;
	}

	//////////////////////////////////////////////////////////////////////////
	// Logging in/out

	public static boolean isLoggedIn(Context context) {
		boolean isLoggedIn = false;

		// Existence of the saved info indicates being logged in
		isLoggedIn |= context.getFileStreamPath(SAVED_INFO_FILENAME).exists();

		// Backwards compatible method for checking logged in state
		isLoggedIn |= context.getFileStreamPath(IS_USER_LOGGED_IN_FILE).exists();

		return isLoggedIn;
	}

	public static void signOut(Context context) {
		// Sign out cookies
		ExpediaServices services = new ExpediaServices(context);
		services.clearCookies();

		// Delete User
		delete(context);

		// Clear User from Db
		Db.setUser(null);
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
			obj.putOpt("email", mEmail);
			obj.putOpt("firstName", mFirstName);
			obj.putOpt("middleName", mMiddleName);
			obj.putOpt("lastName", mLastName);
			JSONUtils.putJSONableList(obj, "phoneNumbers", mPhoneNumbers);
			JSONUtils.putJSONable(obj, "homeAddress", mHomeAddress);
			JSONUtils.putJSONableList(obj, "storedCreditCards", mStoredCreditCards);
			JSONUtils.putJSONableList(obj, "associatedTravelers", mAssociatedTravelers);

			obj.putOpt("loyaltyMembershipNumber", mLoyaltyMembershipNumber);
			obj.putOpt("isSmokingPreferred", mIsSmokingPreferred);

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert User to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mTuid = obj.optLong("tuid");
		mEmail = obj.optString("email", null);
		mFirstName = obj.optString("firstName", null);
		mMiddleName = obj.optString("middleName", null);
		mLastName = obj.optString("lastName", null);

		JSONArray phoneNumbers = obj.optJSONArray("phoneNumbers");
		mPhoneNumbers = new ArrayList<Phone>();
		if (phoneNumbers != null) {
			for (int i = 0; i < phoneNumbers.length(); i++) {
				try {
					Phone p = new Phone(phoneNumbers.getJSONObject(i));
					mPhoneNumbers.add(p);
				}
				catch (JSONException e) {
					Log.e("Could not get phone number at i=" + i + ":", e);
				}
			}
		}

		JSONArray creditCards = obj.optJSONArray("storedCreditCards");
		mStoredCreditCards = new ArrayList<StoredCreditCard>();
		if (creditCards != null) {
			for (int i = 0; i < creditCards.length(); i++) {
				try {
					StoredCreditCard c = new StoredCreditCard(creditCards.getJSONObject(i));
					mStoredCreditCards.add(c);
				}
				catch (JSONException e) {
					Log.e("Could not get stored credit card at i=" + i + ":", e);
				}
			}
		}

		JSONArray associatedTravelers = obj.optJSONArray("associatedTravelers");
		mAssociatedTravelers = new ArrayList<Traveler>();
		if(associatedTravelers != null){
			for(int i = 0; i < associatedTravelers.length(); i++){
				try{
					Traveler fp = new Traveler();
					fp.fromJson(associatedTravelers.getJSONObject(i));
					mAssociatedTravelers.add(fp);
				}catch(JSONException e){
					Log.e("Could not get associated traveler at i=" + i + ":", e);
				}
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

			mHomeAddress = loc;
		}
		// TODO - frequent guest memberships
		mLoyaltyMembershipNumber = obj.optString("loyaltyMembershipNumber", null);
		mIsSmokingPreferred = obj.optBoolean("isSmokingPreferred", false);
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
