package com.expedia.bookings.test.data;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleLocale;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

import junit.framework.Assert;

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

	private Locale getDefaultLocale(String mockLanguage,String mockRegion) {
		return new Locale(mockLanguage, mockRegion, "");
	}

	private boolean comparePointOfSaleLocale(PointOfSaleLocale posLocale, String expectedLocaleIdentifier,String expectedLanguageCode) {
		return (posLocale.getLocaleIdentifier().equals(expectedLocaleIdentifier) &&
				posLocale.getLanguageCode().equals(expectedLanguageCode));
	}
}
