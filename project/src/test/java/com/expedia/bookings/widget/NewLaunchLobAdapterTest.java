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

	@Test
	public void spansAreCorrect() {
		NewLaunchLobAdapter adapter = new NewLaunchLobAdapter(null);

		ArrayList<NewLaunchLobAdapter.LobInfo> lobs = new ArrayList<>();
		lobs.add(NewLaunchLobAdapter.LobInfo.HOTELS);
		lobs.add(NewLaunchLobAdapter.LobInfo.FLIGHTS);
		adapter.setLobs(lobs);
		assertItemCount(2, adapter);
		assertAllSpansAreOne(adapter);

		lobs.add(NewLaunchLobAdapter.LobInfo.CARS);
		adapter.setLobs(lobs);
		assertItemCount(3, adapter);
		assertFinalSpanIsTwoAndRestAreOne(adapter);

		lobs.add(NewLaunchLobAdapter.LobInfo.ACTIVITIES);
		adapter.setLobs(lobs);
		assertItemCount(4, adapter);
		assertAllSpansAreOne(adapter);

		lobs.add(NewLaunchLobAdapter.LobInfo.TRANSPORT);
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
		lobs.add(NewLaunchLobAdapter.LobInfo.HOTELS);
		lobs.add(NewLaunchLobAdapter.LobInfo.FLIGHTS);
		lobs.add(NewLaunchLobAdapter.LobInfo.CARS);
		lobs.add(NewLaunchLobAdapter.LobInfo.ACTIVITIES);
		adapter.setLobs(lobs);

		NewLaunchLobAdapter.LobViewHolder vh = new NewLaunchLobAdapter.LobViewHolder(mockItemView, null);
		adapter.onBindViewHolder(vh, 0);
		Mockito.verify(mockTextView).setText(NewLaunchLobAdapter.LobInfo.HOTELS.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(NewLaunchLobAdapter.LobInfo.HOTELS.iconRes, 0, 0, 0);

		adapter.onBindViewHolder(vh, 1);
		Mockito.verify(mockTextView).setText(NewLaunchLobAdapter.LobInfo.FLIGHTS.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(NewLaunchLobAdapter.LobInfo.FLIGHTS.iconRes, 0, 0, 0);

		adapter.onBindViewHolder(vh, 2);
		Mockito.verify(mockTextView).setText(NewLaunchLobAdapter.LobInfo.CARS.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(NewLaunchLobAdapter.LobInfo.CARS.iconRes, 0, 0, 0);

		adapter.onBindViewHolder(vh, 3);
		Mockito.verify(mockTextView).setText(NewLaunchLobAdapter.LobInfo.ACTIVITIES.labelRes);
		Mockito.verify(mockTextView).setCompoundDrawablesWithIntrinsicBounds(NewLaunchLobAdapter.LobInfo.ACTIVITIES.iconRes, 0, 0, 0);
	}

	@Test
	public void listenerCalledOnClick() {
		View mockItemView = Mockito.mock(View.class);
		TextView mockTextView = Mockito.mock(TextView.class);
		Mockito.when(mockItemView.findViewById(R.id.lob_cell_text)).thenReturn(mockTextView);

		NewLaunchLobAdapter.OnLobClickListener mockListener = Mockito.mock(NewLaunchLobAdapter.OnLobClickListener.class);

		NewLaunchLobAdapter.LobViewHolder vh = new NewLaunchLobAdapter.LobViewHolder(mockItemView, mockListener);

		vh.bind(NewLaunchLobAdapter.LobInfo.HOTELS, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onHotelsLobClick(mockItemView);

		vh.bind(NewLaunchLobAdapter.LobInfo.FLIGHTS, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onFlightsLobClick();

		vh.bind(NewLaunchLobAdapter.LobInfo.CARS, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onCarsLobClick();

		vh.bind(NewLaunchLobAdapter.LobInfo.ACTIVITIES, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onActivitiesLobClick();

		vh.bind(NewLaunchLobAdapter.LobInfo.TRANSPORT, false);
		vh.onClick(mockItemView);
		Mockito.verify(mockListener).onTransportLobClick();
	}
}
