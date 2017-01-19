package com.expedia.bookings.launch.widget;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.launch.vm.NewLaunchLobViewModel;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.widget.CollectionViewHolder;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelViewHolder;
import com.expedia.bookings.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class LaunchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";
	private static final int LOB_VIEW = 0;
	private static final int HEADER_VIEW = 1;
	public static final int HOTEL_VIEW = 2;
	public static final int COLLECTION_VIEW = 3;
	public static final int LOADING_VIEW = 4;
	private List<?> listData = new ArrayList<>();

	private ViewGroup parentView;
	private View headerView;
	private TextView seeAllButton;
	private TextView launchListTitle;
	private LaunchLobHeaderViewHolder lobViewHolder;

	// 0 means we are in old launch screen and 1 means we are in new search screen we want to add lob
	public int headerPosition = 0;

	public static boolean loadingState = false;

	public BehaviorSubject<Unit> posSubject = BehaviorSubject.create();
	public BehaviorSubject<Boolean> hasInternetConnectionChangeSubject = BehaviorSubject.create();
	public PublishSubject<Hotel> hotelSelectedSubject = PublishSubject.create();
	public PublishSubject<Bundle> seeAllClickSubject = PublishSubject.create();
	private boolean showOnlyLOBView = false;

	public LaunchListAdapter(View header) {
		headerView = header;
		if (header == null) {
			throw new IllegalArgumentException("Don't pass a null View into LaunchListAdapter");
		}
		seeAllButton = ButterKnife.findById(headerView, R.id.see_all_hotels_button);
		launchListTitle = ButterKnife.findById(headerView, R.id.launch_list_header_title);
		FontCache.setTypeface(launchListTitle, FontCache.Font.ROBOTO_MEDIUM);
	}

	public LaunchListAdapter(View header, boolean showLobView) {
		this(header);
		if (showLobView) {
			headerPosition = 1;
		}
		else {
			headerPosition = 0;
		}
	}

	private boolean isHeader(int position) {
		return position == headerPosition;
	}

	public boolean isLobView(int position) {
		return headerPosition == 1 && position == 0;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == LOB_VIEW) {
			NewLaunchLobWidget view = (NewLaunchLobWidget) LayoutInflater.from(parent.getContext())
				.inflate(R.layout.widget_new_launch_lob, parent, false);
			lobViewHolder = new LaunchLobHeaderViewHolder(view);
			return lobViewHolder;
		}
		if (viewType == HEADER_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.launch_header_root, parent, false);
			FrameLayout layout = (FrameLayout) view.findViewById(R.id.parent_layout);
			layout.addView(headerView);
			return new LaunchHeaderViewHolder(view);
		}
		parentView = parent;

		if (viewType == LOADING_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.launch_tile_loading_widget, parent, false);
			return new LaunchLoadingViewHolder(view);
		}
		else if (viewType == HOTEL_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.section_launch_list_card, parent, false);
			return new HotelViewHolder(view);
		}
		else {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.section_collection_list_card, parent, false);
			return new CollectionViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		boolean fullWidthTile;
		if (holder.getItemViewType() == LOB_VIEW) {
			NewLaunchLobWidget lobWidget = ((LaunchLobHeaderViewHolder) holder).getLobWidget();
			lobWidget
				.setViewModel(
					new NewLaunchLobViewModel(lobWidget.getContext(), hasInternetConnectionChangeSubject, posSubject));
		}
		if (holder.getItemViewType() == HEADER_VIEW || holder.getItemViewType() == LOB_VIEW) {
			StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
				.getLayoutParams();
			layoutParams.setFullSpan(true);
			return;
		}

		// based on list data setting click listener on header
		if (listData.get(0).getClass() == Hotel.class) {
			headerView.setOnClickListener(seeAllClickListener);
		}
		else if (BuildConfig.DEBUG && Db.getMemoryTestActive()) {
			headerView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.MemoryTestImpetus());
				}
			});
		}

		StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
			.getLayoutParams();
		int actualPosition = position - (headerPosition + 1);
		if (actualPosition % 5 == 0) {
			layoutParams.setFullSpan(true);
			fullWidthTile = true;
		}
		else {
			fullWidthTile = false;
			layoutParams.setFullSpan(false);
		}

		int width = fullWidthTile ? parentView.getWidth() : parentView.getWidth() / 2;

		if (holder.getItemViewType() == LOADING_VIEW) {
			((LaunchLoadingViewHolder) holder).bind();
		}
		else if (holder.getItemViewType() == HOTEL_VIEW) {
			Hotel hotel = (Hotel) listData.get(actualPosition);

			final String url = Images.getNearbyHotelImage(hotel);
			HeaderBitmapDrawable drawable = Images
				.makeHotelBitmapDrawable(parentView.getContext(), (HotelViewHolder) holder, width / 2, url,
					PICASSO_TAG, R.drawable.results_list_placeholder);
			((HotelViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((HotelViewHolder) holder).bindListData(hotel, fullWidthTile, hotelSelectedSubject);
		}
		else if (holder.getItemViewType() == COLLECTION_VIEW) {
			CollectionLocation location = (CollectionLocation) listData.get(actualPosition);

			final String url = Images.getCollectionImageUrl(location, width / 2);
			HeaderBitmapDrawable drawable = Images
				.makeCollectionBitmapDrawable(parentView.getContext(), (CollectionViewHolder) holder, url, PICASSO_TAG);
			((CollectionViewHolder) holder).setCollectionUrl(url);
			((CollectionViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((CollectionViewHolder) holder).bindListData(location, fullWidthTile, false);
		}
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (holder.getItemViewType() == LOADING_VIEW) {
			((LaunchLoadingViewHolder) holder).cancelAnimation();
		}
		super.onViewRecycled(holder);
	}

	@Override
	public int getItemViewType(int position) {
		if (isLobView(position)) {
			return LOB_VIEW;
		}
		else if (isHeader(position)) {
			return HEADER_VIEW;
		}
		else if (loadingState) {
			return LOADING_VIEW;
		}
		else if (listData.get(position - (headerPosition + 1)).getClass() == CollectionLocation.class) {
			return COLLECTION_VIEW;
		}
		else {
			return HOTEL_VIEW;
		}
	}

	@Override
	public int getItemCount() {
		if (showOnlyLOBView && headerPosition == 1) {
			return 1;
		}
		return listData.size() + 1 + headerPosition;
	}

	public void setListData(List<?> listData, String headerTitle) {

		Class clz = listData.get(0).getClass();
		launchListTitle.setText(headerTitle);
		if (clz == Integer.class) {
			seeAllButton.setVisibility(View.GONE);
			loadingState = true;
		}
		else if (clz == Hotel.class) {
			seeAllButton.setVisibility(View.VISIBLE);
			loadingState = false;
		}
		else if (clz == CollectionLocation.class) {
			seeAllButton.setVisibility(View.GONE);
			loadingState = false;
		}

		this.listData = listData;
	}

	private final View.OnClickListener seeAllClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle animBundle = AnimUtils.createActivityScaleBundle(v);
			Events.post(new Events.LaunchSeeAllButtonPressed(animBundle));
			if (seeAllClickSubject != null) {
				seeAllClickSubject.onNext(animBundle);
			}
			OmnitureTracking.trackNewLaunchScreenSeeAllClick();
		}
	};

	public void onPOSChange() {
		posSubject.onNext(Unit.INSTANCE);
	}


	public void onHasInternetConnectionChange(boolean enabled) {
		showOnlyLOBView = !enabled;
		hasInternetConnectionChangeSubject.onNext(enabled);
		notifyDataSetChanged();
	}
}
