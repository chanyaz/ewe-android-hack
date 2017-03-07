package com.expedia.bookings.test.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowTelephonyManager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.utils.Ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricRunner.class)
public class LXConfirmationWidgetTest {

	// This test hits Omniture which in turn throws NPE because of unavailability of operator information.
	@Before
	public void before() {
		TelephonyManager telephonyManager = (TelephonyManager) RuntimeEnvironment.application.getSystemService(
			Context.TELEPHONY_SERVICE);
		ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
		shadowTelephonyManager.setNetworkOperatorName("Test Operator");
	}

	public static class TestActivity extends Activity {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Ui.getApplication(this).defaultLXComponents();
			setContentView(R.layout.test_lx_confirmation_widget);
		}
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
		MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS})
	public void testConfirmationWidgetViews() {
		Activity activity = Robolectric.buildActivity(TestActivity.class).create().start().resume().visible().get();
		mockConfirmationLXState();

		ImageView confirmationImage = (ImageView) activity.findViewById(R.id.confirmation_image_view);
		TextView title = (TextView) activity.findViewById(R.id.title);
		TextView location = (TextView) activity.findViewById(R.id.location);
		TextView tickets = (TextView) activity.findViewById(R.id.tickets);
		TextView date = (TextView) activity.findViewById(R.id.date);
		TextView email = (TextView) activity.findViewById(R.id.email_text);
		TextView confirmation = (TextView) activity.findViewById(R.id.confirmation_text);
		TextView itinNumber = (TextView) activity.findViewById(R.id.itin_number);
		TextView reservationConfirmation = (TextView) activity.findViewById(R.id.reservation_confirmation_text);

		String expectedConfirmationText = activity.getResources().getString(R.string.lx_successful_checkout_email_label);
		String reservationConfirmationText = activity.getResources().getString(
			R.string.lx_successful_checkout_reservation_label);

		assertNotNull(confirmationImage);
		assertEquals("New York Pass: Visit up to 80 Attractions, Museums & Tours", title.getText());
		assertEquals("New York, United States", location.getText());
		assertEquals("3 Adults, 1 Child", tickets.getText());
		assertEquals("Tue, Feb 24", date.getText());
		assertEquals("coolguy@expedia.com", email.getText());
		assertEquals("Itinerary #7666328719", itinNumber.getText());
		assertEquals(expectedConfirmationText, confirmation.getText());
		assertEquals(reservationConfirmationText, reservationConfirmation.getText());
	}

	private void mockConfirmationLXState() {
		LXStateTestUtil.searchParamsState();
		LXStateTestUtil.selectActivityState();
		LXStateTestUtil.offerSelected();
		LXStateTestUtil.checkoutSuccessState();
	}
}
