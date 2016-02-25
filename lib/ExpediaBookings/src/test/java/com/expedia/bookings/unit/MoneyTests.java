package com.expedia.bookings.unit;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;

public class MoneyTests {
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
}
