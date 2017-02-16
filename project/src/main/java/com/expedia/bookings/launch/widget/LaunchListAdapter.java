package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.launch.vm.NewLaunchLobViewModel;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.widget.CollectionViewHolder;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelViewHolder;
import com.expedia.bookings.widget.SignInPlaceholderCard;
import com.expedia.bookings.widget.TextView;
import com.expedia.vm.SignInPlaceHolderViewModel;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kotlin.Unit;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static java.util.Collections.emptyList;

public class LaunchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";

	enum LaunchListViewsEnum {
		LOB_VIEW,
		HEADER_VIEW,
		HOTEL_VIEW,
		COLLECTION_VIEW,
		LOADING_VIEW,
		SIGN_IN_VIEW
	}

	private List<LaunchListViewsEnum> getHotelsStateOrder() {
		if (userBucketedForSignIn()) {
			return Collections.unmodifiableList(Arrays.asList(LaunchListViewsEnum.LOB_VIEW,
				LaunchListViewsEnum.SIGN_IN_VIEW,
				LaunchListViewsEnum.HEADER_VIEW,
				LaunchListViewsEnum.HOTEL_VIEW));
		}
		else {
			return Collections.unmodifiableList(Arrays.asList(LaunchListViewsEnum.LOB_VIEW,
				LaunchListViewsEnum.HEADER_VIEW,
				LaunchListViewsEnum.HOTEL_VIEW));
		}
	}

	private List<LaunchListViewsEnum> getCollectionStateOrder() {
		if (userBucketedForSignIn()) {
			return Collections.unmodifiableList(Arrays.asList(LaunchListViewsEnum.LOB_VIEW,
				LaunchListViewsEnum.SIGN_IN_VIEW,
				LaunchListViewsEnum.HEADER_VIEW,
				LaunchListViewsEnum.COLLECTION_VIEW));
		}
		else {
			return Collections.unmodifiableList(Arrays.asList(LaunchListViewsEnum.LOB_VIEW,
				LaunchListViewsEnum.HEADER_VIEW,
				LaunchListViewsEnum.COLLECTION_VIEW));

		}
	}

	private List<LaunchListViewsEnum> getLoadingStateOrder() {
		if (userBucketedForSignIn()) {
			return Collections.unmodifiableList(Arrays.asList(LaunchListViewsEnum.LOB_VIEW,
				LaunchListViewsEnum.SIGN_IN_VIEW,
				LaunchListViewsEnum.HEADER_VIEW,
				LaunchListViewsEnum.LOADING_VIEW));
		}
		else {
			return Collections.unmodifiableList(Arrays.asList(LaunchListViewsEnum.LOB_VIEW,
				LaunchListViewsEnum.HEADER_VIEW,
				LaunchListViewsEnum.LOADING_VIEW));
		}
	}

	private List<?> listData = emptyList();

	private Context context;
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

	public LaunchListAdapter(Context context, View header, boolean showLobView) {
		this(header);
		this.context = context;
		if (showLobView) {
			headerPosition = 1;
		}
		else {
			headerPosition = 0;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		parentView = parent;

		if (viewType == LaunchListViewsEnum.LOB_VIEW.ordinal()) {
			NewLaunchLobWidget view = (NewLaunchLobWidget) LayoutInflater.from(context)
				.inflate(R.layout.widget_new_launch_lob, parent, false);
			lobViewHolder = new LaunchLobHeaderViewHolder(view);
			return lobViewHolder;
		}
		else if (viewType == LaunchListViewsEnum.HEADER_VIEW.ordinal()) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.launch_header_root, parent, false);
			FrameLayout layout = (FrameLayout) view.findViewById(R.id.parent_layout);
			layout.addView(headerView);
			return new LaunchHeaderViewHolder(view);
		}
		else if (viewType == LaunchListViewsEnum.LOADING_VIEW.ordinal()) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.launch_tile_loading_widget, parent, false);
			return new LaunchLoadingViewHolder(view);
		}
		else if (viewType == LaunchListViewsEnum.HOTEL_VIEW.ordinal()) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.section_launch_list_card, parent, false);
			return new HotelViewHolder(view);
		}
		else if (viewType == LaunchListViewsEnum.SIGN_IN_VIEW.ordinal()) {
			View view = LayoutInflater.from(context).inflate(R.layout.feeds_prompt_card, parent, false);
			return new SignInPlaceholderCard(view, context);
		}
		else if (viewType == LaunchListViewsEnum.COLLECTION_VIEW.ordinal()) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.section_collection_list_card, parent, false);
			return new CollectionViewHolder(view);
		}
		else {
			throw new RuntimeException("Could not find view type");
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		boolean fullWidthTile;
		int itemViewType = holder.getItemViewType();
		boolean isFullSpanView = itemViewType == LaunchListViewsEnum.HEADER_VIEW.ordinal() ||
			itemViewType == LaunchListViewsEnum.LOB_VIEW.ordinal() ||
			itemViewType == LaunchListViewsEnum.SIGN_IN_VIEW.ordinal();

		if (itemViewType == LaunchListViewsEnum.LOB_VIEW.ordinal()) {
			NewLaunchLobWidget lobWidget = ((LaunchLobHeaderViewHolder) holder).getLobWidget();
			lobWidget
				.setViewModel(
					new NewLaunchLobViewModel(context, hasInternetConnectionChangeSubject, posSubject));
		}
		else if (itemViewType == LaunchListViewsEnum.SIGN_IN_VIEW.ordinal()) {
			((SignInPlaceholderCard) holder).bind(makeSignInPlaceholderViewModel());

		}
		else if (itemViewType == LaunchListViewsEnum.LOADING_VIEW.ordinal()) {
			((LaunchLoadingViewHolder) holder).bind();
		}

		if (isFullSpanView) {
			StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
				.getLayoutParams();
			layoutParams.setFullSpan(true);
			return;
		}

		// TODO - test moving this block into more appropriate home (e.g. setListDataType)
		// based on list data setting click listener on header
		if (getListDataTypeClass() == Hotel.class) {
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

		// NOTE: All the code below is for staggered views.
		// TODO - move this out into own function

		StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
			.getLayoutParams();
		int actualPosition = position - getFixedItemCount();

		if (actualPosition % 5 == 0) {
			layoutParams.setFullSpan(true);
			fullWidthTile = true;
		}
		else {
			fullWidthTile = false;
			layoutParams.setFullSpan(false);
		}

		int width = fullWidthTile ? parentView.getWidth() : parentView.getWidth() / 2;

		if (itemViewType == LaunchListViewsEnum.HOTEL_VIEW.ordinal()) {
			Hotel hotel = (Hotel) listData.get(actualPosition);

			final String url = Images.getNearbyHotelImage(hotel);
			HeaderBitmapDrawable drawable = Images
				.makeHotelBitmapDrawable(context, (HotelViewHolder) holder, width / 2, url,
					PICASSO_TAG, R.drawable.results_list_placeholder);
			((HotelViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((HotelViewHolder) holder).bindListData(hotel, fullWidthTile, hotelSelectedSubject);
		}
		else if (itemViewType == LaunchListViewsEnum.COLLECTION_VIEW.ordinal()) {
			CollectionLocation location = (CollectionLocation) listData.get(actualPosition);

			final String url = Images.getCollectionImageUrl(location, width / 2);
			HeaderBitmapDrawable drawable = Images
				.makeCollectionBitmapDrawable(context, (CollectionViewHolder) holder, url, PICASSO_TAG);
			((CollectionViewHolder) holder).setCollectionUrl(url);
			((CollectionViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((CollectionViewHolder) holder).bindListData(location, fullWidthTile, false);
		}
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (holder.getItemViewType() == LaunchListViewsEnum.LOADING_VIEW.ordinal()) {
			((LaunchLoadingViewHolder) holder).cancelAnimation();
		}
		super.onViewRecycled(holder);
	}

	private List<LaunchListViewsEnum> getCollectionWithoutSignInView(List<LaunchListViewsEnum> views) {
		ArrayList<LaunchListViewsEnum> viewsWithNoSignInView = new ArrayList<>(views);
		viewsWithNoSignInView.remove(LaunchListViewsEnum.SIGN_IN_VIEW);
		return viewsWithNoSignInView;
	}

	@Override
	public int getItemViewType(int position) {

		Class<?> listDataTypeClass = getListDataTypeClass();
		boolean showSignInView = showSignInCard();

		boolean isLoadingView = listDataTypeClass == Integer.class;
		if (isLoadingView) {
			List<LaunchListViewsEnum> viewOrder = getLoadingStateOrder();
			if (!showSignInView) {
				viewOrder = getCollectionWithoutSignInView(getLoadingStateOrder());
			}
			return getViewOrder(position, viewOrder);
		}

		boolean isHotelView = listDataTypeClass == Hotel.class;
		if (isHotelView) {
			List<LaunchListViewsEnum> viewOrder = getHotelsStateOrder();
			if (!showSignInView) {
				viewOrder = getCollectionWithoutSignInView(getHotelsStateOrder());
			}
			return getViewOrder(position, viewOrder);
		}

		boolean isCollectionView = listDataTypeClass == CollectionLocation.class;
		if (isCollectionView) {
			List<LaunchListViewsEnum> viewOrder = getCollectionStateOrder();
			if (!showSignInView) {
				viewOrder = getCollectionWithoutSignInView(getCollectionStateOrder());
			}
			return getViewOrder(position, viewOrder);
		}
		return -1;
	}

	@Override
	public int getItemCount() {
		if (showOnlyLOBView) {
			return 1;
		}
		return getFixedItemCount() + listData.size();
	}

	public int getPositionForViewType(LaunchListViewsEnum viewType) {
		Class<?> listDataTypeClass = getListDataTypeClass();
		boolean isHotelView = listDataTypeClass == Hotel.class;
		boolean isCollectionView = listDataTypeClass == CollectionLocation.class;

		List<LaunchListViewsEnum> viewOrder = emptyList();
		if (isCollectionView) {
			viewOrder = getCollectionStateOrder();
		}
		else if (isHotelView) {
			viewOrder = getHotelsStateOrder();
		}

		return viewOrder.indexOf(viewType);
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

	public void onPOSChange() {
		posSubject.onNext(Unit.INSTANCE);
	}

	public void onHasInternetConnectionChange(boolean enabled) {
		showOnlyLOBView = !enabled;
		hasInternetConnectionChangeSubject.onNext(enabled);
		notifyDataSetChanged();
	}

	private int getViewOrder(int position, List<LaunchListViewsEnum> viewOrder) {
		int lastIndex = viewOrder.size() - 1;
		if (position < lastIndex) {
			return viewOrder.get(position).ordinal();
		}
		else {
			return viewOrder.get(lastIndex).ordinal();
		}
	}

	private Class<?> getListDataTypeClass() {
		if (listData.isEmpty()) {
			// default to: loading state
			return Integer.class;
		}
		return listData.get(0).getClass();
	}

	public int getFixedItemCount() {
		Class<?> dataTypeClass = getListDataTypeClass();
		int fixedCount = 0;
		if (dataTypeClass == Integer.class) { // loading
			fixedCount = getLoadingStateOrder().size() - 1;
		}
		else if (dataTypeClass == Hotel.class) { // hotel collection
			fixedCount = getHotelsStateOrder().size() - 1;
		}
		else if (dataTypeClass == CollectionLocation.class) { // staff picks
			fixedCount = getCollectionStateOrder().size() - 1;
		}

		if (userBucketedForSignIn() && isCustomerLoggedIn() && !showOnlyLOBView) {
			fixedCount--;
		}

		return fixedCount;
	}

	public boolean showSignInCard() {
		return userBucketedForSignIn() && !isCustomerLoggedIn();
	}

	private boolean isCustomerLoggedIn() {
		return User.isLoggedIn(context);
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

	private String getBrandForSignInView() {
		return Phrase.from(context, R.string.shop_as_a_member_TEMPLATE)
			.putOptional("brand", BuildConfig.brand).format().toString();
	}

	private SignInPlaceHolderViewModel makeSignInPlaceholderViewModel() {
		return new SignInPlaceHolderViewModel(getBrandForSignInView(),
			context.getString(R.string.earn_rewards_and_unlock_deals),
			context.getString(R.string.sign_in),
			context.getString(R.string.Create_Account));
	}

	private boolean userBucketedForSignIn() {
		return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen);

	}
}
