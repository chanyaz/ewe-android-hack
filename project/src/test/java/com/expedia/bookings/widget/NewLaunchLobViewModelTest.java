package com.expedia.bookings.widget;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.NavigationHelper;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class NewLaunchLobViewModelTest {

	Context mockContext;
	Resources mockResources;
	NavigationHelper mockNavigationHelper;
	View mockView;
	RecyclerView mockRecycler;
	NewLaunchLobAdapter mockAdapter;
	ArgumentCaptor<List> lobArgumentCaptor;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		mockNavigationHelper = Mockito.mock(NavigationHelper.class);

		mockContext = Mockito.mock(Context.class);
		mockResources = Mockito.mock(Resources.class);
		Mockito.when(mockContext.getResources()).thenReturn(mockResources);
		Mockito.when(mockResources.getColor(Mockito.anyInt())).thenReturn(0);
		Mockito.when(mockResources.getDimension(Mockito.anyInt())).thenReturn(1f);

		mockView = Mockito.mock(View.class);
		mockRecycler = Mockito.mock(RecyclerView.class);
		Mockito.when(mockView.findViewById(R.id.lob_grid_recycler)).thenReturn(mockRecycler);
		Mockito.doNothing().when(mockRecycler).setAdapter(Mockito.any(RecyclerView.Adapter.class));
		Mockito.doNothing().when(mockRecycler).addItemDecoration(Mockito.any(RecyclerView.ItemDecoration.class));
		Mockito.doNothing().when(mockRecycler).setLayoutManager(Mockito.any(RecyclerView.LayoutManager.class));

		mockAdapter = Mockito.mock(NewLaunchLobAdapter.class);
		lobArgumentCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.doNothing().when(mockAdapter).setLobs(lobArgumentCaptor.capture());
	}

	@Test
	public void pointOfSaleDeterminesLobsAvailable() {
		NewLaunchLobViewModel vm = new NewLaunchLobViewModel(mockContext, mockNavigationHelper);
		vm.adapter = mockAdapter;

		checkPointOfSaleLobs(vm, LineOfBusiness.HOTELS, LineOfBusiness.FLIGHTS);
		checkPointOfSaleLobs(vm, LineOfBusiness.HOTELS, LineOfBusiness.FLIGHTS, LineOfBusiness.CARS);
		checkPointOfSaleLobs(vm, LineOfBusiness.HOTELS, LineOfBusiness.FLIGHTS, LineOfBusiness.CARS, LineOfBusiness.LX);
		checkPointOfSaleLobs(vm, LineOfBusiness.HOTELS, LineOfBusiness.FLIGHTS, LineOfBusiness.CARS, LineOfBusiness.LX,
				LineOfBusiness.TRANSPORT);
	}

	private void checkPointOfSaleLobs(NewLaunchLobViewModel vm, LineOfBusiness... availableLobs) {
		PointOfSale pos = Mockito.mock(PointOfSale.class);
		Mockito.when(pos.supports(Mockito.any(LineOfBusiness.class))).thenReturn(false);
		for (LineOfBusiness lob : availableLobs) {
			Mockito.when(pos.supports(lob)).thenReturn(true);
		}

		vm.bind(mockView, pos);

		@SuppressWarnings("unchecked")
		List<NewLaunchLobAdapter.LobInfo> infos = lobArgumentCaptor.getValue();
		assertEquals(availableLobs.length, infos.size());
		for (int i = 0; i < availableLobs.length; i++) {
			int labelRes = infos.get(i).labelRes;
			switch (availableLobs[i]) {
			case HOTELS:
				assertEquals(R.string.nav_hotels, labelRes);
				break;
			case FLIGHTS:
				assertEquals(R.string.flights_title, labelRes);
				break;
			case CARS:
				assertEquals(R.string.nav_cars, labelRes);
				break;
			case LX:
				assertEquals(R.string.nav_lx, labelRes);
				break;
			case TRANSPORT:
				assertEquals(R.string.nav_transport, labelRes);
				break;
			}
		}
	}

	@Test
	public void clickLobButtonOpensLob() {
		NewLaunchLobViewModel vm = new NewLaunchLobViewModel(mockContext, mockNavigationHelper);

		vm.onHotelsLobClick();
		Mockito.verify(mockNavigationHelper).goToHotels(Mockito.any(Bundle.class));

		vm.onFlightsLobClick();
		Mockito.verify(mockNavigationHelper).goToFlights(Mockito.any(Bundle.class));

		vm.onCarsLobClick();
		Mockito.verify(mockNavigationHelper).goToCars(Mockito.any(Bundle.class));

		vm.onActivitiesLobClick();
		Mockito.verify(mockNavigationHelper).goToActivities(Mockito.any(Bundle.class));

		vm.onTransportLobClick();
		Mockito.verify(mockNavigationHelper).goToTransport(Mockito.any(Bundle.class));
	}
}
