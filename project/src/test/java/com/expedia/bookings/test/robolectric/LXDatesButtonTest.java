package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.ApiDateUtils;
import com.expedia.bookings.widget.LXOfferDatesButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
public class LXDatesButtonTest {
	@Test
	public void testTicketSelectionWidgetViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		LayoutInflater inflater = activity.getLayoutInflater();
		LXOfferDatesButton button = (LXOfferDatesButton) inflater
			.inflate(R.layout.lx_offer_date_button, null);
		assertNotNull(button);

		LocalDate localDate = ApiDateUtils.yyyyMMddHHmmssToLocalDate("2015-03-18 08:30:00");
		String expectedText = localDate.dayOfWeek().getAsShortText() + "\n" + localDate.dayOfMonth().getAsText() + "\n";

		Gson gson = new GsonBuilder().create();
		final String rawOfferDetailsJson = "{\"offers\":[{\"id\":\"166367\",\"title\":\"8: 30AMEnglishCommentaryTour\",\"description\":\"\",\"currencySymbol\":\"$\",\"currencyDisplayedLeft\":true,\"freeCancellation\":true,\"duration\":\"10h\",\"durationInMillis\":36000000,\"discountPercentage\":null,\"directionality\":\"\",\"availabilityInfo\":[{\"availabilities\":{\"displayDate\":\"Wed,Mar18\",\"valueDate\":\"2015-03-18 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76684\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76685\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]},{\"availabilities\":{\"displayDate\":\"Wed,Apr1\",\"valueDate\":\"2015-04-01 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76684\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76685\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]}],\"direction\":null},{\"id\":\"166372\",\"title\":\"8: 30AMSpanishCommentaryTour\",\"description\":\"\",\"currencySymbol\":\"$\",\"currencyDisplayedLeft\":true,\"freeCancellation\":true,\"duration\":null,\"durationInMillis\":0,\"discountPercentage\":null,\"directionality\":\"\",\"availabilityInfo\":[{\"availabilities\":{\"displayDate\":\"Wed,Mar18\",\"valueDate\":\"2015-03-18 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76703\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76704\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]},{\"availabilities\":{\"displayDate\":\"Wed,Apr1\",\"valueDate\":\"2015-04-01 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76703\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76704\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]}],\"direction\":null}],\"priceFootnote\":\"*Taxesincluded\",\"sameDateSearch\":false}";

		button.bind(localDate, true);
		assertEquals(expectedText, button.getText().toString());
		button.setChecked(true);
		assertTrue(button.isChecked());
		assertTrue(button.isEnabled());
	}
}
