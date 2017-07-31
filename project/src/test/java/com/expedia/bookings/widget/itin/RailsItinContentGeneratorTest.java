package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataRails;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripRails;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;
import com.squareup.phrase.Phrase;

@RunWith(RobolectricRunner.class)
public class RailsItinContentGeneratorTest {

	Context context;
	DateTime start = DateTime.now();
	DateTime end = DateTime.now().plusDays(1);

	@Before
	public void before() {
		context = RuntimeEnvironment.application;
	}

	private ItinContentGenerator<?> getItinGenerator() {
		Trip parentTrip = new Trip();
		parentTrip.setTitle("Rail trip title");
		parentTrip.setStartDate(start);
		parentTrip.setEndDate(end);
		TripRails tripRails = new TripRails();
		tripRails.setParentTrip(parentTrip);
		return ItinContentGenerator.createGenerator(RuntimeEnvironment.application, new ItinCardDataRails(tripRails));
	}

	@Test
	public void testHeader() {
		ItinContentGenerator<?> itin = getItinGenerator();
		Assert.assertEquals("Rail trip title", itin.getHeaderText());
	}

	@Test
	public void testSummaryView() {
		ItinContentGenerator<?> itin = getItinGenerator();
		android.widget.TextView view = (android.widget.TextView) itin.getSummaryView(null, new LinearLayout(context, null));
		String expectedSummaryText = Phrase.from(context, R.string.itin_card_rail_summary_TEMPLATE)
			.put("datetime", LocaleBasedDateFormatUtils.dateTimeToEEEMMMdhmma(start)).format().toString();
		Assert.assertNotNull(view);
		Assert.assertEquals(expectedSummaryText, view.getText().toString());

	}

}
