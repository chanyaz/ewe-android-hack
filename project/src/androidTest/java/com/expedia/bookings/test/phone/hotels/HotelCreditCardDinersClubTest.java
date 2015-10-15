package com.expedia.bookings.test.phone.hotels;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.activity.HotelSearchActivity;
import com.expedia.bookings.test.phone.hotels.HotelCreditCardUtil.HotelCreditCardTestData;

import static com.expedia.bookings.R.drawable.ic_diners_club_white;

@RunWith(AndroidJUnit4.class)
public class HotelCreditCardDinersClubTest {
	@Rule
	public ActivityTestRule<HotelSearchActivity> activity = new ActivityTestRule<>(HotelSearchActivity.class);

	@Test
	public void iconTest() throws Throwable {
		HotelCreditCardTestData data = new HotelCreditCardTestData()
			.name("DinersClub")
			.prefixes("30", "36", "38", "60")
			.length(14)
			.drawableId(ic_diners_club_white);

		HotelCreditCardUtil.driveHotelCreditCardTest(activity.getActivity(), data);
	}
}
