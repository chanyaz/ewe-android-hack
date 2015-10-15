package com.expedia.bookings.test.phone.hotels;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.activity.HotelSearchActivity;

import static com.expedia.bookings.R.drawable.ic_master_card_white;

@RunWith(AndroidJUnit4.class)
public class HotelCreditCardMastercardTest {
	@Rule
	public ActivityTestRule<HotelSearchActivity> activity = new ActivityTestRule<>(HotelSearchActivity.class);

	@Test
	public void iconTest() throws Throwable {
		HotelCreditCardUtil.HotelCreditCardTestData data = new HotelCreditCardUtil.HotelCreditCardTestData()
			.name("MasterCard")
			.prefixes("51", "52", "53", "54", "55")
			.length(16)
			.drawableId(ic_master_card_white);

		HotelCreditCardUtil.driveHotelCreditCardTest(activity.getActivity(), data);
	}
}
