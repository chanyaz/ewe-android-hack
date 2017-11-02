package com.expedia.bookings.widget;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.LobInfo;
import com.expedia.bookings.launch.vm.LaunchLobViewModel;
import com.expedia.bookings.launch.widget.LaunchLobAdapter;
import com.expedia.bookings.test.robolectric.RobolectricRunner;

import kotlin.Pair;
import kotlin.Unit;
import com.expedia.bookings.services.TestObserver;
import io.reactivex.subjects.BehaviorSubject;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class LaunchLobAdapterTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Test
	public void spansAreCorrect() {
		LaunchLobAdapter adapter = new LaunchLobAdapter(
			new LaunchLobViewModel(getContext(), BehaviorSubject.<Boolean>create(), BehaviorSubject.<Unit>create()));

		ArrayList<LobInfo> lobs = new ArrayList<>();
		lobs.add(LobInfo.HOTELS);
		lobs.add(LobInfo.FLIGHTS);
		adapter.setLobs(lobs);
		assertItemCount(2, adapter);
		assertAllSpansAreOne(adapter);

		lobs.add(LobInfo.CARS);
		adapter.setLobs(lobs);
		assertItemCount(3, adapter);
		assertFinalSpanIsTwoAndRestAreOne(adapter);

		lobs.add(LobInfo.ACTIVITIES);
		adapter.setLobs(lobs);
		assertItemCount(4, adapter);
		assertAllSpansAreOne(adapter);

		lobs.add(LobInfo.TRANSPORT);
		adapter.setLobs(lobs);
		assertItemCount(5, adapter);
		assertFinalSpanIsTwoAndRestAreOne(adapter);
	}

	private void assertItemCount(int expected, LaunchLobAdapter adapter) {
		assertEquals(expected, adapter.getItemCount());
	}

	private void assertAllSpansAreOne(LaunchLobAdapter adapter) {
		for (int i = 0; i < adapter.getItemCount(); i++) {
			assertEquals(1, adapter.getSpanSize(i));
		}
	}

	private void assertFinalSpanIsTwoAndRestAreOne(LaunchLobAdapter adapter) {
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
		Mockito.when(mockTextView.getContext()).thenReturn(getContext());

		LaunchLobViewModel launchLobViewModel = new LaunchLobViewModel(getContext(),
			BehaviorSubject.<Boolean>create(),
			BehaviorSubject.<Unit>create());
		LaunchLobAdapter adapter = new LaunchLobAdapter(
			launchLobViewModel);
		ArrayList<LobInfo> lobs = new ArrayList<>();
		lobs.add(LobInfo.HOTELS);
		lobs.add(LobInfo.FLIGHTS);
		lobs.add(LobInfo.CARS);
		lobs.add(LobInfo.ACTIVITIES);
		adapter.setLobs(lobs);

		LaunchLobAdapter.LobViewHolder vh = new LaunchLobAdapter.LobViewHolder(mockItemView,
			launchLobViewModel);
		adapter.onBindViewHolder(vh, 0);
		Mockito.verify(mockTextView).setText(LobInfo.HOTELS.getLabelRes());
		Mockito.verify(mockTextView)
			.setCompoundDrawablesWithIntrinsicBounds(
				ContextCompat.getDrawable(getContext(), LobInfo.HOTELS.getIconRes()), null, null, null);

		adapter.onBindViewHolder(vh, 1);
		Mockito.verify(mockTextView).setText(LobInfo.FLIGHTS.getLabelRes());
		adapter.onBindViewHolder(vh, 2);
		Mockito.verify(mockTextView).setText(LobInfo.CARS.getLabelRes());

		adapter.onBindViewHolder(vh, 3);
		Mockito.verify(mockTextView).setText(LobInfo.ACTIVITIES.getLabelRes());
	}

	@Test
	public void lobNavigationCalledOnClick() {
		View mockItemView = Mockito.mock(View.class);
		TextView mockTextView = Mockito.mock(TextView.class);
		Mockito.when(mockItemView.findViewById(R.id.lob_cell_text)).thenReturn(mockTextView);
		Mockito.when(mockTextView.getContext()).thenReturn(getContext());
		LaunchLobViewModel launchLobViewModel = new LaunchLobViewModel(getContext(),
			BehaviorSubject.<Boolean>create(),
			BehaviorSubject.<Unit>create());

		LaunchLobAdapter.LobViewHolder vh = new LaunchLobAdapter.LobViewHolder(mockItemView,
			launchLobViewModel);

		for (LobInfo lobInfo : LobInfo.values()) {
			TestObserver<Pair<LineOfBusiness, View>> testSubscriber = new TestObserver<>();
			vh.getViewModel().getNavigationSubject().take(1).subscribe(testSubscriber);
			vh.bind(lobInfo, false, getContext(), true);
			vh.onClick(mockItemView);
			testSubscriber.awaitTerminalEvent();
			assertEquals(lobInfo.getLineOfBusiness(), testSubscriber.values().get(0).getFirst());
		}
	}

	@Test
	public void lobNavigationNotCalledOnDisabledLOBs() {
		View mockItemView = Mockito.mock(View.class);
		TextView mockTextView = Mockito.mock(TextView.class);
		Mockito.when(mockItemView.findViewById(R.id.lob_cell_text)).thenReturn(mockTextView);
		Mockito.when(mockTextView.getContext()).thenReturn(getContext());
		LaunchLobViewModel launchLobViewModel = new LaunchLobViewModel(getContext(),
			BehaviorSubject.<Boolean>create(),
			BehaviorSubject.<Unit>create());
		LaunchLobAdapter.LobViewHolder vh = new LaunchLobAdapter.LobViewHolder(mockItemView,
			launchLobViewModel);
		for (LobInfo lobInfo : LobInfo.values()) {
			TestObserver<Pair<LineOfBusiness, View>> testSubscriber = new TestObserver<>();
			vh.getViewModel().getNavigationSubject().take(1).subscribe(testSubscriber);
			vh.bind(lobInfo, false, getContext(), false);
			vh.onClick(mockItemView);
			assertEquals(0, testSubscriber.values().size());
		}
	}
}
