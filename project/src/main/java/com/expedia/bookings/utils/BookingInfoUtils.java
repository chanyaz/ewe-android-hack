package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.model.FlightTravelerFlowState;
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.section.CommonSectionValidators;
import com.mobiata.android.Log;

public class BookingInfoUtils {


	/**
	 * This should get called before we do any sort of checkout network work.
	 *
	 * It ensures we have a valid checkout email address (returns false otherwise)
	 * and it copies important information from the provided traveler to the billing
	 * info object in Db.
	 *
	 * @return false if we couldnt find a valid email address to use, true otherwise.
	 */
	public static boolean migrateRequiredCheckoutDataToDbBillingInfo(Context context, LineOfBusiness lob,
		Traveler traveler) {

		//Ensure the correct (and valid) email address makes it to billing info
		String checkoutEmail = BookingInfoUtils.getCheckoutEmail(context, lob);
		if (!TextUtils.isEmpty(checkoutEmail)) {
			Db.getBillingInfo().setEmail(checkoutEmail);
		}
		else {
			//We tried to fix the email address, but failed. Do something drastic (this should very very very rarely happen)
			Db.getBillingInfo().setEmail(null);
			return false;
		}

		//Currently the gui has us setting phone info on traveler information entry screens, copy that business to BillingInfo
		BillingInfo billingInfo = Db.getBillingInfo();
		if (lob == LineOfBusiness.HOTELS) {
			billingInfo.setFirstName(traveler.getFirstName());
			billingInfo.setLastName(traveler.getLastName());
		}
		billingInfo.setTelephone(traveler.getPhoneNumber());
		billingInfo.setTelephoneCountryCode(traveler.getPhoneCountryCode());

		return true;
	}

	public static boolean travelerRequiresOverwritePrompt(Context context, Traveler workingTraveler) {
		boolean travelerAlreadyExistsOnAccount = false;
		if (workingTraveler.getSaveTravelerToExpediaAccount() && User.isLoggedIn(context)
			&& !workingTraveler.hasTuid()) {
			//If we want to save, and we're logged in, and we have a new traveler
			//We have to check if that travelers name matches an existing traveler
			if (Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null
				&& Db.getUser().getAssociatedTravelers().size() > 0) {
				for (Traveler trav : Db.getUser().getAssociatedTravelers()) {
					if (workingTraveler.nameEquals(trav)) {
						//A traveler with this name already exists on the account. Foo. ok so lets show a dialog and be all like "Hey yall, you wanna overwrite your buddy dave bob?"
						travelerAlreadyExistsOnAccount = true;
						break;
					}
				}
			}
		}
		return travelerAlreadyExistsOnAccount;
	}

	public static List<StoredCreditCard> getStoredCreditCards(Context context) {
		List<StoredCreditCard> cards = new ArrayList<>();
		boolean seenSelectedCard = false;

		if (User.isLoggedIn(context) && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
			List<StoredCreditCard> dbCards = Db.getUser().getStoredCreditCards();
			StoredCreditCard currentCard = Db.getBillingInfo().getStoredCard();
			if (currentCard != null && !seenSelectedCard) {
				for (int i = 0; i < dbCards.size(); i++) {
					if (currentCard.compareTo(dbCards.get(i)) == 0) {
						Db.getUser().getStoredCreditCards().get(i).setIsSelectable(false);
					}
				}
			}
			cards.addAll(dbCards);
		}

		return cards;
	}

	/**
	 * If the current card is replaced by another stored card from the list, let's reset {@link StoredCreditCard#isSelectable()} state.
	 * We need to do this so that the stored card is available to be selected again.
	 */
	public static void resetPreviousCreditCardSelectState(Context context, StoredCreditCard creditCard) {
		// Check to find the desired credit card and reset his selectable state
		if (creditCard != null && User.isLoggedIn(context) && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
			List<StoredCreditCard> dbCards = Db.getUser().getStoredCreditCards();
			for (int i = 0; i < dbCards.size(); i++) {
				if (creditCard.compareTo(dbCards.get(i)) == 0) {
					Db.getUser().getStoredCreditCards().get(i).setIsSelectable(true);
				}
			}
		}
	}

	public static List<Traveler> getAlternativeTravelers(Context context) {
		List<Traveler> travelers = new ArrayList<>();

		if (User.isLoggedIn(context) && Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null) {
			travelers.addAll(Db.getUser().getAssociatedTravelers());
		}

		return travelers;
	}

	public static boolean travelerInUse(Traveler traveler) {
		for (int j = 0; j < Db.getTravelers().size(); j++) {
			Traveler inUseTraveler = Db.getTravelers().get(j);
			if ((traveler.hasTuid() && inUseTraveler.hasTuid()
				&& traveler.getTuid().compareTo(inUseTraveler.getTuid()) == 0)) {
				return true;
			}
		}

		return false;
	}

	public static void insertTravelerDataIfNotFilled(Context context, Traveler traveler, LineOfBusiness lob) {
		if (traveler != null && Db.getTravelers() != null && Db.getTravelers().size() >= 1) {
			// If the first traveler is not already all the way filled out, and the
			// provided traveler has all the required data, then use that one instead
			boolean useNewTraveler = false;
			Traveler currentFirstTraveler = Db.getTravelers().get(0);

			if (lob == LineOfBusiness.HOTELS) {
				HotelTravelerFlowState state = HotelTravelerFlowState.getInstance(context);
				if (!state.hasValidTraveler(currentFirstTraveler)) {
					useNewTraveler = state.hasValidTraveler(traveler);
				}
			}
			else if (lob == LineOfBusiness.FLIGHTS) {
				FlightTravelerFlowState state = FlightTravelerFlowState.getInstance(context);
				if (Db.getTripBucket().getFlight().getFlightTrip().isInternational()) {
					// International
					useNewTraveler = !state.allTravelerInfoIsValidForInternationalFlight(currentFirstTraveler)
						&& state.allTravelerInfoIsValidForInternationalFlight(traveler);
				}
				else {
					// Domestic
					useNewTraveler = !state.allTravelerInfoIsValidForDomesticFlight(currentFirstTraveler)
						&& state.allTravelerInfoIsValidForDomesticFlight(traveler);
				}
			}

			if (useNewTraveler) {
				Db.getTravelers().set(0, traveler);
			}
		}
	}

	public static void populateTravelerData(LineOfBusiness lob) {
		List<Traveler> travelers = Db.getTravelers();
		if (travelers == null) {
			travelers = new ArrayList<>();
			Db.setTravelers(travelers);
		}

		// If there are more numAdults from HotelSearchParams, add empty Travelers to the Db to anticipate the addition of
		// new Travelers in order for check out
		final int travelerSize = travelers.size();
		int numTravelersNeeded;
		if (lob == LineOfBusiness.FLIGHTS) {
			numTravelersNeeded = Db.getTripBucket().getFlight().getFlightSearchParams().getNumTravelers();
			TravelerListGenerator gen = new TravelerListGenerator(
				Db.getTripBucket().getFlight().getFlightTrip().getPassengers(), travelers);
			Db.setTravelers(gen.generateTravelerList());
		}
		else {
			//Hotels currently always just has one traveler object
			numTravelersNeeded = 1;
			if (travelerSize < numTravelersNeeded) {
				for (int i = travelerSize; i < numTravelersNeeded; i++) {
					Traveler traveler = new Traveler();
					traveler.setPassengerCategory(PassengerCategory.ADULT);
					travelers.add(traveler);
				}
			}

			// If there are more Travelers than number of adults required by the HotelSearchParams, remove the extra Travelers,
			// although, keep the first numAdults Travelers.
			else if (travelerSize > numTravelersNeeded) {
				for (int i = travelerSize - 1; i >= numTravelersNeeded; i--) {
					travelers.remove(i);
				}
			}
		}
		Log.d("BookingInfoUtils - populateTravelerData - travelers.size():" + travelerSize + " numTravelersNeeded:"
			+ numTravelersNeeded);
	}

	/**
	 * Go through the Db.getTravelers list:
	 * If we are logged in, try to fill Db.getTravelers with our User's account travelers.
	 * If we are logged out, remove any user account travelers that might still be around.
	 */
	public static void populateTravelerDataFromUser(Context context, LineOfBusiness lob) {
		if (User.isLoggedIn(context)) {
			//Populate traveler data
			BookingInfoUtils.insertTravelerDataIfNotFilled(context, Db.getUser().getPrimaryTraveler(), lob);
		}
		else {
			for (int i = 0; i < Db.getTravelers().size(); i++) {
				//Travelers that have tuids are from the account and thus should be removed.
				if (Db.getTravelers().get(i).hasTuid()) {
					Db.getTravelers().set(i, new Traveler());
				}
				//We can't save travelers to an account if we aren't logged in, so we unset the flag
				Db.getTravelers().get(i).setSaveTravelerToExpediaAccount(false);
			}
		}
	}

	/**
	 * Rectify the provided BillingInfo object with the logged in user
	 *
	 * @return true if billing info was updated;
	 */
	public static boolean populatePaymentDataFromUser(Context context, LineOfBusiness lob) {
		BillingInfo info = Db.getBillingInfo();
		if (User.isLoggedIn(context)) {
			// Populate Credit Card only if the user doesn't have any manually entered (or selected) data
			if (Db.getUser().getStoredCreditCards() != null && Db.getUser().getStoredCreditCards().size() == 1
				&& !hasSomeManuallyEnteredData(info) && !info.hasStoredCard()) {
				StoredCreditCard scc = Db.getUser().getStoredCreditCards().get(0);

				if (lob == LineOfBusiness.FLIGHTS) {
					// Make sure the card is supported by this flight trip before automatically selecting it
					if (Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().getFlightTrip() != null &&
						Db.getTripBucket().getFlight().isPaymentTypeSupported(scc.getType())) {

						info.setStoredCard(scc);

						Db.getTripBucket().getFlight().getFlightTrip().setShowFareWithCardFee(true);
						//mListener.onBillingInfoChange();
						return true;
					}
				}
				else {
					if (Db.getTripBucket().getHotel() != null &&
						Db.getTripBucket().getHotel().isPaymentTypeSupported(scc.getType())) {
						info.setStoredCard(scc);
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Return true if the provided billing info object contains manually entered data.
	 */
	public static boolean hasSomeManuallyEnteredData(BillingInfo info) {
		if (info == null) {
			return false;
		}

		if (info.getLocation() == null) {
			return false;
		}
		//Checkout the major fields, if any of them have data, then we know some data has been manually enetered
		if (!TextUtils.isEmpty(info.getLocation().getStreetAddressString())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getCity())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getPostalCode())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getStateCode())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getNameOnCard())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getNumber())) {
			return true;
		}
		return false;
	}

	/**
	 * This looks through our various static data and tries to determine the email address to use at checkout.
	 *
	 * @return email address to use for checkout or null if no valid email addresses were found
	 */
	public static String getCheckoutEmail(Context context, LineOfBusiness lob) {
		Log.d("getCheckoutEmail");
		//Ensure we have billingInfo (this is called getCheckoutEmail after all, so we should have checkout information)

		//Get User email...
		String userEmail = null;
		if (User.isLoggedIn(context)) {
			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
				&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
				String email = Db.getUser().getPrimaryTraveler().getEmail();
				if (CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(email) == 0) {
					Log.d("getCheckoutEmail - found Db.getUser().getPrimaryTraveler().getEmail():" + email);
					userEmail = email;
				}
			}
		}

		//Get BillingInfo email
		String billingInfoEmail = null;
		if (Db.hasBillingInfo()) {
			if (!TextUtils.isEmpty(Db.getBillingInfo().getEmail())) {
				String email = Db.getBillingInfo().getEmail();
				if (CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(email) == 0) {
					Log.d("getCheckoutEmail - found Db.getBillingInfo().getEmail():" + email);
					billingInfoEmail = email;
				}
			}
		}

		//Get traveler email
		String travelerEmail = null;
		if (Db.getTravelers() != null && Db.getTravelers().size() > 0 && Db.getTravelers().get(0) != null
			&& !TextUtils.isEmpty(Db.getTravelers().get(0).getEmail())) {
			String email = Db.getTravelers().get(0).getEmail();
			if (CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(email) == 0) {
				Log.d("getCheckoutEmail - found Db.getTravelers().get(0).getEmail():" + email);
				travelerEmail = email;
			}
		}

		//Choose best email address based on priority and availability.
		String retEmail = null;
		if (!TextUtils.isEmpty(userEmail)) {
			//If we are logged in, always use that email address
			retEmail = userEmail;
		}
		else {
			//If we aren't logged in we need to wade through other sources, based on lineOfBusiness

			if (lob == LineOfBusiness.FLIGHTS) {
				//In flights email is associated with billingInfo. So if we have a wallet email, we use that, because it replaces our payment method.
				//Failing that we try the BillingInfo, because that is where we put manually entered email addresses. Finally we try traveler, because
				//the user may have entered an address in Hotels that is to be used now.
				if (!TextUtils.isEmpty(billingInfoEmail)) {
					retEmail = billingInfoEmail;
				}
				else if (!TextUtils.isEmpty(travelerEmail)) {
					retEmail = travelerEmail;
				}
			}
			else {
				//In hotels email is associated with the traveler. So we first look at the travelerEmail.
				//Failing that we look billingInfo because we are desperate.
				if (!TextUtils.isEmpty(travelerEmail)) {
					retEmail = travelerEmail;
				}
				else if (!TextUtils.isEmpty(billingInfoEmail)) {
					retEmail = billingInfoEmail;
				}
			}
		}

		Log.d("getCheckoutEmail - returning email:" + retEmail);
		return retEmail;
	}

	public static final int getGreyCardIcon(PaymentType type) {
		type.assertIsCard();
		if (CREDIT_CARD_GREY_ICONS.containsKey(type)) {
			return CREDIT_CARD_GREY_ICONS.get(type);
		}
		return R.drawable.ic_generic_card;
	}

	public static final int getBlackCardIcon(PaymentType type) {
		type.assertIsCard();
		if (CREDIT_CARD_BLACK_ICONS.containsKey(type)) {
			return CREDIT_CARD_BLACK_ICONS.get(type);
		}
		return R.drawable.ic_generic_card_black;
	}

	public static final int getWhiteCardIcon(PaymentType type) {
		type.assertIsCard();
		if (CREDIT_CARD_WHITE_ICONS.containsKey(type)) {
			return CREDIT_CARD_WHITE_ICONS.get(type);
		}
		return R.drawable.ic_generic_card_white;
	}

	public static final int getTabletCardIcon(PaymentType type) {
		type.assertIsCardOrPoints();
		if (CREDIT_CARD_TABLET_ICONS.containsKey(type)) {
			return CREDIT_CARD_TABLET_ICONS.get(type);
		}
		return R.drawable.ic_tablet_checkout_generic_credit_card;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// More static data (that just takes up a lot of space, so at bottom)

	// Which icon to use with which credit card
	@SuppressWarnings("serial")
	private static final HashMap<PaymentType, Integer> CREDIT_CARD_GREY_ICONS = new HashMap<PaymentType, Integer>() {
		{
			put(PaymentType.CARD_AMERICAN_EXPRESS, R.drawable.ic_amex_grey);
			put(PaymentType.CARD_CARTE_BLANCHE, R.drawable.ic_carte_blanche_grey);
			put(PaymentType.CARD_CARTE_BLEUE, R.drawable.ic_carte_bleue_grey);
			put(PaymentType.CARD_CHINA_UNION_PAY, R.drawable.ic_union_pay_grey);
			put(PaymentType.CARD_DINERS_CLUB, R.drawable.ic_diners_club_grey);
			put(PaymentType.CARD_DISCOVER, R.drawable.ic_discover_grey);
			put(PaymentType.CARD_JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_grey);
			put(PaymentType.CARD_MAESTRO, R.drawable.ic_maestro_grey);
			put(PaymentType.CARD_MASTERCARD, R.drawable.ic_master_card_grey);
			put(PaymentType.CARD_VISA, R.drawable.ic_visa_grey);
		}
	};

	// Which icon to use with which credit card
	@SuppressWarnings("serial")
	private static final HashMap<PaymentType, Integer> CREDIT_CARD_BLACK_ICONS = new HashMap<PaymentType, Integer>() {
		{
			put(PaymentType.CARD_AMERICAN_EXPRESS, R.drawable.ic_amex_black);
			put(PaymentType.CARD_CARTE_BLANCHE, R.drawable.ic_carte_blanche_black);
			put(PaymentType.CARD_CARTE_BLEUE, R.drawable.ic_carte_bleue_black);
			put(PaymentType.CARD_CHINA_UNION_PAY, R.drawable.ic_union_pay_black);
			put(PaymentType.CARD_DINERS_CLUB, R.drawable.ic_diners_club_black);
			put(PaymentType.CARD_DISCOVER, R.drawable.ic_discover_black);
			put(PaymentType.CARD_JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_black);
			put(PaymentType.CARD_MAESTRO, R.drawable.ic_maestro_black);
			put(PaymentType.CARD_MASTERCARD, R.drawable.ic_master_card_black);
			put(PaymentType.CARD_VISA, R.drawable.ic_visa_black);
		}
	};

	// Which icon to use with which credit card
	@SuppressWarnings("serial")
	private static final HashMap<PaymentType, Integer> CREDIT_CARD_WHITE_ICONS = new HashMap<PaymentType, Integer>() {
		{
			put(PaymentType.CARD_AMERICAN_EXPRESS, R.drawable.ic_amex_white);
			put(PaymentType.CARD_CARTE_BLANCHE, R.drawable.ic_carte_blanche_white);
			put(PaymentType.CARD_CARTE_BLEUE, R.drawable.ic_carte_bleue_white);
			put(PaymentType.CARD_CHINA_UNION_PAY, R.drawable.ic_union_pay_white);
			put(PaymentType.CARD_DINERS_CLUB, R.drawable.ic_diners_club_white);
			put(PaymentType.CARD_DISCOVER, R.drawable.ic_discover_white);
			put(PaymentType.CARD_JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_white);
			put(PaymentType.CARD_MAESTRO, R.drawable.ic_maestro_white);
			put(PaymentType.CARD_MASTERCARD, R.drawable.ic_master_card_white);
			put(PaymentType.CARD_VISA, R.drawable.ic_visa_white);
		}
	};

	@SuppressWarnings("serial")
	private static final HashMap<PaymentType, Integer> CREDIT_CARD_TABLET_ICONS = new HashMap<PaymentType, Integer>() {
		{
			put(PaymentType.CARD_AMERICAN_EXPRESS, R.drawable.ic_tablet_checkout_amex);
			put(PaymentType.CARD_CARTE_BLANCHE, R.drawable.ic_tablet_checkout_carte_blanche);
			put(PaymentType.CARD_CARTE_BLEUE, R.drawable.ic_tablet_checkout_carte_bleue);
			put(PaymentType.CARD_CHINA_UNION_PAY, R.drawable.ic_tablet_checkout_union_pay);
			put(PaymentType.CARD_DINERS_CLUB, R.drawable.ic_tablet_checkout_diners_club);
			put(PaymentType.CARD_DISCOVER, R.drawable.ic_tablet_checkout_discover);
			put(PaymentType.CARD_JAPAN_CREDIT_BUREAU, R.drawable.ic_tablet_checkout_jcb);
			put(PaymentType.CARD_MAESTRO, R.drawable.ic_tablet_checkout_maestro);
			put(PaymentType.CARD_MASTERCARD, R.drawable.ic_tablet_checkout_mastercard);
			put(PaymentType.CARD_VISA, R.drawable.ic_tablet_checkout_visa);
			put(PaymentType.POINTS_REWARDS, R.drawable.pwp_icon);
		}
	};
}
