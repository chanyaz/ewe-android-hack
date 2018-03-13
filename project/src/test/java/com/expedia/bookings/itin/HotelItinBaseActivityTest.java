package com.expedia.bookings.itin;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.itin.hotel.details.HotelItinDetailsActivity;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder;

import junit.framework.Assert;

@RunWith(RobolectricRunner.class)
public class HotelItinBaseActivityTest {

	private TestHotelItinDetailsActivity testHotelItinDetailsActivity;
	private ItinCardDataHotel itinCardDataHotel;

	@Before
	public void before() {
		testHotelItinDetailsActivity = Robolectric.buildActivity(TestHotelItinDetailsActivity.class).create().start().visible().get();
		itinCardDataHotel = (new ItinCardDataHotelBuilder()).build();
	}

	@Test
	public void testItinRefresh() {
		testHotelItinDetailsActivity.itinCardDataHotel = itinCardDataHotel;
		testHotelItinDetailsActivity.setUpWidgets();
		Assert.assertEquals(itinCardDataHotel.getProperty().getItinBedType(), testHotelItinDetailsActivity.itinCardDataHotel.getProperty().getItinBedType());

		testHotelItinDetailsActivity.getHotelBookingDetailsView().getAdditionalInfoCard().performClick();
		Shadows.shadowOf(testHotelItinDetailsActivity).receiveResult(
			new Intent(testHotelItinDetailsActivity, WebViewActivity.class),
			Activity.RESULT_OK,
			new Intent().putExtra(Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER, itinCardDataHotel.getTripNumber())
		);
		Assert.assertEquals("Room with a view", testHotelItinDetailsActivity.itinCardDataHotel.getProperty().getItinBedType());
	}

	public static class TestHotelItinDetailsActivity extends HotelItinDetailsActivity {
		private ItineraryManager mockItinManager = Mockito.mock(ItineraryManager.class);
		private TestHotelItinDetailsActivity testHotelItinDetailsActivity = this;

		public TestHotelItinDetailsActivity() {
			Mockito.when(mockItinManager.deepRefreshTrip(Mockito.anyString(), Mockito.anyBoolean()))
				.then(new Answer<Void>() {
					public Void answer(InvocationOnMock invocation) {
						testHotelItinDetailsActivity.getSyncListener().onSyncFinished(new ArrayList<Trip>());
						return null;
					}
				});
		}

		@NotNull
		@Override
		public ItineraryManager getItineraryManager() {
			return mockItinManager;
		}

		@Override
		public void updateItinCardDataHotel() {
			this.itinCardDataHotel.getProperty().setItinBedType("Room with a view");
		}

		@Override
		protected void onCreate(@Nullable Bundle savedInstanceState) {
			setTheme(R.style.ItinTheme);
			super.onCreate(savedInstanceState);
		}
	}
}
