package com.expedia.bookings.data.pos;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.PointOfSaleTestConfiguration;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class PointOfSaleTest {

	Context context = RuntimeEnvironment.application;

	User mockUser = Mockito.mock(User.class);
	UserLoyaltyMembershipInformation mockLoyaltyInfo = Mockito.mock(UserLoyaltyMembershipInformation.class);

	private final String expediaSharedFilePath = "ExpediaSharedData/ExpediaPointOfSaleConfig.json";

	@Before
	public void setup() {
		Mockito.when(mockUser.getLoyaltyMembershipInformation()).thenReturn(mockLoyaltyInfo);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void checkSupportPhoneNumbersMiddleTier() {
		Mockito.when(mockLoyaltyInfo.isLoyaltyMembershipActive()).thenReturn(true);
		Mockito.when(mockLoyaltyInfo.getLoyaltyMembershipTier()).thenReturn(LoyaltyMembershipTier.MIDDLE);

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
		Mockito.when(mockLoyaltyInfo.isLoyaltyMembershipActive()).thenReturn(true);
		Mockito.when(mockLoyaltyInfo.getLoyaltyMembershipTier()).thenReturn(LoyaltyMembershipTier.TOP);

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
		Mockito.when(mockLoyaltyInfo.isLoyaltyMembershipActive()).thenReturn(false);

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
	public void checkSupportPhoneNumbersBaseTier() {
		Mockito.when(mockLoyaltyInfo.isLoyaltyMembershipActive()).thenReturn(true);
		Mockito.when(mockLoyaltyInfo.getLoyaltyMembershipTier()).thenReturn(LoyaltyMembershipTier.BASE);

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

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void checkVipAccessEnabledLocales() {
		// https://eiwork.mingle.thoughtworks.com/projects/eb_ad_app/cards/8757
		assertVipAccessForPOSKey(PointOfSaleId.ARGENTINA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.AUSTRIA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.AUSTRALIA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.BELGIUM.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.BRAZIL.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.CANADA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.DENMARK.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.SPAIN.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.FRANCE.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.HONG_KONG.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.INDONESIA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.IRELAND.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.INDIA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.JAPAN.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.SOUTH_KOREA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.MEXICO.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.MALAYSIA.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.NETHERLANDS.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.NORWAY.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.NEW_ZEALND.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.PHILIPPINES.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.SWEDEN.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.SINGAPORE.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.THAILAND.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.TAIWAN.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.UNITED_KINGDOM.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.UNITED_STATES.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.VIETNAM.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.SWITZERLAND.getId(), true);
		assertVipAccessForPOSKey(PointOfSaleId.FINLAND.getId(), true);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void checkVipAccessDisabledLocales() {
		assertVipAccessForPOSKey(PointOfSaleId.ITALY.getId(), false);
		assertVipAccessForPOSKey(PointOfSaleId.GERMANY.getId(), false);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void checkEarnMessageEnabledExpediaLocales() {
		assertHotelEarnMessageForPOSKey(PointOfSaleId.ARGENTINA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.AUSTRIA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.AUSTRALIA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.BELGIUM.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.BRAZIL.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.CANADA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.GERMANY.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.DENMARK.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.SPAIN.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.FRANCE.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.HONG_KONG.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.INDONESIA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.IRELAND.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.INDIA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.ITALY.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.JAPAN.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.SOUTH_KOREA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.MEXICO.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.MALAYSIA.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.NETHERLANDS.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.NORWAY.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.NEW_ZEALND.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.PHILIPPINES.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.SWEDEN.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.SINGAPORE.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.THAILAND.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.TAIWAN.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.UNITED_KINGDOM.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.UNITED_STATES.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.VIETNAM.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.SWITZERLAND.getId(), true);
		assertHotelEarnMessageForPOSKey(PointOfSaleId.FINLAND.getId(), true);
	}

	private void assertVipAccessForPOSKey(int posKey, boolean enabled) {
		PointOfSaleTestConfiguration.configurePOS(context, expediaSharedFilePath, Integer.toString(posKey), false);
		PointOfSale pos = PointOfSale.getPointOfSale();
		assertEquals(enabled, pos.supportsVipAccess());
	}

	private void assertHotelEarnMessageForPOSKey(int posKey, boolean enabled) {
		PointOfSaleTestConfiguration.configurePOS(context, expediaSharedFilePath, Integer.toString(posKey), false);
		PointOfSale pos = PointOfSale.getPointOfSale();
		assertEquals(enabled, pos.isEarnMessageEnabledForHotels());
	}
}
