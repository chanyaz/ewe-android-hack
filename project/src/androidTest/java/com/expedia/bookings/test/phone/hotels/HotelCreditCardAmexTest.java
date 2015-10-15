package com.expedia.bookings.test.phone.hotels;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.activity.HotelSearchActivity;

import static com.expedia.bookings.R.drawable.ic_amex_white;

@RunWith(AndroidJUnit4.class)
public class HotelCreditCardAmexTest {
	@Rule
	public ActivityTestRule<HotelSearchActivity> activity = new ActivityTestRule<>(HotelSearchActivity.class);

	@Test
	public void iconTest() throws Throwable {
		HotelCreditCardUtil.HotelCreditCardTestData data = new HotelCreditCardUtil.HotelCreditCardTestData()
			.name("Amex")
			.prefixes("34", "37")
			.length(15)
			.drawableId(ic_amex_white);

		HotelCreditCardUtil.driveHotelCreditCardTest(activity.getActivity(), data);
	}
}
