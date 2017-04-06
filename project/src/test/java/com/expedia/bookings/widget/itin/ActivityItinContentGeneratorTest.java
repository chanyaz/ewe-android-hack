package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripActivity;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.widget.InfoTripletView;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class ActivityItinContentGeneratorTest {
	ActivityItinContentGenerator sut;
	Activity activity;
	Context context;

	@Before
	public void before() {
		context = getContext();
		activity = new Activity();
		sut = getItinGenerator(activity);
	}

	private Context getContext() {
		Context spyContext = Mockito.spy(RuntimeEnvironment.application);
		Resources spyResources = Mockito.spy(spyContext.getResources());
		Mockito.when(spyContext.getResources()).thenReturn(spyResources);
		return spyContext;
	}

	private ActivityItinContentGenerator getItinGenerator(Activity activity) {
		Trip parentTrip = new Trip();
		parentTrip.setStartDate(DateTime.now());
		parentTrip.setEndDate(DateTime.now().plusDays(1));
		TripActivity trip = new TripActivity();
		trip.setParentTrip(parentTrip);
		trip.setActivity(activity);
		return new ActivityItinContentGenerator(context, new ItinCardDataActivity(trip));
	}

	private View getDetails(Activity activity) {
		return sut.getDetailsView(null, new LinearLayout(context, null));
	}

	@Test
	public void testGuestCountUnavailable() {
		guestCountVisibilityCheck(0, View.GONE);
	}

	@Test
	public void testGuestCountAvailable() {
		guestCountVisibilityCheck(2, View.VISIBLE);
	}

	@Test
	public void testSupportContentDesc() {
		activity.setGuestCount(2);
		Resources resources = context.getResources();
		Mockito.doReturn(2 + "Guests").when(resources).getQuantityText(R.plurals.number_of_guests_label, 2);
		getDetails(activity);

		assertEquals("Call Support", sut.getSummaryRightButton().getContentDescription());
	}

	private void guestCountVisibilityCheck(int count, final int visibility) {
		activity.setGuestCount(count);

		Resources resources = context.getResources();
		Mockito.doReturn(count + "Guests").when(resources).getQuantityText(R.plurals.number_of_guests_label, count);

		final View detailsView = getDetails(activity);
		Assert.assertNotNull(detailsView);

		InfoTripletView tripletView = (InfoTripletView) detailsView.findViewById(R.id.info_triplet);

		Ui.runOnNextLayout(tripletView, new Runnable() {
			@Override
			public void run() {
				TextView guestLabel = (TextView) detailsView.findViewById(R.id.label3);
				TextView guestValue = (TextView) detailsView.findViewById(R.id.value3);
				Assert.assertEquals(visibility, guestValue.getVisibility());
				Assert.assertEquals(visibility, guestLabel.getVisibility());
			}
		});
	}
}
