package com.expedia.bookings.test.data;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import junit.framework.Assert;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleLocale;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

@RunWith(RobolectricRunner.class)
public class POSLocaleTest {
	PointOfSale pos;

	@Before
	public void setup() {
		Context context = RuntimeEnvironment.application;
		PointOfSale.init(context, "mockJson/point_of_sale_config.json");
		pos = PointOfSale.getPointOfSale();
	}

	@Test
	public void testPosLocale() {
		Assert.assertTrue(comparePointOfSaleLocale(pos.getPosLocale(getDefaultLocale("nl", "BE")), "nl_BE", "nl"));
		Assert.assertTrue(comparePointOfSaleLocale(pos.getPosLocale(getDefaultLocale("fr", "US")), "fr_BE", "fr"));
		Assert.assertTrue(comparePointOfSaleLocale(pos.getPosLocale(getDefaultLocale("en", "BE")), "fr_BE", "fr"));
	}

	@Test
	public void testPosUrl() {
		PointOfSaleLocale locale = pos.getPosLocale(getDefaultLocale("fr", "US"));
		locale.getAirlineFeeBasedOnPaymentMethodTermsAndConditionsURL();
		Assert.assertEquals(locale.getAirlineFeeBasedOnPaymentMethodTermsAndConditionsURL(), "https://www.expedia.be/fr-BE/p/airline-credit-card-fees.htm");
		Assert.assertEquals(locale.getTermsAndConditionsUrl(), "http://www.expedia.be/fr-BE/conditions-generales-de-reservation.aspx");
		Assert.assertEquals(locale.getPrivacyPolicyUrl(), "http://www.expedia.be/fr-BE/charte-de-confidentialite.aspx");
	}

	private Locale getDefaultLocale(String mockLanguage,String mockRegion) {
		return new Locale(mockLanguage, mockRegion, "");
	}

	private boolean comparePointOfSaleLocale(PointOfSaleLocale posLocale, String expectedLocaleIdentifier,String expectedLanguageCode) {
		return (posLocale.getLocaleIdentifier().equals(expectedLocaleIdentifier) &&
				posLocale.getLanguageCode().equals(expectedLanguageCode));
	}
}
