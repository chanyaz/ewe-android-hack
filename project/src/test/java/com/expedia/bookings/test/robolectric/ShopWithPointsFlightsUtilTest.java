package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.PointOfSaleTestConfiguration;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.utils.ShopWithPointsFlightsUtil;
import com.mobiata.android.util.SettingUtils;

@RunWith(RobolectricRunner.class)
public class ShopWithPointsFlightsUtilTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA})
	public void testIsShopWithPointsEnabled() {
		Assert.assertEquals(ShopWithPointsFlightsUtil.isShopWithPointsEnabled(getContext()), false);

		PointOfSaleTestConfiguration.configurePointOfSale(getContext(), "MockSharedData/pos_with_flight_earn_messaging_enabled.json");
		Assert.assertEquals(ShopWithPointsFlightsUtil.isShopWithPointsEnabled(getContext()), true);

		PointOfSaleTestConfiguration.configurePointOfSale(getContext(), "MockSharedData/pos_with_flight_earn_messaging_disabled.json");
		SettingUtils.save(getContext(), R.string.preference_swp_flights_earn_messaging, true);
		Assert.assertEquals(ShopWithPointsFlightsUtil.isShopWithPointsEnabled(getContext()), true);

		SettingUtils.remove(getContext(), R.string.preference_swp_flights_earn_messaging);
		Assert.assertEquals(ShopWithPointsFlightsUtil.isShopWithPointsEnabled(getContext()), false);
	}
}
