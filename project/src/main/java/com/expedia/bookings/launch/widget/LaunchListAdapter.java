package com.expedia.bookings.launch.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
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
import com.expedia.bookings.mia.activity.MemberDealActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.widget.CollectionViewHolder;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelViewHolder;
import com.expedia.bookings.widget.PopularHotelsTonightCard;
import com.expedia.bookings.widget.SignInPlaceholderCard;
import com.expedia.bookings.widget.TextView;
import com.expedia.vm.MemberOnlyDealViewModel;
import com.expedia.vm.PopularHotelsTonightViewModel;
import com.expedia.vm.SignInPlaceHolderViewModel;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import kotlin.Unit;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class LaunchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";

	enum LaunchListViewsEnum {
		LOADING_VIEW,
		LOB_VIEW,
		SIGN_IN_VIEW,
		POPULAR_HOTELS,
		MEMBER_DEAL_VIEW,
		HEADER_VIEW,
		HOTEL_VIEW,
		COLLECTION_VIEW
	}

	public static boolean isStaticCard(int itemViewType) {
		return itemViewType == LaunchListViewsEnum.SIGN_IN_VIEW.ordinal() ||
			itemViewType == LaunchListViewsEnum.POPULAR_HOTELS.ordinal()
			|| itemViewType == LaunchListViewsEnum.MEMBER_DEAL_VIEW.ordinal();
	}

	private List<?> staticCards = new ArrayList<>();
	private List<?> dynamicCards = new ArrayList<>();
	private ArrayList<Object> listData = new ArrayList<>();

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

	public LaunchListAdapter(Context context, View header) {
		this.context = context;
		headerView = header;
		if (header == null) {
			throw new IllegalArgumentException("Don't pass a null View into LaunchListAdapter");
		}
		seeAllButton = ButterKnife.findById(headerView, R.id.see_all_hotels_button);
		launchListTitle = ButterKnife.findById(headerView, R.id.launch_list_header_title);
		FontCache.setTypeface(launchListTitle, FontCache.Font.ROBOTO_MEDIUM);
		setListData(new ArrayList<Object>(), "");
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
		else if (viewType == LaunchListViewsEnum.POPULAR_HOTELS.ordinal()) {
			View view = LayoutInflater.from(context).inflate(R.layout.feeds_popular_hotels_tonight_card, parent, false);
			return new PopularHotelsTonightCard(view, context);
		}
		else if (viewType == LaunchListViewsEnum.MEMBER_DEAL_VIEW.ordinal()) {
			View view = LayoutInflater.from(context).inflate(R.layout.member_deal_launch_cell, parent, false);
			view.setOnClickListener(new MemberDealClickListener());
			return new MemberDealLaunchViewHolder(view);
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
			itemViewType == LaunchListViewsEnum.LOB_VIEW.ordinal() || isStaticCard(itemViewType);

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
		else if (holder instanceof PopularHotelsTonightCard) {
			((PopularHotelsTonightCard) holder).bind(makePopularHotelsTonightViewModel());
			((PopularHotelsTonightCard) holder).itemView.setOnClickListener(seeAllClickListener);
		}
		else if (holder instanceof LaunchLoadingViewHolder) {
			((LaunchLoadingViewHolder) holder).bind();
		}

		else if (holder instanceof HotelViewHolder) {
			Hotel hotel = (Hotel) listData.get(position);

			final String url = Images.getNearbyHotelImage(hotel);
			HeaderBitmapDrawable drawable = Images
				.makeHotelBitmapDrawable(context, (HotelViewHolder) holder, width / 2, url,
					PICASSO_TAG, R.drawable.results_list_placeholder);
			((HotelViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((HotelViewHolder) holder).bindListData(hotel, fullWidthTile, hotelSelectedSubject);
		}
		else if (holder instanceof CollectionViewHolder) {
			CollectionLocation location = (CollectionLocation) listData.get(position);

			final String url = Images.getCollectionImageUrl(location, width / 2);
			HeaderBitmapDrawable drawable = Images
				.makeCollectionBitmapDrawable(context, (CollectionViewHolder) holder, url, PICASSO_TAG);
			((CollectionViewHolder) holder).setCollectionUrl(url);
			((CollectionViewHolder) holder).getBackgroundImage().setImageDrawable(drawable);

			((CollectionViewHolder) holder).bindListData(location, fullWidthTile, false);
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
		if (showOnlyLOBView) {
			return LaunchListViewsEnum.LOB_VIEW.ordinal();
		}

		Object item = listData.get(position);

		if (item instanceof Integer) {
			return LaunchListViewsEnum.LOADING_VIEW.ordinal();
		}
		else if (item instanceof Hotel) {
			return LaunchListViewsEnum.HOTEL_VIEW.ordinal();
		}
		else if (item instanceof CollectionLocation) {
			return LaunchListViewsEnum.COLLECTION_VIEW.ordinal();
		}
		else if (item instanceof HeaderViewModel) {
			return LaunchListViewsEnum.HEADER_VIEW.ordinal();
		}
		else if (item instanceof SignInPlaceHolderViewModel) {
			return LaunchListViewsEnum.SIGN_IN_VIEW.ordinal();
		}
		else if (item instanceof NewLaunchLobViewModel) {
			return LaunchListViewsEnum.LOB_VIEW.ordinal();
		}
		else if (item instanceof PopularHotelsTonightViewModel) {
			return LaunchListViewsEnum.POPULAR_HOTELS.ordinal();
		}
		else if (item instanceof MemberOnlyDealViewModel) {
			return LaunchListViewsEnum.MEMBER_DEAL_VIEW.ordinal();
		}
		else {
			return -1;
		}
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

	private ArrayList<Object> makeStaticCards() {
		ArrayList<Object> items = new ArrayList<Object>();
		items.add(new NewLaunchLobViewModel(context, null, null));
		if (showSignInCard()) {
			items.add(makeSignInPlaceholderViewModel());
		}
		if (userBucketedForPopularHotels()) {
			items.add(makePopularHotelsTonightViewModel());
		}
		if (isBucketedForMemberDeal()) {
			items.add(new MemberOnlyDealViewModel());
		}
		items.add(new LaunchListAdapter.HeaderViewModel());
		return items;
	}

	public void setListData(List<?> objects, String headerTitle) {
		staticCards = makeStaticCards();
		dynamicCards = objects;
		listData = new ArrayList<Object>();
		listData.addAll(staticCards);
		listData.addAll(dynamicCards);
		setSeeAllButtonVisibility(objects, headerTitle);
		notifyDataSetChanged();
	}

	private void setSeeAllButtonVisibility(List<?> listData, String headerTitle) {
		if (listData.isEmpty()) {
			return;
		}
		Class clz = listData.get(0).getClass();
		launchListTitle.setText(headerTitle);
		if (clz == Integer.class) {
			seeAllButton.setVisibility(View.GONE);
		}
		else if (clz == Hotel.class) {
			seeAllButton.setVisibility(View.VISIBLE);
		}
		else if (clz == CollectionLocation.class) {
			seeAllButton.setVisibility(View.GONE);
		}
	}

	public void onPOSChange() {
		posSubject.onNext(Unit.INSTANCE);
	}

	public void onHasInternetConnectionChange(boolean enabled) {
		showOnlyLOBView = !enabled;
		hasInternetConnectionChangeSubject.onNext(enabled);
		notifyDataSetChanged();
	}

	private final View.OnClickListener seeAllClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle animBundle = AnimUtils.createActivityScaleBundle(v);
			seeAllClickSubject.onNext(animBundle);
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

	private PopularHotelsTonightViewModel makePopularHotelsTonightViewModel() {
		if (Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen)
			== AbacusUtils.TripsPopularHotelsVariant.VARIANT1.ordinal()) {
			return new PopularHotelsTonightViewModel(R.drawable.popular_hotel_stock_image,
				context.getString(R.string.launch_find_hotels_near_you),
				context.getString(R.string.launch_find_popular_hotels));
		}
		else {
			return new PopularHotelsTonightViewModel(R.drawable.popular_hotel_stock_image,
				context.getString(R.string.launch_find_popular_hotels),
				context.getString(R.string.launch_find_hotels_near_you));
		}
	}

	private boolean showSignInCard() {
		return userBucketedForSignIn(context) && !User.isLoggedIn(context);
	}

	private boolean userBucketedForPopularHotels() {
		return FeatureToggleUtil
			.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen,
				R.string.preference_show_popular_hotels_on_launch_screen);
	}

	private boolean userBucketedForSignIn(Context context) {
		return FeatureToggleUtil
			.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen,
				R.string.preference_show_sign_in_on_launch_screen);
	}

	private boolean isBucketedForMemberDeal() {
		return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_member_deal_on_launch_screen);
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

