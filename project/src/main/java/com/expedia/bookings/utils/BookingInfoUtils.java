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
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.Log;

public class BookingInfoUtils {


	/**
	 * This should get called before we do any sort of checkout network work.
	 *
	 * It ensures we have a valid checkout email address (returns false otherwise)
	 * and it copies important information from the provided traveler to the billing
	 * info object in Db.
	 *
	 * @param context
	 * @param lob
	 * @param traveler
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
					if (workingTraveler.compareNameTo(trav) == 0) {
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
		List<StoredCreditCard> cards = new ArrayList<StoredCreditCard>();
		boolean seenSelectedCard = false;

		if (Db.getMaskedWallet() != null) {
			StoredCreditCard walletCard = WalletUtils.convertToStoredCreditCard(Db.getMaskedWallet());
			StoredCreditCard currentCard = Db.getBillingInfo().getStoredCard();
			if (currentCard != null && currentCard.compareTo(walletCard) == 0) {
				walletCard.setIsSelectable(false);
				seenSelectedCard = true;
			}
			cards.add(walletCard);
		}

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
	 * @param creditCard
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
		List<Traveler> travelers = new ArrayList<Traveler>();

		if (Db.getGoogleWalletTraveler() != null) {
			travelers.add(Db.getGoogleWalletTraveler());
		}

		if (User.isLoggedIn(context) && Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null) {
			travelers.addAll(Db.getUser().getAssociatedTravelers());
		}

		return travelers;
	}

	public static boolean travelerInUse(Traveler traveler) {
		for (int j = 0; j < Db.getTravelers().size(); j++) {
			Traveler inUseTraveler = Db.getTravelers().get(j);
			if ((traveler.hasTuid() && inUseTraveler.hasTuid()
				&& traveler.getTuid().compareTo(inUseTraveler.getTuid()) == 0)
				|| (traveler.fromGoogleWallet() && inUseTraveler.fromGoogleWallet())) {
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

			if (traveler.fromGoogleWallet() && currentFirstTraveler.fromGoogleWallet()) {
				// If the current user is from GWallet (and no edits have been made, signified
				// by the fact that we still think it is from GWallet) then replace it automatically.
				useNewTraveler = true;
			}
			else if (lob == LineOfBusiness.HOTELS) {
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
			travelers = new ArrayList<Traveler>();
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
	 *
	 * @param context
	 * @param lob
	 */
	public static void populateTravelerDataFromUser(Context context, LineOfBusiness lob) {
		if (User.isLoggedIn(context)) {
			if (Db.getUser() == null) {
				Db.loadUser(context);
			}
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
	 * @param context
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
		else if (Db.getMaskedWallet() == null) {
			//Remove stored card(s)
			info.setStoredCard(null);
			//Turn off the save to expedia account flag
			info.setSaveCardToExpediaAccount(false);
		}
		return false;
	}

	/**
	 * Return true if the provided billing info object contains manually entered data.
	 *
	 * @param info
	 * @return
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
	 * @param context
	 * @return email address to use for checkout or null if no valid email addresses were found
	 */
	public static String getCheckoutEmail(Context context, LineOfBusiness lob) {
		Log.d("getCheckoutEmail");
		//Ensure we have billingInfo (this is called getCheckoutEmail after all, so we should have checkout information)

		//Get User email...
		String userEmail = null;
		if (User.isLoggedIn(context)) {
			if (Db.getUser() == null) {
				Db.loadUser(context);
			}
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

		//Get google wallet email...
		String walletEmail = null;
		if (Db.hasBillingInfo()) {
			if (Db.getBillingInfo().getStoredCard() != null && Db.getBillingInfo().getStoredCard().isGoogleWallet()) {
				MaskedWallet wallet = Db.getMaskedWallet();
				if (wallet != null && !TextUtils.isEmpty(wallet.getEmail())) {
					String email = wallet.getEmail();
					if (CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(email) == 0) {
						Log.d("getCheckoutEmail - found Db.getMaskedWallet().getEmail():" + email);
						walletEmail = email;
					}
				}
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
				if (!TextUtils.isEmpty(walletEmail)) {
					retEmail = walletEmail;
				}
				else if (!TextUtils.isEmpty(billingInfoEmail)) {
					retEmail = billingInfoEmail;
				}
				else if (!TextUtils.isEmpty(travelerEmail)) {
					retEmail = travelerEmail;
				}
			}
			else {
				//In hotels email is associated with the traveler. So we first look at the travelerEmail. Failing that we look in wallet, but wallet should
				//have copied its email over to traveler already, so that should be rare, finally we check billingInfo because we are desperate.
				if (!TextUtils.isEmpty(travelerEmail)) {
					retEmail = travelerEmail;
				}
				else if (!TextUtils.isEmpty(walletEmail)) {
					retEmail = walletEmail;
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
		CreditCardUtils.assertPaymentTypeIsCardOrGoogleWallet(type);
		if (CREDIT_CARD_GREY_ICONS.containsKey(type)) {
			return CREDIT_CARD_GREY_ICONS.get(type);
		}
		return R.drawable.ic_generic_card;
	}

	public static final int getBlackCardIcon(PaymentType type) {
		CreditCardUtils.assertPaymentTypeIsCardOrGoogleWallet(type);
		if (CREDIT_CARD_BLACK_ICONS.containsKey(type)) {
			return CREDIT_CARD_BLACK_ICONS.get(type);
		}
		return R.drawable.ic_generic_card_black;
	}

	public static final int getWhiteCardIcon(PaymentType type) {
		CreditCardUtils.assertPaymentTypeIsCardOrGoogleWallet(type);
		if (CREDIT_CARD_WHITE_ICONS.containsKey(type)) {
			return CREDIT_CARD_WHITE_ICONS.get(type);
		}
		return R.drawable.ic_generic_card_white;
	}

	public static final int getTabletCardIcon(PaymentType type) {
		CreditCardUtils.assertPaymentTypeIsCardOrGoogleWallet(type);
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
			put(PaymentType.WALLET_GOOGLE, R.drawable.ic_tablet_checkout_google_wallet);
		}
	};

	// Static data that auto-fills states/countries
	@SuppressWarnings("serial")
	public static final HashMap<CharSequence, Integer> COMMON_US_CITIES = new HashMap<CharSequence, Integer>() {
		{
			put("new york", R.string.state_code_ny);
			put("los angeles", R.string.state_code_ca);
			put("chicago", R.string.state_code_il);
			put("houston", R.string.state_code_tx);
			put("philadelphia", R.string.state_code_pa);
			put("phoenix", R.string.state_code_az);
			put("san antonio", R.string.state_code_tx);
			put("san diego", R.string.state_code_ca);
			put("dallas", R.string.state_code_tx);
			put("san jose", R.string.state_code_ca);
			put("jacksonville", R.string.state_code_fl);
			put("indianapolis", R.string.state_code_in);
			put("san francisco", R.string.state_code_ca);
			put("austin", R.string.state_code_tx);
			put("columbus", R.string.state_code_oh);
			put("fort worth", R.string.state_code_tx);
			put("charlotte", R.string.state_code_nc);
			put("detroit", R.string.state_code_mi);
			put("el paso", R.string.state_code_tx);
			put("memphis", R.string.state_code_tn);
			put("baltimore", R.string.state_code_md);
			put("boston", R.string.state_code_ma);
			put("seattle", R.string.state_code_wa);
			put("washington", R.string.state_code_dc);
			put("nashville", R.string.state_code_tn);
			put("denver", R.string.state_code_co);
			put("louisville", R.string.state_code_ky);
			put("milwaukee", R.string.state_code_wi);
			put("portland", R.string.state_code_or);
			put("las vegas", R.string.state_code_nv);
			put("oklahoma city", R.string.state_code_ok);
			put("albuquerque", R.string.state_code_nm);
			put("tucson", R.string.state_code_az);
			put("fresno", R.string.state_code_ca);
			put("sacramento", R.string.state_code_ca);
			put("long beach", R.string.state_code_ca);
			put("kansas city", R.string.state_code_mo);
			put("mesa", R.string.state_code_az);
			put("virginia beach", R.string.state_code_va);
			put("atlanta", R.string.state_code_ga);
			put("colorado springs", R.string.state_code_co);
			put("omaha", R.string.state_code_ne);
			put("raleigh", R.string.state_code_nc);
			put("miami", R.string.state_code_fl);
			put("cleveland", R.string.state_code_oh);
			put("tulsa", R.string.state_code_ok);
			put("oakland", R.string.state_code_ca);
			put("minneapolis", R.string.state_code_mn);
			put("wichita", R.string.state_code_ks);
			put("arlington", R.string.state_code_tx);
			put("bakersfield", R.string.state_code_ca);
			put("new orleans", R.string.state_code_la);
			put("honolulu", R.string.state_code_hi);
			put("anaheim", R.string.state_code_ca);
			put("tampa", R.string.state_code_fl);
			put("aurora", R.string.state_code_co);
			put("santa ana", R.string.state_code_ca);
			put("st louis", R.string.state_code_mo);
			put("pittsburgh", R.string.state_code_pa);
			put("corpus christi", R.string.state_code_tx);
			put("riverside", R.string.state_code_ca);
			put("cincinnati", R.string.state_code_oh);
			put("lexington", R.string.state_code_ky);
			put("anchorage", R.string.state_code_ak);
			put("stockton", R.string.state_code_ca);
			put("toledo", R.string.state_code_oh);
			put("st paul", R.string.state_code_mn);
			put("newark", R.string.state_code_nj);
			put("greensboro", R.string.state_code_nc);
			put("buffalo", R.string.state_code_ny);
			put("plano", R.string.state_code_tx);
			put("lincoln", R.string.state_code_ne);
			put("henderson", R.string.state_code_nv);
			put("fort wayne", R.string.state_code_in);
			put("jersey city", R.string.state_code_nj);
			put("st petersburg", R.string.state_code_fl);
			put("chula vista", R.string.state_code_ca);
			put("norfolk", R.string.state_code_va);
			put("orlando", R.string.state_code_fl);
			put("chandler", R.string.state_code_az);
			put("laredo", R.string.state_code_tx);
			put("madison", R.string.state_code_wi);
			put("winston-salem", R.string.state_code_nc);
			put("lubbock", R.string.state_code_tx);
			put("baton rouge", R.string.state_code_la);
			put("durham", R.string.state_code_nc);
			put("garland", R.string.state_code_tx);
			put("glendale", R.string.state_code_az);
			put("reno", R.string.state_code_nv);
			put("hialeah", R.string.state_code_fl);
			put("paradise", R.string.state_code_nv);
			put("chesapeake", R.string.state_code_va);
			put("scottsdale", R.string.state_code_az);
			put("north las vegas", R.string.state_code_nv);
			put("irving", R.string.state_code_tx);
			put("fremont", R.string.state_code_ca);
			put("irvine", R.string.state_code_ca);
			put("birmingham", R.string.state_code_al);
			put("rochester", R.string.state_code_ny);
			put("san bernardino", R.string.state_code_ca);
			put("spokane", R.string.state_code_wa);
			put("gilbert", R.string.state_code_az);
			put("arlington", R.string.state_code_va);
			put("montgomery", R.string.state_code_al);
			put("boise", R.string.state_code_id);
			put("richmond", R.string.state_code_va);
			put("des moines", R.string.state_code_ia);
			put("modesto", R.string.state_code_ca);
			put("fayetteville", R.string.state_code_nc);
			put("shreveport", R.string.state_code_la);
			put("akron", R.string.state_code_oh);
			put("tacoma", R.string.state_code_wa);
			put("aurora", R.string.state_code_il);
			put("oxnard", R.string.state_code_ca);
			put("fontana", R.string.state_code_ca);
			put("yonkers", R.string.state_code_ny);
			put("augusta", R.string.state_code_ga);
			put("mobile", R.string.state_code_al);
			put("little rock", R.string.state_code_ar);
			put("moreno valley", R.string.state_code_ca);
			put("glendale", R.string.state_code_ca);
			put("amarillo", R.string.state_code_tx);
			put("huntington beach", R.string.state_code_ca);
			put("columbus", R.string.state_code_ga);
			put("grand rapids", R.string.state_code_mi);
			put("salt lake city", R.string.state_code_ut);
			put("tallahassee", R.string.state_code_fl);
			put("worcester", R.string.state_code_ma);
			put("newport news", R.string.state_code_va);
			put("huntsville", R.string.state_code_al);
			put("knoxville", R.string.state_code_tn);
			put("providence", R.string.state_code_ri);
			put("santa clarita", R.string.state_code_ca);
			put("grand prairie", R.string.state_code_tx);
			put("brownsville", R.string.state_code_tx);
			put("jackson", R.string.state_code_ms);
			put("overland park", R.string.state_code_ks);
			put("garden grove", R.string.state_code_ca);
			put("santa rosa", R.string.state_code_ca);
			put("chattanooga", R.string.state_code_tn);
			put("oceanside", R.string.state_code_ca);
			put("fort lauderdale", R.string.state_code_fl);
			put("rancho cucamonga", R.string.state_code_ca);
			put("port st. lucie", R.string.state_code_fl);
			put("ontario", R.string.state_code_ca);
			put("vancouver", R.string.state_code_wa);
			put("tempe", R.string.state_code_az);
			put("springfield", R.string.state_code_mo);
			put("lancaster", R.string.state_code_ca);
			put("eugene", R.string.state_code_or);
			put("pembroke pines", R.string.state_code_fl);
			put("salem", R.string.state_code_or);
			put("cape coral", R.string.state_code_fl);
			put("peoria", R.string.state_code_az);
			put("sioux falls", R.string.state_code_sd);
			put("springfield", R.string.state_code_ma);
			put("elk grove", R.string.state_code_ca);
			put("rockford", R.string.state_code_il);
			put("palmdale", R.string.state_code_ca);
			put("corona", R.string.state_code_ca);
			put("salinas", R.string.state_code_ca);
			put("pomona", R.string.state_code_ca);
			put("pasadena", R.string.state_code_tx);
			put("joliet", R.string.state_code_il);
			put("paterson", R.string.state_code_nj);
			put("kansas city", R.string.state_code_ks);
			put("torrance", R.string.state_code_ca);
			put("syracuse", R.string.state_code_ny);
			put("bridgeport", R.string.state_code_ct);
			put("hayward", R.string.state_code_ca);
			put("fort collins", R.string.state_code_co);
			put("escondido", R.string.state_code_ca);
			put("lakewood", R.string.state_code_co);
			put("naperville", R.string.state_code_il);
			put("dayton", R.string.state_code_oh);
			put("hollywood", R.string.state_code_fl);
			put("sunnyvale", R.string.state_code_ca);
			put("alexandria", R.string.state_code_va);
			put("mesquite", R.string.state_code_tx);
			put("hampton", R.string.state_code_va);
			put("pasadena", R.string.state_code_ca);
			put("orange", R.string.state_code_ca);
			put("savannah", R.string.state_code_ga);
			put("cary", R.string.state_code_nc);
			put("fullerton", R.string.state_code_ca);
			put("warren", R.string.state_code_mi);
			put("clarksville", R.string.state_code_tn);
			put("mckinney", R.string.state_code_tx);
			put("mcallen", R.string.state_code_tx);
			put("new haven", R.string.state_code_ct);
			put("sterling heights", R.string.state_code_mi);
			put("west valley city", R.string.state_code_ut);
			put("columbia", R.string.state_code_sc);
			put("killeen", R.string.state_code_tx);
			put("topeka", R.string.state_code_ks);
			put("thousand oaks", R.string.state_code_ca);
			put("cedar rapids", R.string.state_code_ia);
			put("olathe", R.string.state_code_ks);
			put("elizabeth", R.string.state_code_nj);
			put("waco", R.string.state_code_tx);
			put("hartford", R.string.state_code_ct);
			put("visalia", R.string.state_code_ca);
			put("gainesville", R.string.state_code_fl);
			put("simi valley", R.string.state_code_ca);
			put("stamford", R.string.state_code_ct);
			put("bellevue", R.string.state_code_wa);
			put("concord", R.string.state_code_ca);
			put("miramar", R.string.state_code_fl);
			put("coral springs", R.string.state_code_fl);
			put("lafayette", R.string.state_code_la);
			put("charleston", R.string.state_code_sc);
			put("carrollton", R.string.state_code_tx);
			put("roseville", R.string.state_code_ca);
			put("thornton", R.string.state_code_co);
			put("beaumont", R.string.state_code_tx);
			put("allentown", R.string.state_code_pa);
			put("surprise", R.string.state_code_az);
			put("evansville", R.string.state_code_in);
			put("abilene", R.string.state_code_tx);
			put("frisco", R.string.state_code_tx);
			put("independence", R.string.state_code_mo);
			put("santa clara", R.string.state_code_ca);
			put("springfield", R.string.state_code_il);
			put("vallejo", R.string.state_code_ca);
			put("victorville", R.string.state_code_ca);
			put("athens", R.string.state_code_ga);
			put("peoria", R.string.state_code_il);
			put("lansing", R.string.state_code_mi);
			put("ann arbor", R.string.state_code_mi);
			put("el monte", R.string.state_code_ca);
			put("denton", R.string.state_code_tx);
			put("berkeley", R.string.state_code_ca);
			put("provo", R.string.state_code_ut);
			put("downey", R.string.state_code_ca);
			put("midland", R.string.state_code_tx);
			put("norman", R.string.state_code_ok);
			put("waterbury", R.string.state_code_ct);
			put("costa mesa", R.string.state_code_ca);
			put("inglewood", R.string.state_code_ca);
			put("manchester", R.string.state_code_nh);
			put("murfreesboro", R.string.state_code_tn);
			put("columbia", R.string.state_code_mo);
			put("elgin", R.string.state_code_il);
			put("clearwater", R.string.state_code_fl);
			put("miami gardens", R.string.state_code_fl);
			put("rochester", R.string.state_code_mn);
			put("pueblo", R.string.state_code_co);
			put("lowell", R.string.state_code_ma);
			put("wilmington", R.string.state_code_nc);
			put("arvada", R.string.state_code_co);
			put("ventura", R.string.state_code_ca);
			put("westminster", R.string.state_code_co);
			put("west covina", R.string.state_code_ca);
			put("gresham", R.string.state_code_or);
			put("fargo", R.string.state_code_nd);
			put("norwalk", R.string.state_code_ca);
			put("carlsbad", R.string.state_code_ca);
			put("fairfield", R.string.state_code_ca);
			put("cambridge", R.string.state_code_ma);
			put("wichita falls", R.string.state_code_tx);
			put("high point", R.string.state_code_nc);
			put("billings", R.string.state_code_mt);
			put("green bay", R.string.state_code_wi);
			put("west jordan", R.string.state_code_ut);
			put("richmond", R.string.state_code_ca);
			put("murrieta", R.string.state_code_ca);
			put("burbank", R.string.state_code_ca);
			put("palm bay", R.string.state_code_fl);
			put("everett", R.string.state_code_wa);
			put("flint", R.string.state_code_mi);
			put("antioch", R.string.state_code_ca);
			put("erie", R.string.state_code_pa);
			put("south bend", R.string.state_code_in);
			put("daly city", R.string.state_code_ca);
			put("centennial", R.string.state_code_co);
			put("temecula", R.string.state_code_ca);
		}
	};
}
