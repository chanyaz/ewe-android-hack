package com.expedia.bookings.widget;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class NewLaunchLobAdapterTest {

	private NewLaunchLobAdapter.LobInfo hotelsLob = new NewLaunchLobAdapter.LobInfo(R.string.nav_hotels, R.drawable.ic_lob_hotels);
	private NewLaunchLobAdapter.LobInfo flightsLob = new NewLaunchLobAdapter.LobInfo(R.string.flights_title, R.drawable.ic_lob_flights);
	private NewLaunchLobAdapter.LobInfo carsLob = new NewLaunchLobAdapter.LobInfo(R.string.nav_cars, R.drawable.ic_lob_cars);
	private NewLaunchLobAdapter.LobInfo lxLob = new NewLaunchLobAdapter.LobInfo(R.string.nav_lx, R.drawable.ic_lob_lx);
	private NewLaunchLobAdapter.LobInfo gtLob = new NewLaunchLobAdapter.LobInfo(R.string.nav_transport, R.drawable.ic_lob_gt);

	@Test
	public void spansAreCorrect() {
		NewLaunchLobAdapter adapter = new NewLaunchLobAdapter(null);

		ArrayList<NewLaunchLobAdapter.LobInfo> lobs = new ArrayList<>();
		lobs.add(hotelsLob);
		lobs.add(flightsLob);
		adapter.setLobs(lobs);
		assertItemCount(2, adapter);
		assertAllSpansAreOne(adapter);

		lobs.add(carsLob);
		adapter.setLobs(lobs);
		assertItemCount(3, adapter);
		assertFinalSpanIsTwoAndRestAreOne(adapter);

		lobs.add(lxLob);
		adapter.setLobs(lobs);
		assertItemCount(4, adapter);
		assertAllSpansAreOne(adapter);

		lobs.add(gtLob);
		adapter.setLobs(lobs);
		assertItemCount(5, adapter);
		assertFinalSpanIsTwoAndRestAreOne(adapter);
	}

	private void assertItemCount(int expected, NewLaunchLobAdapter adapter) {
		assertEquals(expected, adapter.getItemCount());
	}

	private void assertAllSpansAreOne(NewLaunchLobAdapter adapter) {
		for (int i = 0; i < adapter.getItemCount(); i++) {
			assertEquals(1, adapter.getSpanSize(i));
		}
	}

	private void assertFinalSpanIsTwoAndRestAreOne(NewLaunchLobAdapter adapter) {
		for (int i = 0; i < adapter.getItemCount() - 1; i++) {
			assertEquals(1, adapter.getSpanSize(i));
		}
		assertEquals(2, adapter.getSpanSize(adapter.getItemCount() - 1));
	}

	@Test
	public void correctDataIsBound() {
		View mockItemView = Mockito.mock(View.class);
		TextView mockTextView = Mockito.mock(TextView.class);
		Mockito.when(mockItemView.findViewById(R.id.lob_cell_text)).thenReturn(mockTextView);

		NewLaunchLobAdapter adapter = new NewLaunchLobAdapter(null);
		ArrayList<NewLaunchLobAdapter.LobInfo> lobs = new ArrayList<>();
		lobs.add(hotelsLob);
		lobs.add(flightsLob);
		lobs.add(carsLob);
		lobs.add(lxLob);
		adapter.setLobs(lobs);

		NewLaunchLobAdapter.LobViewHolder vh = new NewLaunchLobAdapter.LobViewHolder(mockItemView, null);
		adapter.onBindViewHolder(vh, 0);
		Mockito.verify(mockTextView).setText(hotelsLob.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(hotelsLob.iconRes, 0, 0, 0);

		adapter.onBindViewHolder(vh, 1);
		Mockito.verify(mockTextView).setText(flightsLob.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(flightsLob.iconRes, 0, 0, 0);

		adapter.onBindViewHolder(vh, 2);
		Mockito.verify(mockTextView).setText(carsLob.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(carsLob.iconRes, 0, 0, 0);

		adapter.onBindViewHolder(vh, 3);
		Mockito.verify(mockTextView).setText(lxLob.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(lxLob.iconRes, 0, 0, 0);
	}

	@Test
	public void listenerCalledOnClick() {
		View mockItemView = Mockito.mock(View.class);
		TextView mockTextView = Mockito.mock(TextView.class);
		Mockito.when(mockItemView.findViewById(R.id.lob_cell_text)).thenReturn(mockTextView);

		NewLaunchLobAdapter.OnLobClickListener mockListener = Mockito.mock(NewLaunchLobAdapter.OnLobClickListener.class);

		NewLaunchLobAdapter.LobViewHolder vh = new NewLaunchLobAdapter.LobViewHolder(mockItemView, mockListener);

		vh.bind(hotelsLob, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onHotelsLobClick();

		vh.bind(flightsLob, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onFlightsLobClick();

		vh.bind(carsLob, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onCarsLobClick();

		vh.bind(lxLob, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onActivitiesLobClick();

		vh.bind(gtLob, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onTransportLobClick();
	}
}
