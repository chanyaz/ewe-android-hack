package com.expedia.bookings.test.data;

import org.junit.Test;

import com.expedia.bookings.data.LoyaltyMembershipTier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LoyaltyMembershipTierTest {

	@Test
	public void testIsMidOrTopTier() throws Exception {
		assertFalse(LoyaltyMembershipTier.BASE.isMidOrTopTier());
		assertTrue(LoyaltyMembershipTier.MIDDLE.isMidOrTopTier());
		assertTrue(LoyaltyMembershipTier.TOP.isMidOrTopTier());
	}

	@Test
	public void testFromApiValue() throws Exception {
		assertEquals(LoyaltyMembershipTier.NONE, LoyaltyMembershipTier.fromApiValue(null));
		assertEquals(LoyaltyMembershipTier.NONE, LoyaltyMembershipTier.fromApiValue(""));
		assertEquals(LoyaltyMembershipTier.NONE, LoyaltyMembershipTier.fromApiValue("gibberish"));
		assertEquals(LoyaltyMembershipTier.BASE, LoyaltyMembershipTier.fromApiValue("blue"));
		assertEquals(LoyaltyMembershipTier.MIDDLE, LoyaltyMembershipTier.fromApiValue("silver"));
		assertEquals(LoyaltyMembershipTier.TOP, LoyaltyMembershipTier.fromApiValue("gold"));
		assertEquals(LoyaltyMembershipTier.NONE, LoyaltyMembershipTier.fromApiValue("platinum"));
	}

	@Test
	public void testToApiValue() throws Exception {
		assertNull(LoyaltyMembershipTier.NONE.toApiValue());
		assertEquals("BLUE", LoyaltyMembershipTier.BASE.toApiValue());
		assertEquals("SILVER", LoyaltyMembershipTier.MIDDLE.toApiValue());
		assertEquals("GOLD", LoyaltyMembershipTier.TOP.toApiValue());
	}
}
