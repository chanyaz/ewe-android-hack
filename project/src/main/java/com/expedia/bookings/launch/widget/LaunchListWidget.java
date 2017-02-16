package com.expedia.bookings.launch.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

public class LaunchListWidget extends RecyclerView {

	private static final String PICASSO_TAG = "LAUNCH_LIST";

	private LaunchListAdapter adapter;

	private View header;
	boolean showLobHeader = false;

	public LaunchListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LaunchListWidget);
		showLobHeader = typedArray.getBoolean(R.styleable.LaunchListWidget_show_lob_in_header, false);
		typedArray.recycle();
	}

	public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		StaggeredGridLayoutManager layoutManager = makeLayoutManager();
		setLayoutManager(layoutManager);

		header = LayoutInflater.from(getContext()).inflate(R.layout.snippet_launch_list_header, null);
		adapter = new LaunchListAdapter(getContext(), header, showLobHeader);
		setAdapter(adapter);
		addItemDecoration(new LaunchListDividerDecoration(getContext()));
		addOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));
	}

	@NonNull
	public StaggeredGridLayoutManager makeLayoutManager() {
		return new StaggeredGridLayoutManager(2,
				StaggeredGridLayoutManager.VERTICAL);
	}

	public void setHeaderPaddingTop(float paddingTop) {
		int left = header.getPaddingLeft();
		int top = (int) paddingTop;
		int right = header.getPaddingRight();
		int bottom = header.getPaddingBottom();
		header.setPadding(left, top, right, bottom);
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
	public void onNearbyHotelsSearchResults(Events.LaunchHotelSearchResponse event) {
		String headerTitle = getResources().getString(R.string.nearby_deals_title);
		Db.setLaunchListHotelData(event.topHotels);
		adapter.setListData(event.topHotels, headerTitle);
		adapter.notifyDataSetChanged();
	}

	@Subscribe
	public void onCollectionDownloadComplete(Events.CollectionDownloadComplete event) {
		String headerTitle = event.collection.title;
		if (ProductFlavorFeatureConfiguration.getInstance().getCollectionCount() != 0
			&& ProductFlavorFeatureConfiguration.getInstance().getCollectionCount() < event.collection.locations
			.size()) {
			event.collection.locations = event.collection.locations
				.subList(0, ProductFlavorFeatureConfiguration.getInstance().getCollectionCount());
		}
		adapter.setListData(event.collection.locations, headerTitle);
		adapter.notifyDataSetChanged();
	}

	public void showListLoadingAnimation() {
		List<Integer> elements = createDummyListForAnimation();
		String headerTitle = getResources().getString(R.string.loading_header);
		adapter.setListData(elements, headerTitle);
		adapter.notifyDataSetChanged();
	}

	// Create list to show cards for loading animation
	public List<Integer> createDummyListForAnimation() {
		List<Integer> elements = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			elements.add(0);
		}
		return elements;
	}

	public void onPOSChange() {
		adapter.onPOSChange();
		smoothScrollToPosition(0);
	}

	public void onHasInternetConnectionChange(boolean enabled) {
		adapter.onHasInternetConnectionChange(enabled);
	}
}
