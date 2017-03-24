package com.expedia.bookings.test.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LXCheckoutSummaryWidget;
import com.expedia.bookings.widget.TextView;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class LXCheckoutSummaryWidgetTest {
	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY })
	public void testCheckoutSummaryViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		Ui.getApplication(activity).defaultLXComponents();
		LXCheckoutSummaryWidget checkoutSummaryWidget = (LXCheckoutSummaryWidget) LayoutInflater.from(activity)
			.inflate(R.layout.lx_checkout_summary_widget, null);
		LXStateTestUtil.selectActivityState();
		LXStateTestUtil.offerSelected();
		Money price = new Money("500", "USD");
		checkoutSummaryWidget.bind(price, price, null);

		TextView location = (TextView) checkoutSummaryWidget.findViewById(R.id.lx_offer_location);
		TextView groupText = (TextView) checkoutSummaryWidget.findViewById(R.id.lx_group_text);
		TextView date = (TextView) checkoutSummaryWidget.findViewById(R.id.lx_offer_date);
		TextView freeCancellation = (TextView) checkoutSummaryWidget.findViewById(R.id.free_cancellation_text);
		TextView tripTotal = (TextView) checkoutSummaryWidget.findViewById(R.id.price_text);

		assertEquals("New York, United States", location.getText());
		assertEquals("Tue, Feb 24", date.getText());
		assertEquals("3 Adults, 1 Child", groupText.getText());
		assertEquals("$500", tripTotal.getText());
		assertEquals("$500. Cost summary information button.", tripTotal.getContentDescription());
		assertEquals(View.VISIBLE, freeCancellation.getVisibility());
	}
}
