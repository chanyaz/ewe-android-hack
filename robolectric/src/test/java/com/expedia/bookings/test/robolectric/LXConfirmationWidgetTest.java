package com.expedia.bookings.test.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowTelephonyManager;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXConfirmationWidget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class LXConfirmationWidgetTest {

	// This test hits Omniture which in turn throws NPE because of unavailability of operator information.
	@Before
	public void before() {
		TelephonyManager telephonyManager = (TelephonyManager) Robolectric.application.getSystemService(Context.TELEPHONY_SERVICE);
		ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
		shadowTelephonyManager.setNetworkOperatorName("Test Operator");
	}

	@Test
	public void testConfirmationWidgetViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Ui.getApplication(activity).defaultLXComponents();
		LXConfirmationWidget confirmationWidget = (LXConfirmationWidget) LayoutInflater.from(activity)
			.inflate(R.layout.test_lx_confirmation_widget, null);
		mockConfirmationLXState();

		ImageView confirmationImage = (ImageView) confirmationWidget.findViewById(R.id.confirmation_image_view);
		TextView title = (TextView) confirmationWidget.findViewById(R.id.title);
		TextView location = (TextView) confirmationWidget.findViewById(R.id.location);
		TextView tickets = (TextView) confirmationWidget.findViewById(R.id.tickets);
		TextView date = (TextView) confirmationWidget.findViewById(R.id.date);
		TextView email = (TextView) confirmationWidget.findViewById(R.id.email_text);
		TextView confirmation = (TextView) confirmationWidget.findViewById(R.id.confirmation_text);

		assertNotNull(confirmationImage);
		assertEquals("2-Day New York Pass", title.getText());
		assertEquals("New York, United States", location.getText());
		assertEquals("3 Adult, 1 Child", tickets.getText());
		assertEquals("Tue, Feb 24", date.getText());
		assertEquals("coolguy@expedia.com", email.getText());
		assertEquals("Itinerary #7666328719 sent to", confirmation.getText());
	}

	private void mockConfirmationLXState() {
		LXStateTestUtil.searchParamsState();
		LXStateTestUtil.selectActivityState();
		LXStateTestUtil.offerSelected();
		LXStateTestUtil.checkoutSuccessState();
	}
}
