package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NearbyHotelsWidget extends FrameLayout {

	private static final String PICASSO_TAG = "NEARBY_HOTELS_LIST";

	@InjectView(R.id.nearby_hotel_list)
	RecyclerView nearbyHotels;

	private NearbyHotelsListAdapter adapter;

	public NearbyHotelsWidget(Context context) {
		super(context);
	}

	public NearbyHotelsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
			StaggeredGridLayoutManager.VERTICAL);
		nearbyHotels.setLayoutManager(layoutManager);

		adapter = new NearbyHotelsListAdapter();
		nearbyHotels.setAdapter(adapter);
		nearbyHotels.setOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onNearbyHotelsSearchResults(Events.NearbyHotelSearchResults event) {
		adapter.setNearbyHotels(event.topTen);
		adapter.notifyDataSetChanged();
	}
}
