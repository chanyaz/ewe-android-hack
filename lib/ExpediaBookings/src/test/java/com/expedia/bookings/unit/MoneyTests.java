package com.expedia.bookings.unit;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;

public class MoneyTests {
	private static final String EURO = "\u20ac";

	@Test
	public void testFlagNoDecimalIfIntegerElseTwoPlacesAfterDecimal() {
		Money money = new Money("23.20", "USD");
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL | Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));

		money = new Money("23.00", "USD");
		Assert.assertEquals("$23", money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));
		Assert.assertEquals("$23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));
		Assert.assertEquals("$23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL | Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));
	}

	@Test
	public void testFlagAlwaysTwoPlacesAfterDecimal() {
		Money money = new Money("23", "USD");
		Assert.assertEquals("$23.00", money.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));
		Assert.assertEquals("$23.00", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));

		money = new Money("23.2", "USD");
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));

		money = new Money("23.66", "USD");
		Assert.assertEquals("$23.66", money.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));
		Assert.assertEquals("$23.66", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL));
	}

	@Test
	public void testFlagRoundHalfUp() {
		Money money = new Money("23.20", "USD");
		Assert.assertEquals("$23.00", money.getFormattedMoney(Money.F_ROUND_HALF_UP));
		Assert.assertEquals("$23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL | Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));

		money = new Money("23.67", "USD");
		Assert.assertEquals("$24.00", money.getFormattedMoney(Money.F_ROUND_HALF_UP));
		Assert.assertEquals("$24", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
		Assert.assertEquals("$23.67", money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL | Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@Test
	public void testPositiveAmountWithCurrencyAsPrefix() {
		Money money = new Money("23", "USD");
		Assert.assertEquals("$23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@Test
	public void testNegativeAmountWithCurrencyAsPrefix() {
		Money money = new Money("-23", "USD");
		Assert.assertEquals("-$23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@Test
	public void testPositiveAmountWithCurrencyAsPostfix() {
		setLocale("fr", "FR");
		Money money = new Money("23", "EUR");
		Assert.assertEquals("23 " + EURO, money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@Test
	public void testNegativeAmountWithCurrencyAsPostfix() {
		setLocale("fr", "FR");
		Money money = new Money("-23", "EUR");
		Assert.assertEquals("-23 " + EURO, money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@Test
	public void testPositiveAmountForItaly() {
		setLocale("it", "IT");
		Money money = new Money("23", "EUR");
		Assert.assertEquals(EURO + " 23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@Test
	public void testNegativeAmountForItaly() {
		setLocale("it", "IT");
		Money money = new Money("-23", "EUR");
		Assert.assertEquals("-" + EURO + " 23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@Test
	public void testForNegationOfNegativePrefix() {
		Money money = new Money("-0.45", "USD");
		Assert.assertEquals("$0", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));

		money = new Money("-0.49", "USD");
		Assert.assertEquals("$0", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));

		money = new Money("-0.5", "USD");
		Assert.assertEquals("-$1", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));

		money = new Money("-0.7", "USD");
		Assert.assertEquals("-$1", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
	}

	@After
	public void resetLocale() {
		setLocale("en", "US");
	}

	private void setLocale(String lang, String region) {
		Locale.setDefault(new Locale(lang, region));
	}
}
