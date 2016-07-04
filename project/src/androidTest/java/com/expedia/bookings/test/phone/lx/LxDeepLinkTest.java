package com.expedia.bookings.test.phone.lx;

import org.joda.time.LocalDate;

import android.content.Intent;

import com.expedia.bookings.data.Codes;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.ui.LXBaseActivity;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LxDeepLinkTest extends PhoneTestCase {
	public void testDeeplinkForLXSearch() {
		Intent intent = new Intent(super.getActivity(), LXBaseActivity.class);
		intent.putExtra("startDateStr", DateUtils.localDateToyyyyMMdd(LocalDate.now()));
		intent.putExtra("location", "San Fransisco");
		intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		super.getActivity().startActivity(intent);

		LXScreen.didNotGoToResults();
		SearchScreen.selectDestinationTextView().check(matches(withText("San Fransisco")));
	}
}
