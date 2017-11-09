package com.expedia.bookings.data.user;

import android.support.annotation.Nullable;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.StoredPointsCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.utils.CollectionUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User implements JSONable {

	// Version of this User
	private static final int VERSION = 2;

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

	protected User(JSONObject userData) {
		fromJson(userData);
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
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.put("version", VERSION);
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
}
