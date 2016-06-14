package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.NavigationHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewLaunchLobViewModel implements NewLaunchLobAdapter.OnLobClickListener {

	@InjectView(R.id.lob_grid_recycler)
	RecyclerView gridRecycler;

	NewLaunchLobAdapter adapter;
	GridLayoutManager layoutManager;
	GridLinesItemDecoration itemDecoration;

	NavigationHelper nav;

	public NewLaunchLobViewModel(Context context, NavigationHelper navigationHelper) {
		nav = navigationHelper;

		adapter = new NewLaunchLobAdapter(this);

		layoutManager = new GridLayoutManager(context, 2);
		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return adapter.getSpanSize(position);
			}
		});

		itemDecoration = new GridLinesItemDecoration(
			ContextCompat.getColor(context, R.color.new_launch_lob_divider_color),
			context.getResources().getDimension(R.dimen.new_launch_lob_divider_stroke_width));
	}


	public void bind(View view, PointOfSale pos) {
		ButterKnife.inject(this, view);

		gridRecycler.setAdapter(adapter);
		gridRecycler.setLayoutManager(layoutManager);
		gridRecycler.addItemDecoration(itemDecoration);

		setupLinesOfBusiness(pos);
	}

	private void setupLinesOfBusiness(PointOfSale pos) {
		ArrayList<NewLaunchLobAdapter.LobInfo> lobs = new ArrayList<>();
		lobs.add(NewLaunchLobAdapter.LobInfo.HOTELS);
		lobs.add(NewLaunchLobAdapter.LobInfo.FLIGHTS);

		if (pos.supports(LineOfBusiness.CARS)) {
			lobs.add(NewLaunchLobAdapter.LobInfo.CARS);
		}

		if (pos.supports(LineOfBusiness.LX)) {
			lobs.add(NewLaunchLobAdapter.LobInfo.ACTIVITIES);
		}

		if (pos.supports(LineOfBusiness.TRANSPORT)) {
			lobs.add(NewLaunchLobAdapter.LobInfo.TRANSPORT);
		}

		adapter.setLobs(lobs);
	}

	@Override
	public void onHotelsLobClick(View view) {
		Bundle animOptions = AnimUtils.createActivityScaleBundle(view);
		nav.goToHotels(animOptions);
		trackLobNavigation(LineOfBusiness.HOTELS);
	}

	@Override
	public void onFlightsLobClick() {
		nav.goToFlights(null);
		trackLobNavigation(LineOfBusiness.FLIGHTS);
	}

	@Override
	public void onCarsLobClick() {
		nav.goToCars(null);
		trackLobNavigation(LineOfBusiness.CARS);
	}

	@Override
	public void onActivitiesLobClick() {
		nav.goToActivities(null);
		trackLobNavigation(LineOfBusiness.LX);
	}

	@Override
	public void onTransportLobClick() {
		nav.goToTransport(null);
		trackLobNavigation(LineOfBusiness.TRANSPORT);
	}

	private void trackLobNavigation(LineOfBusiness lineOfBusiness) {
		OmnitureTracking.trackNewLaunchScreenLobNavigation(lineOfBusiness);
	}

	public void onHasInternetConnectionChange(boolean enabled) {
		adapter.enableLobs(enabled);
	}
}
