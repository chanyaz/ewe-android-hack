package com.expedia.bookings.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import com.expedia.bookings.utils.Strings;

public class Money {

	/**
	 * Flag to remove all value past the decimal point in formatting.
	 */
	public static final int F_NO_DECIMAL = 1;

	/**
	 * Flag to remove all value past the decimal point in formatting, if it is an Integer, else use 2 Decimal Places
	 */
	public static final int F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL = 2;

	/**
	 * Flag to always use 2 Decimal Places
	 */
	public static final int F_ALWAYS_TWO_PLACES_AFTER_DECIMAL = 4;

	/**
	 * Rounding Flags
	 */
	public static final int F_ROUND_HALF_UP = 8;

	public BigDecimal amount;
	public String currencyCode;
	public String formattedPrice;
	public String formattedWholePrice;
	public BigDecimal roundedAmount;

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

	public Money(int amount, String currency) {
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
			this.amount = BigDecimal.ZERO;
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

	public boolean isLessThanZero() {
		return amount == null || amount.signum() == -1;
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

	public boolean hasPreformatedMoney() {
		return Strings.isNotEmpty(formattedPrice) || Strings.isNotEmpty(formattedWholePrice);
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

	public String getFormattedMoneyFromAmountAndCurrencyCode() {
		return getFormattedMoneyFromAmountAndCurrencyCode(amount, currencyCode, 0);
	}

	public String getFormattedMoneyFromAmountAndCurrencyCode(int flags) {
		return getFormattedMoneyFromAmountAndCurrencyCode(amount, currencyCode, flags);
	}

	public static String getFormattedMoneyFromAmountAndCurrencyCode(BigDecimal amount, String currencyCode) {
		return getFormattedMoneyFromAmountAndCurrencyCode(amount, currencyCode, 0);
	}

	public static String getFormattedMoneyFromAmountAndCurrencyCode(BigDecimal amount, String currencyCode, int flags) {
		if (amount != null && Strings.isNotEmpty(currencyCode)) {
			return formatRate(amount, currencyCode, flags);
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
	 * @see boolean add(Money)
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

	private static HashMap<Integer, NumberFormat> sFormats = new HashMap<>();

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

		DecimalFormat nf = (DecimalFormat) sFormats.get(key);
		if (nf == null) {
			// We use the default user locale for both of these, as it should
			// be properly set by the Android system.
			Currency currency = Currency.getInstance(currencyCode);
			nf = (DecimalFormat) NumberFormat.getCurrencyInstance();
			if (currency != null) {
				nf.setCurrency(currency);
				nf.setMaximumFractionDigits(currency.getDefaultFractionDigits());
				String formattedAmount = nf.format(amount);
				if (formattedAmount.endsWith(currency.getSymbol())) {
					nf.setNegativePrefix("-");
					nf.setNegativeSuffix(" " + currency.getSymbol());
				}
				else {
					nf.setNegativePrefix("-" + currency.getSymbol());
					nf.setNegativeSuffix("");
				}
			}

			if ((flags & F_NO_DECIMAL) != 0) {
				nf.setMaximumFractionDigits(0);
			}
			sFormats.put(key, nf);
		}

		//Handle F_ALWAYS_TWO_PLACES_AFTER_DECIMAL which trumps all other flags
		if ((flags & F_ALWAYS_TWO_PLACES_AFTER_DECIMAL) != 0) {
			nf = (DecimalFormat) nf.clone();
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
		}
		//Handle F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL flag
		else if ((flags & F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL) != 0) {
			nf = (DecimalFormat) nf.clone();
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

		//We don't want negative sign as prefix in case of no decimal for formatted amount as 0, ex: -0.37 -> -0
		if (nf.getMaximumFractionDigits() == 0 && amount.compareTo(BigDecimal.valueOf(-0.5)) >= 0) {
			nf = (DecimalFormat) nf.clone();
			nf.setNegativePrefix("");
		}

		String formattedAmount = nf.format(amount);
		// We are hardcoding this condition because for Java 7 euro sign appears after amount for IT
		// TODO: Remove this once Number Format Library starts giving us correct Currency Format for IT
		if (currencyCode.equals("EUR") && Locale.getDefault().equals(Locale.ITALY)) {
			String currencySymbol = nf.getCurrency().getSymbol();
//			currency is reversed, eg 34,00 €
			if (formattedAmount.endsWith(currencySymbol)) {
				String[] amountAndCurrencySymbol = formattedAmount.split("\\s+");
				if (amountAndCurrencySymbol.length > 1 && amountAndCurrencySymbol[1].matches(currencySymbol)) {
					boolean isNegative = false;
					if (amount.compareTo(BigDecimal.ZERO) < 0) {
						isNegative = true;
					}
					formattedAmount = amountAndCurrencySymbol[1] + " " + amountAndCurrencySymbol[0].replace("-", "");
					if (isNegative) {
						formattedAmount = "-" + formattedAmount;
					}
				}
			}
//			formatted but without a space, eg -€34,00
			else if (formattedAmount.startsWith(nf.getNegativePrefix()) && !formattedAmount.startsWith(nf.getNegativePrefix() + " ")) {
				formattedAmount = formattedAmount.replace(nf.getNegativePrefix(), nf.getNegativePrefix() + " ");
			}
		}
		return formattedAmount;
	}

	public String getCurrencySymbol() {
		return Currency.getInstance(currencyCode).getSymbol();
	}
}
