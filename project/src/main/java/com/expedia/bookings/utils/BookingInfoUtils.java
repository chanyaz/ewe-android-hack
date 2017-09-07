package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.user.UserStateManager;

public class BookingInfoUtils {

	public static List<StoredCreditCard> getStoredCreditCards(UserStateManager userStateManager) {
		List<StoredCreditCard> cards = new ArrayList<>();
		boolean seenSelectedCard = false;

		if (userStateManager.isUserAuthenticated() && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
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
	public static void resetPreviousCreditCardSelectState(UserStateManager userStateManager, StoredCreditCard creditCard) {
		// Check to find the desired credit card and reset his selectable state
		if (creditCard != null && userStateManager.isUserAuthenticated() && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
			List<StoredCreditCard> dbCards = Db.getUser().getStoredCreditCards();
			for (int i = 0; i < dbCards.size(); i++) {
				if (creditCard.compareTo(dbCards.get(i)) == 0) {
					Db.getUser().getStoredCreditCards().get(i).setIsSelectable(true);
				}
			}
		}
	}

	public static int getGreyCardIcon(PaymentType type) {
		type.assertIsCard();
		if (CREDIT_CARD_GREY_ICONS.containsKey(type)) {
			return CREDIT_CARD_GREY_ICONS.get(type);
		}
		return R.drawable.ic_generic_card;
	}

	public static int getWhiteCardIcon(PaymentType type) {
		type.assertIsCard();
		if (CREDIT_CARD_WHITE_ICONS.containsKey(type)) {
			return CREDIT_CARD_WHITE_ICONS.get(type);
		}
		return R.drawable.ic_generic_card_white;
	}

	public static int getColorfulCardIcon(PaymentType type) {
		type.assertIsCardOrPoints();
		if (CREDIT_CARD_COLORFUL_ICONS.containsKey(type)) {
			return CREDIT_CARD_COLORFUL_ICONS.get(type);
		}
		return R.drawable.ic_generic_card_colorful;
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
	private static final HashMap<PaymentType, Integer> CREDIT_CARD_COLORFUL_ICONS = new HashMap<PaymentType, Integer>() {
		{
			put(PaymentType.CARD_AMERICAN_EXPRESS, R.drawable.ic_amex_colorful);
			put(PaymentType.CARD_CARTE_BLANCHE, R.drawable.ic_carte_blanche_colorful);
			put(PaymentType.CARD_CARTE_BLEUE, R.drawable.ic_carte_bleue_colorful);
			put(PaymentType.CARD_CHINA_UNION_PAY, R.drawable.ic_union_pay_colorful);
			put(PaymentType.CARD_DINERS_CLUB, R.drawable.ic_diners_club_colorful);
			put(PaymentType.CARD_DISCOVER, R.drawable.ic_discover_colorful);
			put(PaymentType.CARD_JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_colorful);
			put(PaymentType.CARD_MAESTRO, R.drawable.ic_maestro_colorful);
			put(PaymentType.CARD_MASTERCARD, R.drawable.ic_mastercard_colorful);
			put(PaymentType.CARD_VISA, R.drawable.ic_visa_colorful);
			put(PaymentType.POINTS_REWARDS, R.drawable.pwp_icon);
			put(PaymentType.CARD_UNKNOWN, R.drawable.ic_unknown_card_colorful);
		}
	};
}
