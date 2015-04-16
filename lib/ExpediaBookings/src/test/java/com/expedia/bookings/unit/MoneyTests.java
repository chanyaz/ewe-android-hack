package com.expedia.bookings.unit;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;

public class MoneyTests {
	@Test
	public void testFlagNoDecimalIfIntegerElseTwoPlacesAfterDecimal() {
		Money money = new Money("23.20", "USD");
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));

		money = new Money("23.20", "USD");
		Assert.assertEquals("$23.20", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));

		money = new Money("23.00", "USD");
		Assert.assertEquals("$23", money.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));

		money = new Money("23.00", "USD");
		Assert.assertEquals("$23", money.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL));
	}
}
