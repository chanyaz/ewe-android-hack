package com.expedia.bookings.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;

import com.expedia.bookings.utils.Strings;

public class Money {

	// Version of this class
	private static final int VERSION = 2;

	/**
	 * Flag to remove all value past the decimal point in formatting.
	 */
	public static final int F_NO_DECIMAL = 1;

	/**
	 * Flag to remove all value past the decimal point in formatting, if it is an Integer, else use 2 Decimal Places
	 */
	public static final int F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL = 2;

	/**
	 * Rounding Flags
	 */
	public static final int F_ROUND_DOWN = 4;
	public static final int F_ROUND_HALF_UP = 8;

	public BigDecimal amount;
	public String currencyCode = null;
	public String formattedPrice;
	public String formattedWholePrice;

	public Money() {
		amount = BigDecimal.ZERO;
	}

	public Money(Money oldMoney) {
		amount = oldMoney.getAmount();
		currencyCode = oldMoney.getCurrency();
	}

	public Money(String amount, String currency) {
		setAmount(amount);
		currencyCode = currency;
	}

	public Money(BigDecimal amount, String currency) {
		setAmount(amount);
		currencyCode = currency;
	}

	public Money copy() {
		return new Money(this);
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		if (Strings.isEmpty(amount)) {
			// Default to 0 for the amount value
			this.amount = new BigDecimal(0);
		}
		else {
			this.amount = new BigDecimal(amount);
		}
	}

	public void setAmount(double amount) {
		this.amount = new BigDecimal(amount);
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Handy utility for figuring out if this Money represents free.
	 *
	 * @return true if the amount is null or zero
	 */
	public boolean isZero() {
		return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
	}

	public boolean hasCents() {
		return amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) > 0;
	}

	public String getCurrency() {
		return currencyCode;
	}

	public void setCurrency(String currency) {
		this.currencyCode = currency;
	}

	public void setFormattedMoney(String formattedMoney) {
		this.formattedWholePrice = formattedMoney;
	}

	public boolean hasPreformatedMoney() {
		return formattedWholePrice != null;
	}

	public String getFormattedMoney() {
		return getFormattedMoney(0);
	}

	public String getFormattedMoney(int flags) {
		if (formattedWholePrice != null) {
			return formattedWholePrice;
		}
		else {
			return formatRate(amount, currencyCode, flags);
		}
	}

	public String getFormattedMoney(int flags, String currencyCode) {
		if (formattedWholePrice != null) {
			return formattedWholePrice;
		}
		else {
			return formatRate(amount, currencyCode, flags);
		}
	}

	public static String getFormattedMoneyFromAmountAndCurrencyCode(BigDecimal amount, String currencyCode) {
		if (amount != null && Strings.isNotEmpty(currencyCode)) {
			return formatRate(amount, currencyCode, 0);
		}
		else {
			throw new IllegalStateException(
				"amount != null && Strings.isNotEmpty(currencyCode) failed!");
		}
	}

	/**
	 * Adds one Money to this one.
	 * <p/>
	 * There are a number of situations where addition isn't possible, at which point
	 * the method will return false.  The situations are:
	 * <p/>
	 * 1. The parameter is null.
	 * 2. One or both Moneys only use a pre-formatted currency.
	 * 3. They explicitly use different currencies.
	 * <p/>
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
		if (currencyCode == null) {
			currencyCode = money.getCurrency();
		}

		// Do the addition
		amount = amount.add(money.getAmount());

		return true;
	}

	/**
	 * Acts just like add(), only subtracts the amount at the end.
	 *
	 * @see add()
	 */
	public boolean subtract(Money money) {
		if (!canManipulate(money)) {
			return false;
		}

		// Determine the result currency
		if (currencyCode == null) {
			currencyCode = money.getCurrency();
		}

		// Do the subtraction
		amount = amount.subtract(money.getAmount());

		return true;
	}

	public boolean negate() {
		if (!canManipulate(this)) {
			return false;
		}

		amount = amount.negate();
		return true;
	}

	public boolean add(BigDecimal amount) {
		if (!canManipulate(this)) {
			return false;
		}

		this.amount = this.amount.add(amount);

		return true;
	}

	public boolean subtract(BigDecimal amount) {
		if (!canManipulate(this)) {
			return false;
		}

		this.amount = this.amount.subtract(amount);

		return true;
	}

	private boolean canManipulate(Money money) {
		if (money == null) {
			return false;
		}
		else if (hasPreformatedMoney()) {
			return false;
		}
		else if (money.hasPreformatedMoney()) {
			return false;
		}
		else if (currencyCode != null && money.getCurrency() != null && !currencyCode.equals(money.getCurrency())) {
			return false;
		}

		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Money)) {
			return false;
		}

		Money other = (Money) o;

		return ((amount == null) == (other.amount == null))
			&& (amount == null || amount.equals(other.amount))
			&& ((currencyCode == null) == (other.currencyCode == null))
			&& (currencyCode == null || currencyCode.equals(other.currencyCode))
			&& ((formattedWholePrice == null) == (other.formattedWholePrice == null))
			&& (formattedWholePrice == null || formattedWholePrice.equals(other.formattedWholePrice));
	}

	public int compareTo(Money other) {
		if (equals(other)) {
			return 0;
		}

		if (other == null || other.getAmount() == null) {
			return 1;
		}

		return amount.compareTo(other.getAmount());
	}

	public int compareToTheWholeValue(Money other) {
		if (equals(other)) {
			return 0;
		}

		if (other == null || other.getAmount() == null) {
			return 1;
		}

		// Get the whole dollar amount
		int thisAmount = amount.intValue();
		int otherAmount = other.getAmount().intValue();

		return thisAmount - otherAmount;
	}

	// #7012 - INR on Android 2.1 is messed up, so we have to fix it here.
	private static final String INR_MESSED_UP = "=0#Rs.|1#Re.|1<Rs.";

	// #12791 - PHP (Philippine Peso) doesn't display correctly on SGS2 Gingerbread, so fix it here.
	private static final String PHP_CURRENCY_UNICODE = "₱";

	// #12855 - Indonesian POS shows the generic money symbol instead of Rp
	private static final String GENERIC_CURRENCY_UNICODE = "¤";

	// #13560 - No space between BRL currency and price
	private static final String BRL_CURRENCY_STRING = "R$";

	private static final String EURO_CURRENCY_UNICODE = "\u20AC";

	private static HashMap<Integer, NumberFormat> sFormats = new HashMap<Integer, NumberFormat>();

	private static String formatRate(BigDecimal amount, String currencyCode, int flags) {

		// Special case: if the Money does not have any decimal value, let's not show with decimal
		if (amount.scale() <= 0) {
			flags |= F_NO_DECIMAL;
		}

		// F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL trumps all other flags
		if ((flags & F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL) != 0) {
			flags = F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL;
		}

		// NumberFormat.getCurrencyInstance is slow. So let's try to cache it.
		// We want a different NumberFormat cached for each currencyCode/flags combination
		int key = (currencyCode + flags).hashCode();

		NumberFormat nf = sFormats.get(key);
		if (nf == null) {
			// We use the default user locale for both of these, as it should
			// be properly set by the Android system.
			Currency currency = Currency.getInstance(currencyCode);
			nf = NumberFormat.getCurrencyInstance();
			if (currency != null) {
				nf.setCurrency(currency);
				nf.setMaximumFractionDigits(currency.getDefaultFractionDigits());
			}

			if ((flags & F_NO_DECIMAL) != 0) {
				nf.setMaximumFractionDigits(0);
			}
			sFormats.put(key, nf);
		}

		//Handle F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL which trumps all other flags
		if ((flags & F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL) != 0) {
			nf = (NumberFormat) nf.clone();
			nf.setMaximumFractionDigits(amount.stripTrailingZeros().scale() <= 0 ? 0 : 2);
		}
		//Handle Rounding Flags
		else if ((flags & F_ROUND_HALF_UP) != 0) {
			if ((flags & F_NO_DECIMAL) != 0) {
				amount = amount.setScale(0, BigDecimal.ROUND_HALF_UP);
			}
			else {
				amount = amount.round(new MathContext(amount.precision() - amount.scale(), RoundingMode.HALF_UP));
			}
		}
		else if ((flags & F_ROUND_DOWN) != 0) {
			if ((flags & F_NO_DECIMAL) != 0) {
				amount = amount.setScale(0, BigDecimal.ROUND_DOWN);
			}
			else {
				amount = amount.round(new MathContext(amount.precision() - amount.scale(), RoundingMode.DOWN));
			}
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
		else if (currencyCode.equals("IDR")) {
			if (formatted.startsWith(GENERIC_CURRENCY_UNICODE)) {
				formatted = "Rp" + formatted.substring(GENERIC_CURRENCY_UNICODE.length());
			}
			else if (formatted.endsWith(GENERIC_CURRENCY_UNICODE)) {
				formatted = formatted.substring(0, formatted.length() - GENERIC_CURRENCY_UNICODE.length()) + "Rp";
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
