package com.expedia.bookings.data;

import java.text.NumberFormat;
import java.util.Currency;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.AndroidUtils;

public class Money implements JSONable {

	/**
	 * Flag to automatically round down in formatting
	 */
	public static int F_ROUND_DOWN = 1;

	/**
	 * Flag to remove all value past the decimal point in formatting.
	 */
	public static int F_NO_DECIMAL = 2;

	private double mAmount;
	private String mCurrency;
	private String mFormattedMoney;

	public Money() {
		// Default constructor
	}

	public Money(Money oldMoney) {
		mAmount = oldMoney.getAmount();
		mCurrency = oldMoney.getCurrency();
	}

	public Money(JSONObject obj) {
		fromJson(obj);
	}

	public double getAmount() {
		return mAmount;
	}

	public void setAmount(double amount) {
		this.mAmount = amount;
	}

	public String getCurrency() {
		return mCurrency;
	}

	public void setCurrency(String currency) {
		this.mCurrency = currency;
	}

	public void setFormattedMoney(String formattedMoney) {
		this.mFormattedMoney = formattedMoney;
	}

	public boolean hasPreformatedMoney() {
		return mFormattedMoney != null;
	}

	public String getFormattedMoney() {
		return getFormattedMoney(0);
	}

	public String getFormattedMoney(int flags) {
		if (mFormattedMoney != null) {
			return mFormattedMoney;
		}
		else {
			return formatRate(mAmount, mCurrency, flags);
		}
	}

	public String getFormattedMoney(int flags, String currencyCode) {
		if (mFormattedMoney != null) {
			return mFormattedMoney;
		}
		else {
			return formatRate(mAmount, currencyCode, flags);
		}
	}

	/**
	 * Adds one Money to this one.
	 * 
	 * There are a number of situations where addition isn't possible, at which point
	 * the method will return false.  The situations are:
	 * 
	 * 1. The parameter is null.
	 * 2. One or both Moneys only use a pre-formatted currency.
	 * 3. They explicitly use different currencies.
	 * 
	 * In the case that either Money does not have a currency code defined, it is
	 * assumed that they use the same currency code.
	 * 
	 * @param money the Money to add to this one
	 * @return true if successful, false if they were not compatible to be added
	 */
	public boolean add(Money money) {
		if (!canManipulate(money)) {
			return false;
		}

		// Determine the result currency
		if (mCurrency == null) {
			mCurrency = money.getCurrency();
		}

		// Do the addition
		mAmount += money.getAmount();

		return true;
	}

	/**
	 * Acts just like add(), only subtracts the amount at the end.
	 * @see add()
	 */
	public boolean subtract(Money money) {
		if (!canManipulate(money)) {
			return false;
		}

		// Determine the result currency
		if (mCurrency == null) {
			mCurrency = money.getCurrency();
		}

		// Do the subtraction
		mAmount -= money.getAmount();

		return true;
	}

	public boolean negate() {
		if (!canManipulate(this)) {
			return false;
		}

		mAmount = -mAmount;
		return true;
	}

	private boolean canManipulate(Money money) {
		if (money == null) {
			Log.w("Could not add/subtract Moneys together; adding/subtracting Money is null.");
			return false;
		}
		else if (hasPreformatedMoney()) {
			Log.w("Could not add/subtract Moneys together; this Money is preformatted: \"" + getFormattedMoney() + "\"");
			return false;
		}
		else if (money.hasPreformatedMoney()) {
			Log.w("Could not add/subtract Moneys together; adding/subtracting Money is preformatted: \""
					+ money.getFormattedMoney() + "\"");
			return false;
		}
		else if (mCurrency != null && money.getCurrency() != null && !mCurrency.equals(money.getCurrency())) {
			Log.w("Could not add/subtract Moneys together; they have differnet currencies. this=" + mCurrency
					+ " other=" + money.getCurrency());
			return false;
		}

		return true;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("amount", mAmount);
			obj.putOpt("currency", mCurrency);
			obj.putOpt("formatted", mFormattedMoney);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Money object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mAmount = obj.optDouble("amount");
		mCurrency = obj.optString("currency", null);
		mFormattedMoney = obj.optString("formatted", null);
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

	public Money copy() {
		return new Money(toJson());
	}

	// #7012 - INR on Android 2.1 is messed up, so we have to fix it here.
	private static final String INR_MESSED_UP = "=0#Rs.|1#Re.|1<Rs.";

	// #12791 - PHP (Philippine Peso) doesn't display correctly on SGS2 Gingerbread, so fix it here.
	private static final String PHP_UNICODE = "₱";

	// #12855 - Indonesian POS shows the generic money symbol instead of Rp
	private static final String GENERIC_UNICODE = "¤";

	// #13560 - No space between BRL currency and price
	private static final String BRL_CURRENCY_STRING = "R$";

	private static String formatRate(double amount, String currencyCode, int flags) {
		// We use the default user locale for both of these, as it should
		// be properly set by the Android system.
		Currency currency = Currency.getInstance(currencyCode);
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		if (currency != null) {
			nf.setCurrency(currency);
			nf.setMaximumFractionDigits(currency.getDefaultFractionDigits());
		}

		if ((flags & F_ROUND_DOWN) != 0) {
			amount = Math.floor(amount);
		}

		if ((flags & F_NO_DECIMAL) != 0) {
			nf.setMaximumFractionDigits(0);
		}

		String formatted = nf.format(amount);

		// #7012 - INR on Android 2.1 is messed up, so we have to fix it manually here.
		if (currencyCode.equals("INR")) {
			if (formatted.startsWith(INR_MESSED_UP)) {
				formatted = "Rs" + formatted.substring(INR_MESSED_UP.length());
			}
			else if (formatted.endsWith(INR_MESSED_UP)) {
				formatted = formatted.substring(0, formatted.length() - INR_MESSED_UP.length()) + "Rs";
			}
		}

		// #12791 - PHP (Philippine Peso) doesn't display correctly on SGS2 Gingerbread, so fix it here.
		else if (currencyCode.equals("PHP") && AndroidUtils.isGalaxyS2() && AndroidUtils.getSdkVersion() <= 10) {
			if (formatted.startsWith(PHP_UNICODE)) {
				formatted = currencyCode + formatted.substring(PHP_UNICODE.length());
			}
			else if (formatted.endsWith(PHP_UNICODE)) {
				formatted = formatted.substring(0, formatted.length() - PHP_UNICODE.length()) + currencyCode;
			}
		}
		else if (currencyCode.equals("IDR")) {
			if (formatted.startsWith(GENERIC_UNICODE)) {
				formatted = "Rp" + formatted.substring(GENERIC_UNICODE.length());
			}
			else if (formatted.endsWith(GENERIC_UNICODE)) {
				formatted = formatted.substring(0, formatted.length() - GENERIC_UNICODE.length()) + "Rp";
			}
		}
		else if (currencyCode.equals("BRL")) {
			if (formatted.startsWith(BRL_CURRENCY_STRING) && formatted.charAt(2) != ' ') {
				formatted = "R$ " + formatted.substring(BRL_CURRENCY_STRING.length());
			}
			else if (formatted.endsWith(BRL_CURRENCY_STRING) && formatted.charAt(formatted.length() - 3) != ' ') {
				formatted = formatted.substring(0, formatted.length() - BRL_CURRENCY_STRING.length()) + " R$";
			}
		}

		return formatted;
	}
}
