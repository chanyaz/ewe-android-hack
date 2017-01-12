package com.expedia.bookings.data.pos;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.PointOfSaleTestConfiguration;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class PointOfSaleTest {

	Context context = RuntimeEnvironment.application;

	User mockUser = Mockito.mock(User.class);
	Traveler mockTraveler = Mockito.mock(Traveler.class);

	@Before
	public void setup() {
		Mockito.when(mockUser.getPrimaryTraveler()).thenReturn(mockTraveler);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void checkSupportPhoneNumbersMiddleTier() {
		Mockito.when(mockTraveler.getIsLoyaltyMembershipActive()).thenReturn(true);
		Mockito.when(mockTraveler.getLoyaltyMembershipTier()).thenReturn(LoyaltyMembershipTier.MIDDLE);

		// Phone
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", false);
		PointOfSale pos = PointOfSale.getPointOfSale();
		assertEquals("1-877-240-3302", pos.getSupportPhoneNumberMiddleTier());
		assertEquals("1-877-240-3302", pos.getSupportPhoneNumberBestForUser(mockUser));

		// Tablet
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", true);
		pos = PointOfSale.getPointOfSale();
		assertEquals("1-866-573-4664", pos.getSupportPhoneNumberMiddleTier());
		assertEquals("1-866-573-4664", pos.getSupportPhoneNumberBestForUser(mockUser));
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void checkSupportPhoneNumbersTopTier() {
		Mockito.when(mockTraveler.getIsLoyaltyMembershipActive()).thenReturn(true);
		Mockito.when(mockTraveler.getLoyaltyMembershipTier()).thenReturn(LoyaltyMembershipTier.TOP);

		// Phone
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", false);
		PointOfSale pos = PointOfSale.getPointOfSale();
		assertEquals("1-866-925-2524", pos.getSupportPhoneNumberTopTier());
		assertEquals("1-866-925-2524", pos.getSupportPhoneNumberBestForUser(mockUser));

		// Tablet
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", true);
		pos = PointOfSale.getPointOfSale();
		assertEquals("1-800-215-0967", pos.getSupportPhoneNumberTopTier());
		assertEquals("1-800-215-0967", pos.getSupportPhoneNumberBestForUser(mockUser));
	}

	@Test
	public void checkSupportPhoneNumbersGuest() {
		Mockito.when(mockTraveler.getIsLoyaltyMembershipActive()).thenReturn(false);

		// Phone
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", false);
		PointOfSale pos = PointOfSale.getPointOfSale();
		assertEquals("1-877-222-6503", pos.getSupportPhoneNumberBestForUser(mockUser));

		// Tablet
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", true);
		pos = PointOfSale.getPointOfSale();
		assertEquals("1-877-222-6503", pos.getSupportPhoneNumberBestForUser(mockUser));
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void checkSupportPhoneNumbersBlue() {
		Mockito.when(mockTraveler.getIsLoyaltyMembershipActive()).thenReturn(true);
		Mockito.when(mockTraveler.getLoyaltyMembershipTier()).thenReturn(LoyaltyMembershipTier.BASE);

		// Phone
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", false);
		PointOfSale pos = PointOfSale.getPointOfSale();
		assertEquals("1-877-222-6503", pos.getDefaultSupportPhoneNumber());
		assertEquals("1-877-222-6503", pos.getSupportPhoneNumberBestForUser(mockUser));

		// Tablet
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", true);
		pos = PointOfSale.getPointOfSale();
		assertEquals("1-877-222-6503", pos.getDefaultSupportPhoneNumber());
		assertEquals("1-877-222-6503", pos.getSupportPhoneNumberBestForUser(mockUser));
	}
}
