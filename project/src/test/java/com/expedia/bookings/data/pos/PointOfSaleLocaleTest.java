package com.expedia.bookings.data.pos;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.test.PointOfSaleTestConfiguration;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricRunner.class)
public class PointOfSaleLocaleTest {
	PointOfSale pos;

	@Before
	public void setup() {
		Context context = RuntimeEnvironment.application;
		PointOfSaleTestConfiguration.configurePointOfSale(context,
				"MockSharedData/pos_locale_test_config.json");
		pos = PointOfSale.getPointOfSale();
	}

	@Test
	public void verifyGetPosLocaleReturnsCorrectPosLocale() {
		// 1) exact match
		Assert.assertTrue(comparePointOfSaleLocale(pos.getPosLocale(getLocale("nl", "BE")), "nl_BE", "nl"));

		// 2) language match
		Assert.assertTrue(comparePointOfSaleLocale(pos.getPosLocale(getLocale("fr", "US")), "fr_BE", "fr"));

		// 3) no match (default locale for POS)
		Assert.assertTrue(comparePointOfSaleLocale(pos.getPosLocale(getLocale("en", "NZ")), "fr_BE", "fr"));
	}

	@Test
	public void verifyPosLocaleFields() {
		PointOfSaleLocale posLocale = pos.getPosLocale(getLocale("nl", "BE"));

		assertEquals("nl_BE", posLocale.getLocaleIdentifier());
		assertEquals("nl", posLocale.getLanguageCode());
		assertEquals(1043, posLocale.getLanguageId());
		assertEquals("nl bookingSupportURL", posLocale.getBookingSupportUrl());
		assertEquals("nl appInfoURL", posLocale.getAppInfoUrl());
		assertEquals("nl bestPriceGuaranteePolicyURL", posLocale.getBestPriceGuaranteePolicyUrl());
		assertEquals("nl createAccountMarketingText", posLocale.getMarketingText());
		assertEquals("nl flightBookingStatement", posLocale.getFlightBookingStatement());
		assertEquals("nl forgotPasswordURL", posLocale.getForgotPasswordUrl());
		assertEquals("nl hotelBookingStatement", posLocale.getHotelBookingStatement());
		assertEquals("nl packagesBookingStatement", posLocale.getPackagesBookingStatement());
		assertEquals("nl insuranceStatement", posLocale.getInsuranceStatement());
		assertEquals("nl insuranceURL", posLocale.getInsuranceUrl());
		assertEquals("nl loyaltyTermsAndConditionsURL", posLocale.getLoyaltyTermsAndConditionsUrl());
		assertEquals("nl privacyPolicyURL", posLocale.getPrivacyPolicyUrl());
		assertEquals("nl supportNumber", posLocale.getSupportNumber());
		assertEquals("nl termsAndConditionsURL", posLocale.getTermsAndConditionsUrl());
		assertEquals("nl termsOfBookingURL", posLocale.getTermsOfBookingUrl());
		assertEquals("nl websiteURL", posLocale.getWebsiteUrl());

		posLocale = pos.getPosLocale(getLocale("fr", "BE"));

		assertEquals("fr_BE", posLocale.getLocaleIdentifier());
		assertEquals("fr", posLocale.getLanguageCode());
		assertEquals(1036, posLocale.getLanguageId());
		assertEquals("fr bookingSupportURL", posLocale.getBookingSupportUrl());
		assertEquals("fr appInfoURL", posLocale.getAppInfoUrl());
		assertEquals("fr supportNumber", posLocale.getSupportNumber());
		assertEquals("fr websiteURL", posLocale.getWebsiteUrl());
		assertEquals("fr insuranceURL", posLocale.getInsuranceUrl());
		assertEquals("fr bestPriceGuaranteePolicyURL", posLocale.getBestPriceGuaranteePolicyUrl());
		assertEquals("fr hotelBookingStatement", posLocale.getHotelBookingStatement());
		assertEquals("fr flightBookingStatement", posLocale.getFlightBookingStatement());
		assertEquals("fr flightBookingStatement", posLocale.getPackagesBookingStatement());
		assertNull(posLocale.getInsuranceStatement());
		assertEquals("fr termsAndConditionsURL", posLocale.getTermsAndConditionsUrl());
		assertEquals("fr loyaltyTermsAndConditionsURL", posLocale.getLoyaltyTermsAndConditionsUrl());
		assertEquals("fr termsOfBookingURL", posLocale.getTermsOfBookingUrl());
		assertEquals("fr privacyPolicyURL", posLocale.getPrivacyPolicyUrl());
		assertEquals("fr forgotPasswordURL", posLocale.getForgotPasswordUrl());
		assertEquals("fr createAccountMarketingText", posLocale.getMarketingText());
	}

	private Locale getLocale(String mockLanguage, String mockRegion) {
		return new Locale(mockLanguage, mockRegion, "");
	}

	private boolean comparePointOfSaleLocale(PointOfSaleLocale posLocale, String expectedLocaleIdentifier, String expectedLanguageCode) {
		return (posLocale.getLocaleIdentifier().equals(expectedLocaleIdentifier) &&
				posLocale.getLanguageCode().equals(expectedLanguageCode));
	}
}
