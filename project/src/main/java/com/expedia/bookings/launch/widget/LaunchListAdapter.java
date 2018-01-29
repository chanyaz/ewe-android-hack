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
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.data.trips.TripUtils;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.dialog.NoLocationPermissionDialog;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.itin.ItinLaunchScreenHelper;
import com.expedia.bookings.launch.vm.BigImageLaunchViewModel;
import com.expedia.bookings.launch.vm.LaunchLobViewModel;
import com.expedia.bookings.meso.model.MesoDestinationAdResponse;
import com.expedia.bookings.meso.model.MesoHotelAdResponse;
import com.expedia.bookings.meso.MesoDestinationViewHolder;
import com.expedia.bookings.meso.vm.MesoDestinationViewModel;
import com.expedia.bookings.meso.vm.MesoHotelAdViewModel;
import com.expedia.bookings.mia.activity.LastMinuteDealActivity;
import com.expedia.bookings.mia.activity.MemberDealsActivity;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.ProWizardBucketCache;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CollectionViewHolder;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelViewHolder;
import com.expedia.bookings.widget.LaunchScreenAirAttachCard;
import com.expedia.bookings.widget.TextView;
import com.expedia.util.Optional;
import com.expedia.util.PermissionsUtils;
import com.expedia.vm.launch.ActiveItinViewModel;
import com.expedia.vm.launch.BrandSignInLaunchHolderViewModel;
import com.expedia.vm.launch.LaunchScreenAirAttachViewModel;
import com.expedia.vm.launch.RecommendedHotelViewModel;
import com.expedia.vm.launch.SignInPlaceHolderViewModel;
import com.mobiata.android.Log;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;

public class LaunchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";
	private final int contentStartPosition;

	public static boolean isStaticCard(int itemViewKey) {
		return itemViewKey == LaunchDataItem.SIGN_IN_VIEW
			|| itemViewKey == LaunchDataItem.AIR_ATTACH_VIEW
			|| itemViewKey == LaunchDataItem.ITIN_VIEW
			|| itemViewKey == LaunchDataItem.MESO_HOTEL_AD_VIEW
			|| itemViewKey == LaunchDataItem.MESO_DESTINATION_AD_VIEW
			|| itemViewKey == LaunchDataItem.MEMBER_ONLY_DEALS
			|| itemViewKey == LaunchDataItem.LAST_MINUTE_DEALS;
	}

	public PublishSubject<Hotel> hotelSelectedSubject = PublishSubject.create();
	public PublishSubject<Bundle> seeAllClickSubject = PublishSubject.create();

	private BehaviorSubject<Unit> posSubject = BehaviorSubject.create();
	private BehaviorSubject<Boolean> hasInternetConnectionChangeSubject = BehaviorSubject.create();
	private PublishSubject<String> memberDealBackgroundUrlSubject = PublishSubject.create();
	private List<LaunchDataItem> staticCards = new ArrayList<>();
	private List<LaunchDataItem> dynamicCards = new ArrayList<>();
	private ArrayList<LaunchDataItem> listData = new ArrayList<>();

	private MesoHotelAdViewModel mesoHotelAdViewModel;
	private MesoDestinationViewModel mesoDestinationViewModel;


	private Context context;
	private ViewGroup parentView;
	private View headerView;
	private TextView seeAllButton;
	private TextView launchListTitle;

	private boolean showOnlyLOBView = false;
	private UserStateManager userStateManager;

	public LaunchListAdapter(Context context, View header) {
		this.context = context;
		if (showProWizard()) {
			contentStartPosition = 0;
		}
		else {
			contentStartPosition = 1;
		}
		headerView = header;
		if (header == null) {
			throw new IllegalArgumentException("Don't pass a null View into LaunchListAdapter");
		}

		userStateManager = Ui.getApplication(context).appComponent().userStateManager();

		seeAllButton = ButterKnife.findById(headerView, R.id.see_all_hotels_button);
		launchListTitle = ButterKnife.findById(headerView, R.id.launch_list_header_title);
		FontCache.setTypeface(launchListTitle, FontCache.Font.ROBOTO_MEDIUM);
		setListData(new ArrayList<LaunchDataItem>(), "");
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		parentView = parent;

		if (viewType == LaunchDataItem.LOB_VIEW) {
			LaunchLobWidget view = (LaunchLobWidget) LayoutInflater.from(context)
				.inflate(R.layout.widget_launch_lob, parent, false);
			return new LaunchLobHeaderViewHolder(view);
		}

		if (viewType == LaunchDataItem.HEADER_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.launch_header_root, parent, false);
			FrameLayout layout = (FrameLayout) view.findViewById(R.id.parent_layout);
			ViewGroup parentView = (ViewGroup) headerView.getParent();
			if (parentView != null) {
				parentView.removeView(headerView);
			}
			layout.addView(headerView);
			return new LaunchHeaderViewHolder(view);
		}

		if (viewType == LaunchDataItem.LOADING_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.launch_tile_loading_widget, parent, false);
			return new LaunchLoadingViewHolder(view);
		}

		if (viewType == LaunchDataItem.MESO_HOTEL_AD_VIEW) {
			final View view = LayoutInflater.from(context)
				.inflate(R.layout.launch_meso_hotel_ad_card_view, parent, false);
			return new MesoHotelAdViewHolder(view, mesoHotelAdViewModel);
		}

		if (viewType == LaunchDataItem.MESO_DESTINATION_AD_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.meso_destination_launch_card, parent, false);
			return new MesoDestinationViewHolder(view, mesoDestinationViewModel);
		}

		if (viewType == LaunchDataItem.HOTEL_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.section_launch_list_card, parent, false);
			return new HotelViewHolder(view);
		}

		if (viewType == LaunchDataItem.SIGN_IN_VIEW) {
			if (AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppBrandColors)) {
				View view = LayoutInflater.from(context).inflate(R.layout.signin_prompt_card, parent, false);
				return new BrandSignInLaunchCard(view, context);
			}
			else {
				View view = LayoutInflater.from(context).inflate(R.layout.feeds_prompt_card, parent, false);
				return new SignInPlaceholderCard(view, context);
			}
		}

		if (viewType == LaunchDataItem.AIR_ATTACH_VIEW) {
			View view = LayoutInflater.from(context).inflate(R.layout.launch_screen_air_attach_card, parent, false);
			return new LaunchScreenAirAttachCard(view);
		}

		if (viewType == LaunchDataItem.ITIN_VIEW) {
			View view = LayoutInflater.from(context).inflate(R.layout.launch_active_itin, parent, false);
			return new ItinLaunchCard(view, context);
		}


		if (viewType == LaunchDataItem.MEMBER_ONLY_DEALS) {
			View view = LayoutInflater.from(context).inflate(R.layout.big_image_launch_card, parent, false);
			view.setOnClickListener(new MemberDealClickListener());

			int memberDealsDrawable = R.drawable.ic_member_deals_icon;
			if (AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppBrandColors)) {
				memberDealsDrawable = R.drawable.ic_member_only_tag_bg;
			}

			BigImageLaunchViewModel vm = getDealViewModel(memberDealsDrawable,
				R.color.member_deals_background_gradient,
				R.string.member_deal_title, R.string.member_deal_subtitle);
			vm.setBackgroundUrl(getBigImageResizedUrl(PointOfSale.getPointOfSale().getmMemberDealCardImageUrl()));
			BigImageLaunchViewHolder holder = new BigImageLaunchViewHolder(view);
			memberDealBackgroundUrlSubject.subscribe(vm.getBackgroundUrlChangeSubject());
			holder.bind(vm);
			return holder;
		}

		if (viewType == LaunchDataItem.COLLECTION_VIEW) {
			View view = LayoutInflater.from(context)
				.inflate(R.layout.section_collection_list_card, parent, false);
			return new CollectionViewHolder(view);
		}

		if (viewType == LaunchDataItem.LAST_MINUTE_DEALS) {
			View view = LayoutInflater.from(context).inflate(R.layout.big_image_launch_card, parent, false);
			BigImageLaunchViewModel vm = getDealViewModel(R.drawable.ic_last_minute_deals_icon,
				R.color.last_minute_deal_background_gradient, R.string.last_minute_deal_title,
				R.string.last_minute_deal_subtitle);
			vm.setBackgroundUrl(getBigImageResizedUrl(
				PointOfSale.getPointOfSale().getmLastMinuteDealImageUrl()));
			view.setOnClickListener(new LastMinuteDealClickListener());
			vm.setBackgroundUrl(getBigImageResizedUrl(PointOfSale.getPointOfSale().getmLastMinuteDealImageUrl()));
			BigImageLaunchViewHolder holder = new BigImageLaunchViewHolder(view);
			holder.bind(vm);
			return new BigImageLaunchViewHolder(view);
		}

		throw new RuntimeException("Could not find view type");
	}


	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		boolean fullWidthTile;

		// NOTE: All the code below is for staggered views.
		StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
			.getLayoutParams();
		int actualPosition = position - staticCards.size();

		if (actualPosition % 5 == 0 || isFullSpanView(holder)) {
			layoutParams.setFullSpan(true);
			fullWidthTile = true;
		}
		else {
			fullWidthTile = false;
			layoutParams.setFullSpan(false);
		}

		int width = fullWidthTile ? parentView.getWidth() : parentView.getWidth() / 2;

		if (holder instanceof LaunchLobHeaderViewHolder) {
			LaunchLobWidget lobWidget = ((LaunchLobHeaderViewHolder) holder).getLobWidget();
			lobWidget
				.setViewModel(
					new LaunchLobViewModel(context, hasInternetConnectionChangeSubject, posSubject));
		}
		else if (holder instanceof SignInPlaceholderCard) {
			((SignInPlaceholderCard) holder).bind(makeSignInPlaceholderViewModel());
		}
		else if (holder instanceof BrandSignInLaunchCard) {
			((BrandSignInLaunchCard) holder).bind(makeSignInLaunchHolderViewModel());
		}
		else if (holder instanceof LaunchScreenAirAttachCard) {
			Trip recentUpcomingFlightTrip = getUpcomingAirAttachQualifiedFlightTrip();
			if (recentUpcomingFlightTrip != null) {
				TripFlight tripFlight = (TripFlight) recentUpcomingFlightTrip.getTripComponents().get(0);
				HotelSearchParams hotelSearchParams = TripUtils
					.getHotelSearchParamsForRecentFlightAirAttach(tripFlight);
				String cityName = TripUtils.getFlightTripDestinationCity(tripFlight);
				LaunchScreenAirAttachViewModel viewModel = new LaunchScreenAirAttachViewModel(context, holder.itemView,
					recentUpcomingFlightTrip, hotelSearchParams, cityName);
				((LaunchScreenAirAttachCard) holder).bind(viewModel);
			}
		}
		else if (holder instanceof MesoHotelAdViewHolder) {
			((MesoHotelAdViewHolder) holder).bindListData();
		}
		else if (holder instanceof MesoDestinationViewHolder) {
			((MesoDestinationViewHolder) holder).bindData();
		}
		else if (holder instanceof ItinLaunchCard) {
			((ItinLaunchCard) holder).bind(context, makeActiveItinViewModel());
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

			RecommendedHotelViewModel recommendedHotelViewModel = new RecommendedHotelViewModel(context, hotelDataItem.getHotel());
			((HotelViewHolder) holder).bindListData(hotelDataItem.getHotel(), fullWidthTile, hotelSelectedSubject, recommendedHotelViewModel);
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
		if (showOnlyLOBView && !showProWizard()) {
			return LaunchDataItem.LOB_VIEW;
		}
		LaunchDataItem item = listData.get(position);
		return item.getKey();
	}

	@Override
	public int getItemCount() {
		if (showOnlyLOBView && !showProWizard()) {
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
		if (!showProWizard()) {
			items.add(new LaunchDataItem(LaunchDataItem.LOB_VIEW));
		}
		if (!showOnlyLOBView) {
			if (showSignInCard()) {
				items.add(new LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW));
			}
			if (showItinCard()) {
				items.add(new LaunchDataItem(LaunchDataItem.ITIN_VIEW));
			}
			if (showAirAttachMessage()) {
				items.add(new LaunchDataItem(LaunchDataItem.AIR_ATTACH_VIEW));
			}
			if (showMemberDeal()) {
				items.add(new LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS));
			}
			if (showMesoHotelAd()) {
				if (mesoHotelAdViewModel != null && mesoHotelAdViewModel.dataIsValid()) {
					items.add(new LaunchDataItem(LaunchDataItem.MESO_HOTEL_AD_VIEW));
				}
			}
			if (showMesoDestinationAd()) {
				if (mesoDestinationViewModel != null && mesoDestinationViewModel.getMesoDestinationAdResponse() != null) {
					items.add(new LaunchDataItem(LaunchDataItem.MESO_DESTINATION_AD_VIEW));
				}
			}
			if (showLastMinuteDeal()) {
				items.add(new LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS));
			}
			items.add(new LaunchDataItem(LaunchDataItem.HEADER_VIEW));
		}
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

	@VisibleForTesting
	protected void addDelayedStaticCards(final ArrayList<LaunchDataItem> cards) {
		staticCards.addAll(cards);
		listData.addAll(contentStartPosition, cards);
		notifyItemRangeInserted(contentStartPosition, cards.size());
	}

	private void setSeeAllButtonVisibility(List<LaunchDataItem> listData, String headerTitle) {
		if (listData.isEmpty()) {
			return;
		}
		LaunchDataItem dataItem = listData.get(0);
		launchListTitle.setText(headerTitle);
		if (dataItem.getKey() == LaunchDataItem.HOTEL_VIEW) {
			seeAllButton.setVisibility(View.VISIBLE);
			headerView.setOnClickListener(seeAllClickListener);
		}
		else {
			seeAllButton.setVisibility(View.GONE);
			headerView.setOnClickListener(null);
		}
	}

	public void onPOSChange() {
		posSubject.onNext(Unit.INSTANCE);
		memberDealBackgroundUrlSubject
			.onNext(getBigImageResizedUrl(PointOfSale.getPointOfSale().getmMemberDealCardImageUrl()));
	}

	public void onHasInternetConnectionChange(boolean enabled) {
		showOnlyLOBView = !enabled;
		hasInternetConnectionChangeSubject.onNext(enabled);
		updateState();
	}

	@VisibleForTesting
	protected List<Trip> getCustomerTrips() {
		return new ArrayList<>(ItineraryManager.getInstance().getTrips());
	}

	private boolean isFullSpanView(RecyclerView.ViewHolder holder) {
		int itemViewType = holder.getItemViewType();
		return itemViewType == LaunchDataItem.HEADER_VIEW
			|| itemViewType == LaunchDataItem.LOB_VIEW
			|| isStaticCard(itemViewType);
	}

	private final View.OnClickListener seeAllClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			checkForPermissionAndGoToHotels(v);
			OmnitureTracking.trackNewLaunchScreenSeeAllClick();
		}
	};

	private void checkForPermissionAndGoToHotels(View v) {
		if (PermissionsUtils.havePermissionToAccessLocation(context)) {
			Bundle animBundle = AnimUtils.createActivityScaleBundle(v);
			seeAllClickSubject.onNext(animBundle);
		}
		else {
			NoLocationPermissionDialog dialog = NoLocationPermissionDialog.newInstance();
			dialog.show(((FragmentActivity) context).getSupportFragmentManager(),
				NoLocationPermissionDialog.TAG);
		}
	}

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

	private BrandSignInLaunchHolderViewModel makeSignInLaunchHolderViewModel() {
		return new BrandSignInLaunchHolderViewModel(getBrandForSignInView(),
			context.getString(R.string.member_prices_signin),
			context.getString(R.string.sign_in_create_account));
	}

	private BigImageLaunchViewModel getDealViewModel(int dealsIcon, int backgroundGradient,
		int title, int subtitle) {
		return new BigImageLaunchViewModel(dealsIcon,
			backgroundGradient,
			title,
			subtitle);
	}

	private String getBigImageResizedUrl(String imageUrl) {
		Akeakamai akeakamai = new Akeakamai(imageUrl);
		akeakamai.resizeExactly(context.getResources().getDimensionPixelSize(R.dimen.launch_big_image_card_width),
			context.getResources().getDimensionPixelSize(R.dimen.launch_big_image_card_height));
		return akeakamai.build();
	}

	private ActiveItinViewModel makeActiveItinViewModel() {
		return new ActiveItinViewModel(
			context.getString(R.string.launch_upcoming_trips_signed_in),
			context.getString(R.string.launch_upcoming_trips_subtext_signed_in));
	}

	private boolean showSignInCard() {
		return !userStateManager.isUserAuthenticated();
	}

	private boolean showAirAttachMessage() {
		return userBucketedForAirAttach() && userStateManager.isUserAuthenticated()
			&& isBrandAirAttachEnabled() && getUpcomingAirAttachQualifiedFlightTrip() != null;
	}

	@VisibleForTesting
	public Trip getUpcomingAirAttachQualifiedFlightTrip() {
		return TripUtils.getUpcomingAirAttachQualifiedFlightTrip(getCustomerTrips());
	}

	@VisibleForTesting
	public boolean isBrandAirAttachEnabled() {
		return ProductFlavorFeatureConfiguration.getInstance()
			.shouldShowAirAttach();
	}

	@VisibleForTesting
	protected boolean showActiveItinLaunchScreenCard() {
		return ItinLaunchScreenHelper.showActiveItinLaunchScreenCard(userStateManager);
	}

	private boolean showItinCard() {
		return showActiveItinLaunchScreenCard();
	}

	private boolean userBucketedForAirAttach() {
		return AbacusFeatureConfigManager
			.isUserBucketedForTest(AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen);
	}

	private boolean showMemberDeal() {
		return userStateManager.isUserAuthenticated();
	}

	private boolean showLastMinuteDeal() {
		return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppLastMinuteDeals);

	}

	@VisibleForTesting
	protected boolean showMesoHotelAd() {
		if (AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.MesoAd)) {
			int variateForTest = Db.sharedInstance.getAbacusResponse().variateForTest(AbacusUtils.MesoAd);
			return variateForTest == AbacusVariant.ONE.getValue();
		}
		return false;
	}

	@VisibleForTesting
	protected boolean showMesoDestinationAd() {
		if (AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.MesoAd)) {
			int variateForTest = Db.sharedInstance.getAbacusResponse().variateForTest(AbacusUtils.MesoAd);
			return variateForTest == AbacusVariant.TWO.getValue();
		}
		return false;
	}

	public void initMesoAd() {
		if (showMesoHotelAd()) {
			mesoHotelAdViewModel = new MesoHotelAdViewModel(context);
			mesoHotelAdViewModel.fetchHotelMesoAd(new Observer<Optional<MesoHotelAdResponse>>() {
				@Override
				public void onSubscribe(Disposable d) {
					// Not used. Calling fetch completes the Observer.
				}

				@Override
				public void onNext(Optional<MesoHotelAdResponse> mesoHotelAdResponseOptional) {
					updateState();
				}

				@Override
				public void onComplete() {
					Log.d("Meso hotel ad request has been completed");
				}

				@Override
				public void onError(Throwable e) {
					Log.d(e.getMessage());
				}

			});
		}
		else if (showMesoDestinationAd()) {
			mesoDestinationViewModel = new MesoDestinationViewModel(context);
			mesoDestinationViewModel.fetchDestinationMesoAd(new Observer<Optional<MesoDestinationAdResponse>>() {
				@Override
				public void onSubscribe(Disposable d) {
					// Not used. Calling fetch completes the Observer.
				}

				@Override
				public void onNext(Optional<MesoDestinationAdResponse> mesoDestinationAdResponse) {
					updateState();
				}

				@Override
				public void onComplete() {
					Log.d("Meso destination ad request has been completed");
				}

				@Override
				public void onError(Throwable e) {
					Log.d(e.getMessage());
				}

			});
		}
	}

	private boolean showProWizard() {
		return ProWizardBucketCache.isBucketed(context);
	}

	public int getOffset() {
		return staticCards.size();
	}

	private class ItinSyncListener extends ItineraryManager.ItinerarySyncAdapter {
		@Override
		public void onSyncFinished(Collection<Trip> trips) {
			if (isStaticCardAlreadyShown(LaunchDataItem.ITIN_VIEW)) {
				return;
			}

			ArrayList<LaunchDataItem> items = new ArrayList<>();
			if (showItinCard()) {
				items.add(new LaunchDataItem(LaunchDataItem.ITIN_VIEW));
			}

			if (isStaticCardAlreadyShown(LaunchDataItem.AIR_ATTACH_VIEW)) {
				return;
			}

			if (showAirAttachMessage()) {
				items.add(new LaunchDataItem(LaunchDataItem.AIR_ATTACH_VIEW));
			}
			addDelayedStaticCards(items);
		}
	}

	@VisibleForTesting
	protected boolean isStaticCardAlreadyShown(int key) {
		for (LaunchDataItem item : staticCards) {
			if (item.getKey() == key) {
				return true;
			}
		}
		return false;
	}

	private class MemberDealClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, MemberDealsActivity.class);
			context.startActivity(intent);
			OmnitureTracking.trackLaunchMemberPricing();
		}
	}

	private class LastMinuteDealClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, LastMinuteDealActivity.class);
			context.startActivity(intent);
			OmnitureTracking.trackLaunchLastMinuteDeal();
		}
	}

	public void addSyncListener() {
		getItinManager().addSyncListener(new ItinSyncListener());
	}

	protected ItineraryManager getItinManager() {
		return ItineraryManager.getInstance();
	}

	public MesoHotelAdViewModel getMesoHotelAdViewModel() {
		return mesoHotelAdViewModel;
	}

	public void setMesoHotelAdViewModel(MesoHotelAdViewModel mesoHotelAdViewModel) {
		this.mesoHotelAdViewModel = mesoHotelAdViewModel;
	}

	public MesoDestinationViewModel getMesoDestinationViewModel() {
		return mesoDestinationViewModel;
	}

	public void setMesoDestinationViewModel(MesoDestinationViewModel mesoDestinationViewModel) {
		this.mesoDestinationViewModel = mesoDestinationViewModel;
	}
}

