package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.LXOfferDatesButton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class LXDatesButtonTest {
	@Test
	public void testTicketSelectionWidgetViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		LayoutInflater inflater = activity.getLayoutInflater();
		LXOfferDatesButton button = (LXOfferDatesButton) inflater
			.inflate(R.layout.lx_offer_date_button, null);
		assertNotNull(button);

		LocalDate now = LocalDate.now();
		String expextedText = now.dayOfWeek().getAsShortText() + "\n" + now.dayOfMonth().getAsText();
		button.bind(now);
		assertEquals(expextedText, button.getText().toString());
		button.performClick();
		assertTrue(button.isChecked());
	}
}
