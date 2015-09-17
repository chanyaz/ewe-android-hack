package com.expedia.bookings.test.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXCheckoutSummaryWidget;
import com.expedia.bookings.widget.TextView;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class LXCheckoutSummaryWidgetTest {
	@Test
	public void testCheckoutSummaryViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Ui.getApplication(activity).defaultLXComponents();
		LXCheckoutSummaryWidget checkoutSummaryWidget = (LXCheckoutSummaryWidget) LayoutInflater.from(activity)
			.inflate(R.layout.lx_checkout_summary_widget, null);
		LXStateTestUtil.selectActivityState();
		LXStateTestUtil.offerSelected();
		checkoutSummaryWidget.bind();

		TextView location = (TextView) checkoutSummaryWidget.findViewById(R.id.lx_offer_location);
		TextView groupText = (TextView) checkoutSummaryWidget.findViewById(R.id.lx_group_text);
		TextView date = (TextView) checkoutSummaryWidget.findViewById(R.id.lx_offer_date);
		TextView freeCancellation = (TextView) checkoutSummaryWidget.findViewById(R.id.free_cancellation_text);
		TextView tripTotal = (TextView) checkoutSummaryWidget.findViewById(R.id.price_text);

		assertEquals("New York, United States", location.getText());
		assertEquals("2015-02-24 07:30:00", date.getText());
		assertEquals("3 Adult, 1 Child", groupText.getText());
		assertEquals("$500", tripTotal.getText());
		assertEquals(View.VISIBLE, freeCancellation.getVisibility());
	}
}
