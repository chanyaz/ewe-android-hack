package com.expedia.bookings.launch.widget;

import javax.inject.Inject;

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
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.model.UserLoginStateChangedModel;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observer;

public class LaunchListWidget extends RecyclerView {

	private static final String PICASSO_TAG = "LAUNCH_LIST";

	private LaunchListAdapter adapter;

	private View header;
	boolean showLobHeader = false;

	private final ItineraryManager.ItinerarySyncAdapter itinerarySyncListener = new ItineraryManager.ItinerarySyncAdapter() {
		@Override
		public void onSyncFinished(Collection<Trip> trips) {
			notifyDataSetChanged();
		}
	};

	@Inject
	UserLoginStateChangedModel userLoginStateChangedModel;

	public LaunchListWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LaunchListWidget);
		showLobHeader = typedArray.getBoolean(R.styleable.LaunchListWidget_show_lob_in_header, false);
		typedArray.recycle();
	}

	public void visibilityChanged(boolean visible) {
		if (visible) {
			ItineraryManager.getInstance().addSyncListener(itinerarySyncListener);
			notifyDataSetChanged();
		}
		else {
			ItineraryManager.getInstance().removeSyncListener(itinerarySyncListener);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).appComponent().inject(this);

		StaggeredGridLayoutManager layoutManager = makeLayoutManager();
		setLayoutManager(layoutManager);

		header = LayoutInflater.from(getContext()).inflate(R.layout.snippet_launch_list_header, null);
		adapter = new LaunchListAdapter(getContext(), header);
		setAdapter(adapter);
		addItemDecoration(new LaunchListDividerDecoration(getContext()));
		addOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));

		ItineraryManager.getInstance().addSyncListener(itinerarySyncListener);

		userLoginStateChangedModel.getUserLoginStateChanged().debounce(200, TimeUnit.MILLISECONDS)
			.delay(3, TimeUnit.SECONDS)
			.subscribe(new Observer<Boolean>() {

				@Override
				public void onCompleted() {
				}

				@Override
				public void onError(Throwable e) {
				}

				@Override
				public void onNext(Boolean signedIn) {
					if (signedIn) {
						ItineraryManager.getInstance().startSync(false, false, true);
					}
				}
			});
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
		List<LaunchDataItem> hotelDataItemList = new ArrayList<>();
		for (int i = 0; i < event.topHotels.size(); i++) {
			hotelDataItemList.add(new LaunchHotelDataItem(event.topHotels.get(i)));
		}
		adapter.setListData(hotelDataItemList, headerTitle);
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
		List<LaunchDataItem> collectionDataItemList = new ArrayList<>();
		for (int i = 0; i < event.collection.locations.size(); i++) {
			collectionDataItemList.add(new LaunchCollectionDataItem(event.collection.locations.get(i)));
		}
		adapter.setListData(collectionDataItemList, headerTitle);
	}

	public void showListLoadingAnimation() {
		List<LaunchDataItem> elements = createListForLoading();
		String headerTitle = getResources().getString(R.string.loading_header);
		adapter.setListData(elements, headerTitle);
	}

	// Create list to show cards for loading animation
	public List<LaunchDataItem> createListForLoading() {
		ArrayList<LaunchDataItem> elements = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			elements.add(new LaunchDataItem(LaunchDataItem.LOADING_VIEW));
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

	private void notifyDataSetChanged() {
		adapter.updateState();
	}

}
