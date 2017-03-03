package com.expedia.bookings.launch.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;
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
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripUtils;
import com.expedia.bookings.dialog.NoLocationPermissionDialog;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.launch.vm.BigImageLaunchViewModel;
import com.expedia.bookings.launch.vm.NewLaunchLobViewModel;
import com.expedia.bookings.mia.activity.MemberDealActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.widget.CollectionViewHolder;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelViewHolder;
import com.expedia.bookings.widget.TextView;
import com.expedia.util.PermissionsHelperKt;
import com.expedia.vm.ActiveItinViewModel;
import com.expedia.vm.SignInPlaceHolderViewModel;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import kotlin.Unit;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class LaunchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";

	public static boolean isStaticCard(int itemViewKey) {
		return itemViewKey == LaunchDataItem.SIGN_IN_VIEW
		    || itemViewKey == LaunchDataItem.ACTIVE_ITIN_VIEW
			|| itemViewKey == LaunchDataItem.POPULAR_HOTELS
			|| itemViewKey == LaunchDataItem.MEMBER_ONLY_DEALS;
	}

	private List<LaunchDataItem> staticCards = new ArrayList<>();
	private List<LaunchDataItem> dynamicCards = new ArrayList<>();
	private ArrayList<LaunchDataItem> listData = new ArrayList<>();

	private Context context;
	private ViewGroup parentView;
	private View headerView;
	private TextView seeAllButton;
	private TextView launchListTitle;
	private LaunchLobHeaderViewHolder lobViewHolder;
	private BehaviorSubject<Unit> posSubject = BehaviorSubject.create();
	private BehaviorSubject<Boolean> hasInternetConnectionChangeSubject = BehaviorSubject.create();
	public PublishSubject<Hotel> hotelSelectedSubject = PublishSubject.create();
	public PublishSubject<Bundle> seeAllClickSubject = PublishSubject.create();
	private boolean showOnlyLOBView = false;
	private PublishSubject<String> memberDealBackgroundUrlSubject = PublishSubject.create();

	public LaunchListAdapter(Context context, View header) {
		this.context = context;
		headerView = header;
		if (header == null) {
			throw new IllegalArgumentException("Don't pass a null View into LaunchListAdapter");
		}
		seeAllButton = ButterKnife.findById(headerView, R.id.see_all_hotels_button);
		launchListTitle = ButterKnife.findById(headerView, R.id.launch_list_header_title);
		FontCache.setTypeface(launchListTitle, FontCache.Font.ROBOTO_MEDIUM);
		setListData(new ArrayList<LaunchDataItem>(), "");
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		parentView = parent;

		if (viewType == LaunchDataItem.LOB_VIEW) {
			NewLaunchLobWidget view = (NewLaunchLobWidget) LayoutInflater.from(context)
				.inflate(R.layout.widget_new_launch_lob, parent, false);
			lobViewHolder = new LaunchLobHeaderViewHolder(view);
			return lobViewHolder;
		}
		else if (viewType == LaunchDataItem.HEADER_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.launch_header_root, parent, false);
			FrameLayout layout = (FrameLayout) view.findViewById(R.id.parent_layout);
			layout.addView(headerView);
			return new LaunchHeaderViewHolder(view);
		}
		else if (viewType == LaunchDataItem.LOADING_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.launch_tile_loading_widget, parent, false);
			return new LaunchLoadingViewHolder(view);
		}
		else if (viewType == LaunchDataItem.HOTEL_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.section_launch_list_card, parent, false);
			return new HotelViewHolder(view);
		}
		else if (viewType == LaunchDataItem.SIGN_IN_VIEW) {
			View view = LayoutInflater.from(context).inflate(R.layout.feeds_prompt_card, parent, false);
			return new SignInPlaceholderCard(view, context);
		}
		else if (viewType == LaunchDataItem.POPULAR_HOTELS) {
			View view = LayoutInflater.from(context).inflate(R.layout.big_image_launch_card, parent, false);
			BigImageLaunchViewModel vm = getPopularHotelViewModel();
			vm.setBackgroundResId(R.drawable.popular_hotel_stock_image);
			BigImageLaunchViewHolder holder = new BigImageLaunchViewHolder(view);
			holder.bind(vm);
			holder.itemView.setOnClickListener(seeAllClickListener);
			return holder;
		}
		else if (viewType == LaunchDataItem.ACTIVE_ITIN_VIEW) {
			View view = LayoutInflater.from(context).inflate(R.layout.launch_active_itin, parent, false);
			return new ActiveItinLaunchCard(view, context);
		}
		else if (viewType == LaunchDataItem.MEMBER_ONLY_DEALS) {
			View view = LayoutInflater.from(context).inflate(R.layout.big_image_launch_card, parent, false);
			view.setOnClickListener(new MemberDealClickListener());
			BigImageLaunchViewModel vm = getMemberDealViewModel();
			vm.setBackgroundUrl(getMemberDealHomeScreenImageUrl());
			BigImageLaunchViewHolder holder = new BigImageLaunchViewHolder(view);
			memberDealBackgroundUrlSubject.subscribe(vm.getBackgroundUrlChangeSubject());
			holder.bind(vm);
			return holder;
		}
		else if (viewType == LaunchDataItem.COLLECTION_VIEW) {
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
		boolean isFullSpanView = itemViewType == LaunchDataItem.HEADER_VIEW ||
			itemViewType == LaunchDataItem.LOB_VIEW || isStaticCard(itemViewType);

		// NOTE: All the code below is for staggered views.
		StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
			.getLayoutParams();
		int actualPosition = position - staticCards.size();

		if (actualPosition % 5 == 0 || isFullSpanView) {
			layoutParams.setFullSpan(true);
			fullWidthTile = true;
		}
		else {
			fullWidthTile = false;
			layoutParams.setFullSpan(false);
		}

		int width = fullWidthTile ? parentView.getWidth() : parentView.getWidth() / 2;
		if (holder instanceof LaunchLobHeaderViewHolder) {
			NewLaunchLobWidget lobWidget = ((LaunchLobHeaderViewHolder) holder).getLobWidget();
			lobWidget
				.setViewModel(
					new NewLaunchLobViewModel(context, hasInternetConnectionChangeSubject, posSubject));
		}
		else if (holder instanceof SignInPlaceholderCard) {
			((SignInPlaceholderCard) holder).bind(makeSignInPlaceholderViewModel());
		}
		else if (holder instanceof ActiveItinLaunchCard) {
			((ActiveItinLaunchCard) holder).bind(" ", makeActiveItinViewModel());
		}
		else if (holder instanceof LaunchLoadingViewHolder) {
			((LaunchLoadingViewHolder) holder).bind();
		}
		else if (holder instanceof HotelViewHolder) {
			LaunchHotelDataItem hotelDataItem = (LaunchHotelDataItem) listData.get(position);

			final String url = Images.getNearbyHotelImage(hotelDataItem.getHotel());
			HeaderBitmapDrawable drawable = Images
				.makeHotelBitmapDrawable(context, (HotelViewHolder) holder, width / 2, url,
					PICASSO_TAG, R.drawable.results_list_placeholder);
			((HotelViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((HotelViewHolder) holder).bindListData(hotelDataItem.getHotel(), fullWidthTile, hotelSelectedSubject);
		}
		else if (holder instanceof CollectionViewHolder) {
			LaunchCollectionDataItem locationDataItem = (LaunchCollectionDataItem) listData.get(position);

			final String url = Images.getCollectionImageUrl(locationDataItem.getCollection(), width / 2);
			HeaderBitmapDrawable drawable = Images
				.makeCollectionBitmapDrawable(context, (CollectionViewHolder) holder, url, PICASSO_TAG);
			((CollectionViewHolder) holder).setCollectionUrl(url);
			((CollectionViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((CollectionViewHolder) holder).bindListData(locationDataItem.getCollection(), fullWidthTile, false);
		}
		else if (holder instanceof LaunchHeaderViewHolder) {
			headerView.setOnClickListener(seeAllClickListener);
			if (BuildConfig.DEBUG && Db.getMemoryTestActive()) {
				headerView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Events.post(new Events.MemoryTestImpetus());
					}
				});
			}
		}
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (holder.getItemViewType() == LaunchDataItem.LOADING_VIEW) {
			((LaunchLoadingViewHolder) holder).cancelAnimation();
		}
		super.onViewRecycled(holder);
	}

	@Override
	public int getItemViewType(int position) {
		if (showOnlyLOBView) {
			return LaunchDataItem.LOB_VIEW;
		}
		LaunchDataItem item = listData.get(position);
		return item.getKey();
	}

	@Override
	public int getItemCount() {
		if (showOnlyLOBView) {
			return 1;
		}

		return listData.size();
	}

	public void updateState() {
		setListData(dynamicCards, launchListTitle.getText().toString());
		notifyDataSetChanged();
	}

	private ArrayList<LaunchDataItem> makeStaticCards() {
		ArrayList<LaunchDataItem> items = new ArrayList<>();
		items.add(new LaunchDataItem(LaunchDataItem.LOB_VIEW));
		if (showSignInCard()) {
			items.add(new LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW));
		}
		if (showActiveItin()) {
			items.add(new LaunchDataItem(LaunchDataItem.ACTIVE_ITIN_VIEW));
		}
		if (showMemberDeal()) {
			items.add(new LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS));
		}
		if (userBucketedForPopularHotels()) {
			items.add(new LaunchDataItem(LaunchDataItem.POPULAR_HOTELS));
		}

		items.add(new LaunchDataItem(LaunchDataItem.HEADER_VIEW));

		return items;
	}

	public void setListData(List<LaunchDataItem> objects, String headerTitle) {
		staticCards = makeStaticCards();
		dynamicCards = objects;
		listData = new ArrayList<>();
		listData.addAll(staticCards);
		listData.addAll(dynamicCards);
		setSeeAllButtonVisibility(objects, headerTitle);
		notifyDataSetChanged();
	}

	private void setSeeAllButtonVisibility(List<LaunchDataItem> listData, String headerTitle) {
		if (listData.isEmpty()) {
			return;
		}
		LaunchDataItem dataItem = listData.get(0);
		launchListTitle.setText(headerTitle);
		if (dataItem.getKey() == LaunchDataItem.HOTEL_VIEW) {
			seeAllButton.setVisibility(View.VISIBLE);
		}
		else {
			seeAllButton.setVisibility(View.GONE);
		}
	}

	public void onPOSChange() {
		posSubject.onNext(Unit.INSTANCE);
		memberDealBackgroundUrlSubject.onNext(getMemberDealHomeScreenImageUrl());
	}

	public void onHasInternetConnectionChange(boolean enabled) {
		showOnlyLOBView = !enabled;
		hasInternetConnectionChangeSubject.onNext(enabled);
		notifyDataSetChanged();
	}

	@VisibleForTesting
	protected boolean customerHasTripsInNextTwoWeeks() {
		Collection<Trip> customersTrips = getCustomerTrips();
		boolean includeSharedItins = false;
		return TripUtils.customerHasTripsInNextTwoWeeks(customersTrips, includeSharedItins);
	}

	private Collection<Trip> getCustomerTrips() {
		return ItineraryManager.getInstance().getTrips();
	}

	private final View.OnClickListener seeAllClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (PermissionsHelperKt.havePermissionToAccessLocation(context)) {
				Bundle animBundle = AnimUtils.createActivityScaleBundle(v);
				seeAllClickSubject.onNext(animBundle);
			}
			else {
				NoLocationPermissionDialog dialog = NoLocationPermissionDialog.newInstance();
				dialog.show(((FragmentActivity) context).getSupportFragmentManager(),
					NoLocationPermissionDialog.TAG);
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

	private BigImageLaunchViewModel getPopularHotelViewModel() {
			return new BigImageLaunchViewModel(R.drawable.location_pin_icon,
				R.color.hotel_tonight_background_gradient,
				R.string.launch_find_hotels_near_you,
				R.string.launch_find_recommended_hotels);
	}

	private BigImageLaunchViewModel getMemberDealViewModel() {
		return  new BigImageLaunchViewModel(R.drawable.ic_member_deals_icon,
			R.color.member_deal_background_gradient,
			R.string.member_deal_title,
			R.string.member_deal_subtitle);
	}

	private String getMemberDealHomeScreenImageUrl() {
		Akeakamai akeakamai = new Akeakamai(PointOfSale.getPointOfSale().getmMemberDealCardImageUrl());
		akeakamai.resizeExactly(context.getResources().getDimensionPixelSize(R.dimen.launch_mod_card_width), context.getResources().getDimensionPixelSize(R.dimen.launch_mod_card_height));
		return akeakamai.build();
	}

	private ActiveItinViewModel makeActiveItinViewModel() {
		if (User.isLoggedIn(context)) {
			return new ActiveItinViewModel(context.getString(R.string.launch_upcoming_trips_signed_in),
				context.getString(R.string.launch_upcoming_trips_subtext_signed_in));
		}
		else {
			return new ActiveItinViewModel(context.getString(R.string.launch_upcoming_trips_guest_user),
				context.getString(R.string.launch_upcoming_trips_subtext_guest_user));
		}
	}

	private boolean showSignInCard() {
		return userBucketedForSignIn(context) && !User.isLoggedIn(context);
	}

	private boolean showActiveItin() {
		return userBucketedForActiveItin() && customerHasTripsInNextTwoWeeks();
	}

	private boolean userBucketedForPopularHotels() {
		return FeatureToggleUtil
			.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen,
				R.string.preference_show_popular_hotels_on_launch_screen);
	}

	private boolean userBucketedForSignIn(Context context) {
		return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen);
	}

	private boolean showMemberDeal() {
		return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_member_deal_on_launch_screen) && User.isLoggedIn(context);
	}

	private boolean userBucketedForActiveItin() {
		return FeatureToggleUtil
			.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppLaunchShowActiveItinCard,
				R.string.preference_active_itin_on_launch);
	}

	private static class HeaderViewModel {
	}


	public int getOffset() {
		return staticCards.size();
	}

	private class MemberDealClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, MemberDealActivity.class);
			context.startActivity(intent);
		}
	}
}

