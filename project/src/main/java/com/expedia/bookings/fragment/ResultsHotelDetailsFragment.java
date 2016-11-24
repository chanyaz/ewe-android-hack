package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PaletteCallback;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelAvailability;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucket;
import com.expedia.bookings.dialog.VipBadgeClickListener;
import com.expedia.bookings.interfaces.IAddToBucketListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsHotelGalleryClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelReviewsClickedListener;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.ExpediaNetUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.HotelDetailsStickyHeaderLayout;
import com.expedia.bookings.widget.RingedCountView;
import com.expedia.bookings.widget.RowRoomRateLayout;
import com.expedia.bookings.widget.ScrollView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.TimingLogger;
import com.squareup.phrase.Phrase;

/**
 * ResultsHotelDetailsFragment: The hotel details / rooms and rates
 * fragment designed for tablet results 2013
 */
public class ResultsHotelDetailsFragment extends Fragment implements IBackManageable {

	public static ResultsHotelDetailsFragment newInstance() {
		return new ResultsHotelDetailsFragment();
	}

	private ViewGroup mRootC;
	private View mUserRatingContainer;
	private View mRoomsLeftContainer;
	private View mMobileExclusiveContainer;
	private HotelDetailsStickyHeaderLayout mHeaderContainer;
	private ViewGroup mAmenitiesContainer;
	private View mProgressContainer;
	private ViewGroup mReviewsC;
	private ScrollView mScrollView;
	private LinearLayout mRoomsRatesContainer;
	private ViewGroup mSoldOutContainer;

	private IAddToBucketListener mAddToBucketListener;
	private IResultsHotelReviewsClickedListener mHotelReviewsClickedListener;
	private IResultsHotelGalleryClickedListener mHotelGalleryClickedListener;

	private HotelOffersResponse mResponse;

	private GridManager mGrid = new GridManager();
	private Property mCurrentProperty;
	private int mSavedScrollPosition;

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			BackgroundDownloader.getInstance().cancelDownload(CrossContextHelper.KEY_INFO_DOWNLOAD);
			return false;
		}
	};

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mAddToBucketListener = Ui.findFragmentListener(this, IAddToBucketListener.class);
		mHotelReviewsClickedListener = Ui.findFragmentListener(this, IResultsHotelReviewsClickedListener.class);
		mHotelGalleryClickedListener = Ui.findFragmentListener(this, IResultsHotelGalleryClickedListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_hotel_details, null);
		mAmenitiesContainer = Ui.findView(mRootC, R.id.amenities_container);
		mUserRatingContainer = Ui.findView(mRootC, R.id.user_rating_container);
		mRoomsLeftContainer = Ui.findView(mRootC, R.id.rooms_left_container);
		mMobileExclusiveContainer = Ui.findView(mRootC, R.id.mobile_exclusive_container);
		mHeaderContainer = Ui.findView(mRootC, R.id.header_container);
		mProgressContainer = Ui.findView(mRootC, R.id.progress_spinner_container);
		mReviewsC = Ui.findView(mRootC, R.id.reviews_container);
		mScrollView = Ui.findView(mRootC, R.id.scrolling_content);
		mRoomsRatesContainer = Ui.findView(mRootC, R.id.rooms_rates_container);
		mSoldOutContainer = Ui.findView(mRootC, R.id.rooms_sold_out_container);
		toggleLoadingState(true);

		TextView soldOut = Ui.findView(mRootC, R.id.all_rooms_sold_out);
		soldOut.setText(
			Phrase.from(getActivity(), R.string.sorry_rooms_sold_out_TEMPLATE).put("brand", BuildConfig.brand)
				.format());
		return mRootC;
	}

	private OnClickListener mReviewsButtonClickedListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mHotelReviewsClickedListener.onHotelReviewsClicked();
		}
	};

	private OnClickListener mGalleryButtonClickedListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Property property = Db.getHotelSearch().getSelectedProperty();
			if (property != null && property.getMediaCount() > 0) {
				mHotelGalleryClickedListener.onHotelGalleryClicked();
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mBackManager.registerWithParent(this);
		mMeasurementHelper.registerWithProvider(this);
		Events.register(this);
		if (mSavedScrollPosition != 0) {
			mScrollView.scrollTo(0, mSavedScrollPosition);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mBackManager.unregisterWithParent(this);
		mMeasurementHelper.unregisterWithProvider(this);
		Events.unregister(this);
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}
		else {
			bd.unregisterDownloadCallback(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}
		// Let's save the scroll position, to restore it back on resume.
		saveScrollPosition();
	}

	/*
	 * No internet connection dialog
	 */

	public void onHotelSelected() {
		Property property = Db.getHotelSearch().getSelectedProperty();
		if (mCurrentProperty == null || !mCurrentProperty.equals(property)) {
			// We actually have work to do
			mCurrentProperty = property;
			scrollFragmentToTop();
			toggleLoadingState(true);
			prepareDetailsForInfo(mRootC, property);
			if (!ExpediaNetUtils.isOnline(getActivity())) {
				Events.post(new Events.ShowNoInternetDialog(SimpleCallbackDialogFragment.CODE_TABLET_NO_NET_CONNECTION_HOTEL_DETAILS));
				mCurrentProperty = null;
			}
			else {
				downloadDetails();
			}
		}

	}

	public int getTailHeight() {
		return getResources().getDimensionPixelSize(R.dimen.tablet_hotel_details_bottom_padding);
	}

	private void downloadDetails() {
		TimingLogger logger = new TimingLogger("ResultshotelDetailsFragment", "downloadDetails");
		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		if (TextUtils.isEmpty(selectedId)) {
			return;
		}
		logger.addSplit("Get and check selectedId");

		final BackgroundDownloader bd = BackgroundDownloader.getInstance();
		final String key = CrossContextHelper.KEY_INFO_DOWNLOAD;
		final HotelOffersResponse infoResponse = Db.getHotelSearch().getHotelOffersResponse(selectedId);
		logger.addSplit("Get downloader and old response");
		// Let's cancel existing download so we can start a new one or get cached response.
		bd.cancelDownload(key);

		// We may have been downloading the data elsewhere, so let's set the previous response.
		if (infoResponse != null) {
			// Load the data
			mInfoCallback.onDownload(infoResponse);
			logger.addSplit("mInfoCallback.onDownload(infoResponse)");
		}
		else {
			bd.startDownload(key, CrossContextHelper.getHotelOffersDownload(getActivity(), key), mInfoCallback);
			logger.addSplit("bd.startDownload");
		}
		logger.dumpToLog();
	}

	/**
	 * This could be called when not much information is available, or after
	 * details have been downloaded from e3. We should handle both cases gracefully
	 * in here.
	 */
	private void populateViews() {
		TimingLogger logger = new TimingLogger("ResultsHotelDetailsFragment", "populateViews");
		Property property = Db.getHotelSearch().getSelectedProperty();
		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		logger.addSplit("got property and selectedId");
		if (property != null) {
			setupHeader(mRootC, property);
			logger.addSplit("setupHeader");
			setupAmenities(mRootC, property);
			logger.addSplit("setupAmenities");
			setupRoomRates();
			logger.addSplit("setupRoomRates");
			setupDescriptionSections(mRootC, property);
			logger.addSplit("setupDescriptionSections");
			if (Db.getHotelSearch().getAvailability(selectedId) != null) {
				setDefaultSelectedRate();
				logger.addSplit("setDefaultSelectedRate");
			}
		}
		logger.dumpToLog();
	}

	public void setTransitionToAddTripPercentage(float percentage) {
		//TODO
		//		if (mAddToTripButton != null) {
		//			mAddToTripButton.setScaleY(1f - percentage);
		//		}


	}

	public void setTransitionToAddTripHardwareLayer(int layerType) {
		//TODO
		//		if (mAddToTripButton != null) {
		//			mAddToTripButton.setLayerType(layerType, null);
		//		}
	}

	private void prepareDetailsForInfo(View view, Property property) {
		View hotelImageTouchTarget = Ui.findView(view, R.id.hotel_header_image_touch_target);
		ImageView hotelImage = Ui.findView(view, R.id.hotel_header_image);
		TextView hotelName = Ui.findView(view, R.id.hotel_header_hotel_name);

		hotelImageTouchTarget.setOnClickListener(mGalleryButtonClickedListener);

		// Hotel Name
		hotelName.setText(property.getName());

		// Holder image, son
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelImagePlaceHolderDrawable);
		hotelImage.setImageResource(placeholderResId);

		RatingBar ratingBar;
		// Need to hide both sets
		ratingBar = Ui.findView(view, R.id.circle_rating_bar);
		ratingBar.setVisibility(View.GONE);

		ratingBar = Ui.findView(view, R.id.star_rating_bar);
		ratingBar.setVisibility(View.GONE);

		setupReviews(view, property);
	}

	private void setupHeader(View view, Property property) {
		final ImageView hotelImage = Ui.findView(view, R.id.hotel_header_image);
		TextView hotelName = Ui.findView(view, R.id.hotel_header_hotel_name);
		TextView notRatedText = Ui.findView(view, R.id.not_rated_text_view);
		RatingBar ratingBar;
		if (PointOfSale.getPointOfSale().shouldShowCircleForRatings()) {
			ratingBar = Ui.findView(view, R.id.circle_rating_bar);
		}
		else {
			ratingBar = Ui.findView(view, R.id.star_rating_bar);
		}
		View vipView = Ui.findView(view, R.id.vip_badge);

		// Hotel Name
		hotelName.setText(property.getName());

		// Hotel Image
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelImagePlaceHolderDrawable);
		if (property.getThumbnail() != null) {
			PaletteCallback mHeaderBitmapLoadedCallback = new PaletteCallback(hotelImage) {
				@Override
				public void onSuccess(int color) {
					mHeaderContainer.setDominantColor(color);
				}

				@Override
				public void onFailed() {
					mHeaderContainer.resetDominantColor();
				}
			};

			property.getThumbnail().fillImageView(hotelImage, placeholderResId, mHeaderBitmapLoadedCallback, null);
		}
		else {
			hotelImage.setImageResource(placeholderResId);
		}

		// VIP Badge
		boolean shouldShowVipIcon = PointOfSale.getPointOfSale().supportsVipAccess()
			&& property.isVipAccess();
		vipView.setVisibility(shouldShowVipIcon ? View.VISIBLE : View.GONE);
		vipView.setOnClickListener(new VipBadgeClickListener(getResources(), getFragmentManager()));

		// Star Rating
		float starRatingValue = (float) property.getHotelRating();
		if (starRatingValue == 0f) {
			ratingBar.setVisibility(View.GONE);
			notRatedText.setVisibility(View.VISIBLE);
		}
		else {
			ratingBar.setVisibility(View.VISIBLE);
			notRatedText.setVisibility(View.GONE);
			ratingBar.setRating(starRatingValue);
			// Adjust it so it looks centered
			float pct = 1 - starRatingValue / ratingBar.getNumStars();
			ratingBar.setTranslationX(pct * ratingBar.getWidth() / 2);
		}
	}

	private void setupReviews(View view, Property property) {

		// Reviews

		RingedCountView roomsLeftRing = Ui.findView(view, R.id.rooms_left_ring);
		TextView roomsLeftText = Ui.findView(view, R.id.rooms_left_ring_text);
		int totalReviews = property.getTotalReviews();
		boolean userRatingAvailable = totalReviews != 0;
		if (userRatingAvailable) {
			RatingBar userRating = Ui.findView(view, R.id.user_rating_bar);
			TextView userRatingText = Ui.findView(view, R.id.user_rating_text);
			mUserRatingContainer.setOnClickListener(mReviewsButtonClickedListener);
			mUserRatingContainer.setVisibility(View.VISIBLE);

			// User Rating
			float userRatingValue = (float) property.getAverageExpediaRating();
			userRating.setVisibility(View.VISIBLE);
			userRatingText.setVisibility(View.VISIBLE);
			userRating.setRating(userRatingValue);
			userRatingText.setText(getString(R.string.n_reviews_TEMPLATE, totalReviews));
		}
		else {
			mUserRatingContainer.setOnClickListener(null);
			mUserRatingContainer.setVisibility(View.GONE);
		}

		// Urgency / % Recommended

		Resources res = getResources();
		int roomsLeft = property.getRoomsLeftAtThisRate();
		boolean urgencyAvailable = true;
		if (property.isLowestRateMobileExclusive()) {
			mMobileExclusiveContainer.setVisibility(View.VISIBLE);
			mRoomsLeftContainer.setVisibility(View.GONE);
		}
		else if (property.isFromSearchByHotel()) {
			mMobileExclusiveContainer.setVisibility(View.GONE);
			mRoomsLeftContainer.setVisibility(View.VISIBLE);
			int color = ContextCompat.getColor(view.getContext(), R.color.details_ring_blue);
			roomsLeftRing.setVisibility(View.VISIBLE);
			roomsLeftText.setVisibility(View.VISIBLE);
			roomsLeftRing.setPrimaryColor(color);
			roomsLeftRing.setCountTextColor(color);
			roomsLeftRing.setPercent(property.getPercentRecommended() / 100f);
			roomsLeftRing.setCountText("");
			roomsLeftText.setText(res.getString(R.string.n_recommend_TEMPLATE, property.getPercentRecommended()));
		}
		else if (roomsLeft <= 5 && roomsLeft >= 0) {
			mMobileExclusiveContainer.setVisibility(View.GONE);
			mRoomsLeftContainer.setVisibility(View.VISIBLE);
			int color = ContextCompat.getColor(view.getContext(), R.color.details_ring_red);
			roomsLeftRing.setVisibility(View.VISIBLE);
			roomsLeftText.setVisibility(View.VISIBLE);
			roomsLeftRing.setPrimaryColor(color);
			roomsLeftRing.setCountTextColor(color);
			roomsLeftRing.setPercent(roomsLeft / 10f);
			roomsLeftRing.setCountText("");
			roomsLeftText.setText(res.getQuantityString(R.plurals.n_rooms_left_TEMPLATE, roomsLeft, roomsLeft));
		}
		else if (property.getPercentRecommended() != 0f) {
			mMobileExclusiveContainer.setVisibility(View.GONE);
			mRoomsLeftContainer.setVisibility(View.VISIBLE);
			roomsLeftRing.setVisibility(View.VISIBLE);
			roomsLeftText.setVisibility(View.VISIBLE);
			roomsLeftRing.setPrimaryColor(ContextCompat.getColor(view.getContext(), R.color.details_ring_blue));
			roomsLeftRing.setPercent(property.getPercentRecommended() / 100f);
			roomsLeftRing.setCountText("");
			int percent = Math.round(property.getPercentRecommended());
			roomsLeftText.setText(getString(R.string.n_recommend_TEMPLATE, percent));
		}
		else {
			mMobileExclusiveContainer.setVisibility(View.GONE);
			mRoomsLeftContainer.setVisibility(View.GONE);
			urgencyAvailable = false;
		}

		// Show/hide views/containers as appropriate

		if (urgencyAvailable && userRatingAvailable) {
			mReviewsC.setVisibility(View.VISIBLE);
			Ui.findView(mReviewsC, R.id.reviews_section_divider).setVisibility(View.VISIBLE);
		}
		else if (urgencyAvailable || userRatingAvailable) {
			mReviewsC.setVisibility(View.VISIBLE);
			Ui.findView(mReviewsC, R.id.reviews_section_divider).setVisibility(View.GONE);
		}
		else {
			mReviewsC.setVisibility(View.GONE);
		}
	}

	private void setupAmenities(View view, Property property) {
		// Disable some aspects of the horizontal scrollview so it looks pretty
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) view.findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);
		amenitiesScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

		ViewGroup amenitiesTableRow = Ui.findView(view, R.id.amenities_table_row);
		amenitiesTableRow.removeAllViews();

		if (property.hasAmenities()) {
			// Center the amenities if they don't take up the full width
			float amenitiesWidth = LayoutUtils.estimateAmenitiesWidth(getActivity(), property);
			float desiredPadding = (mGrid.getColWidth(1) - amenitiesWidth) / 2;
			float minPadding = 0;
			int padding = (int) Math.max(minPadding, desiredPadding);
			mAmenitiesContainer.setPadding(padding, 0, 0, 0);

			LayoutUtils.addAmenities(getActivity(), property, amenitiesTableRow);
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.VISIBLE);
			view.findViewById(R.id.amenities_none_text).setVisibility(View.GONE);
		}
		else {
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.GONE);
			mAmenitiesContainer.setVisibility(View.GONE);
		}
	}

	private OnClickListener mRateClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Search up through view hierarchy until we find the RowRoomRateLayout
			// that this view is a descendant of (worst case only 3 or 4 parents).
			while (!(v == null || v instanceof RowRoomRateLayout)) {
				ViewParent p = v.getParent();
				v = p instanceof View ? (View) p : null;
			}
			if (v != null) {
				RowRoomRateLayout row = (RowRoomRateLayout) v;
				Rate clickedRate = row.getRate();
				// Let's set and bind the roomRate only when a new roomRate is clicked.
				Rate selectedRate = Db.getHotelSearch().getSelectedRate();
				if (selectedRate == null || !selectedRate.equals(clickedRate)) {
					setSelectedRate(clickedRate);
				}
			}
		}
	};

	private OnClickListener mAddRoomClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			addSelectedRoomToTrip();
		}
	};

	private void setupRoomRates() {
		mRoomsRatesContainer.removeAllViews();

		if (mResponse == null) {
			return;
		}

		List<Rate> rates = mResponse.getRates();

		if (rates != null && rates.size() > 0) {
			mSoldOutContainer.setVisibility(View.GONE);
			mRoomsRatesContainer.setVisibility(View.VISIBLE);

			LinearLayout descriptionsContainer = Ui.findView(mRootC, R.id.description_details_sections_container);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) descriptionsContainer.getLayoutParams();
			params.addRule(RelativeLayout.BELOW, mRoomsRatesContainer.getId());

			// TODO: I wonder if we should use RoomsAndRatesAdapter, or similar
			for (Rate rate : rates) {
				RowRoomRateLayout row = Ui.inflate(R.layout.row_tablet_room_rate, mRoomsRatesContainer, false);
				row.bind(rate, mResponse.getCommonValueAdds(), mRateClickListener, mAddRoomClickListener);

				mRoomsRatesContainer.addView(row);

				// Separator
				View sep = Ui.inflate(R.layout.row_tablet_room_rate_separator, mRoomsRatesContainer, false);
				mRoomsRatesContainer.addView(sep);
			}
		}
		else {
			mSoldOutContainer.setVisibility(View.VISIBLE);
			mRoomsRatesContainer.setVisibility(View.GONE);

			LinearLayout descriptionsContainer = Ui.findView(mRootC, R.id.description_details_sections_container);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) descriptionsContainer.getLayoutParams();
			params.addRule(RelativeLayout.BELOW, mSoldOutContainer.getId());
		}
	}

	private void toggleLoadingState(boolean enable) {
		LinearLayout descriptionsContainer = Ui.findView(mRootC, R.id.description_details_sections_container);
		if (enable) {
			mProgressContainer.setVisibility(View.VISIBLE);
			descriptionsContainer.setVisibility(View.GONE);
			mAmenitiesContainer.setVisibility(View.INVISIBLE);
			mRoomsRatesContainer.setVisibility(View.GONE);
		}
		else {
			mProgressContainer.setVisibility(View.VISIBLE);
			descriptionsContainer.setVisibility(View.VISIBLE);
			mAmenitiesContainer.setVisibility(View.VISIBLE);
			mRoomsRatesContainer.setVisibility(View.VISIBLE);

			long duration = getResources().getInteger(android.R.integer.config_longAnimTime);

			ObjectAnimator.ofFloat(mAmenitiesContainer, "alpha", 0.0f, 1.0f) //
				.setDuration(duration) //
				.start();
			ObjectAnimator.ofFloat(mRoomsRatesContainer, "alpha", 0.0f, 1.0f) //
				.setDuration(duration) //
				.start();
			ObjectAnimator.ofFloat(descriptionsContainer, "alpha", 0.0f, 1.0f) //
				.setDuration(duration) //
				.start();

			ObjectAnimator a = ObjectAnimator.ofFloat(mProgressContainer, "alpha", 1.0f, 0.0f) //
				.setDuration(duration);
			a.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressContainer.setVisibility(View.GONE);
				}
			});
			a.start();
		}
	}

	/**
	 * Makes the selection in the UI matches the selected rate in Db.
	 */
	private void setDefaultSelectedRate() {
		String propertyId = Db.getHotelSearch().getSelectedPropertyId();
		HotelAvailability availability = Db.getHotelSearch().getAvailability(propertyId);
		Rate selectedRate = availability == null ? null : availability.getSelectedRate();
		if (selectedRate == null) {
			if (mResponse == null || mResponse.getRates() == null || mResponse.getRateCount() == 0) {
				selectedRate = Db.getHotelSearch().getSelectedProperty().getLowestRate();
			}
			else {
				selectedRate = mResponse.getRates().get(0);
			}
		}

		Db.getHotelSearch().setSelectedRate(selectedRate);

		setSelectedRateNonAnimated(selectedRate);
	}

	private RowRoomRateLayout setSelectedRateNonAnimated(Rate selectedRate) {
		RowRoomRateLayout selectedRow = null;

		int selectedChildIndex = -1;
		for (int i = 0; i < mRoomsRatesContainer.getChildCount(); i++) {
			View child = mRoomsRatesContainer.getChildAt(i);
			child.setVisibility(View.VISIBLE);
			if (child instanceof RowRoomRateLayout) {
				final RowRoomRateLayout row = (RowRoomRateLayout) child;
				boolean isSelected = row.getRate().equals(selectedRate);
				row.setSelected(isSelected, true);
				if (isSelected) {
					selectedRow = row;
					selectedChildIndex = i;
				}
			}
		}
		// Show/hide row separators appropriately
		if (selectedChildIndex > 0) {
			mRoomsRatesContainer.getChildAt(selectedChildIndex - 1).setVisibility(View.INVISIBLE);
		}
		if (selectedChildIndex < mRoomsRatesContainer.getChildCount() - 1) {
			mRoomsRatesContainer.getChildAt(selectedChildIndex + 1).setVisibility(View.INVISIBLE);
		}

		return selectedRow;
	}

	/**
	 * Called in response to a user click on a different room rate. Makes sure the
	 * checkmark and relative prices are all in sync.
	 */
	private void setSelectedRate(final Rate selectedRate) {
		Db.getHotelSearch().setSelectedRate(selectedRate);

		// Store original positions of children of Rooms & Rates layout
		final HashMap<View, int[]> oldCoordinates = new HashMap<>();
		for (int i = mRoomsRatesContainer.getChildCount() - 1; i >= 0; i--) {
			View v = mRoomsRatesContainer.getChildAt(i);
			v.setHasTransientState(true);
			oldCoordinates.put(v, new int[] {v.getTop(), v.getBottom()});
		}

		final HashMap<View, int[]> newCoordinates = new HashMap<>();

		final RowRoomRateLayout fSelectedRow = setSelectedRateNonAnimated(selectedRate);

		/* What is this whole "pass" thing anyway?
		 * The idea is that every re-layout triggers another draw. We want to intercept these
		 * draw events to make sure the animation flows like butter.
		 *
		 * In the section above marked "Do expand/contract", we resize each hotel room rate
		 * (one shrinks, one expands).
		 *
		 * Pass 0: After the expansion/contraction, we want to stop the actual screen draw, and just
		 * take note of where on the screen each row ended up.
		 *
		 * The trick we're using is to animate setTop and setBottom starting from their original
		 * positions (oldCoordinates) to their new positions (newCoordinates). We haven't done any
		 * complicated math because we just let the layout system arrange them normally and then
		 * hijack before they're drawn.
		 *
		 * Pass >= 1: We'll monitor the draws carefully, because sometimes a re-layout happens
		 * that's not caused by our animation, thereby janking up the animation. Let's block these
		 * drawing events from happening (by returning false from the OnPreDrawListener).
		 */
		mRootC.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			int pass = 0;
			boolean animating = true;

			ArrayList<Animator> animations = new ArrayList<>();

			@Override
			public boolean onPreDraw() {
				switch (pass++) {
				case 0: {
					// Also animate mScrollView.setScrollY so that the newly expanded rate
					// moves to the top of the fragment
					int scrolly = mRoomsRatesContainer.getTop() + fSelectedRow.getTop()
						- getResources().getDimensionPixelOffset(R.dimen.tablet_details_compact_header_height);
					animations.add(ObjectAnimator.ofInt(mScrollView, "scrollY", scrolly));

					// Animate each rate row's setTop and setBottom from its old position
					// (before the expand/collapse) to its new position (the position it's at now
					// after one layout pass has taken place).
					for (View v : oldCoordinates.keySet()) {
						int[] old = oldCoordinates.get(v);
						int oldtop = old[0];
						int oldbot = old[1];
						int newtop = v.getTop();
						int newbot = v.getBottom();

						if (oldtop != newtop || oldbot != newbot) {
							PropertyValuesHolder translationTop = PropertyValuesHolder.ofInt("top", oldtop, newtop);
							PropertyValuesHolder translationBottom = PropertyValuesHolder.ofInt("bottom", oldbot, newbot);

							Animator anim = ObjectAnimator.ofPropertyValuesHolder(v, translationTop, translationBottom);

							newCoordinates.put(v, new int[] {newtop, newbot});
							animations.add(anim);
							if (v instanceof RowRoomRateLayout) {
								((RowRoomRateLayout) v).setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
							}
						}
					}

					// Disabled the ListView for the duration of the animation.
					mRoomsRatesContainer.setEnabled(false);
					mRoomsRatesContainer.setClickable(false);

					// Play all the animations created above together at the same time.
					AnimatorSet s = new AnimatorSet();
					s.playTogether(animations);
					s.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							animating = false;
							mRoomsRatesContainer.setEnabled(true);
							mRoomsRatesContainer.setClickable(true);
							for (int i = mRoomsRatesContainer.getChildCount() - 1; i >= 0; i--) {
								View v = mRoomsRatesContainer.getChildAt(i);
								v.setHasTransientState(false);
							}
						}
					});
					s.start();
					return false;
				}

				default: {
					if (!animating) {
						mRootC.getViewTreeObserver().removeOnPreDrawListener(this);
						return true;
					}

					// Sometimes, some system-caused layouts break this setTop/setBottom animation.
					// So, skip the drawing passes where views are drawn out-of-line with the animation.
					for (View v : newCoordinates.keySet()) {
						int[] oldvalue = oldCoordinates.get(v);
						int[] newvalue = newCoordinates.get(v);
						if (!between(v.getTop(), oldvalue[0], newvalue[0])
							|| !between(v.getBottom(), oldvalue[1], newvalue[1])) {
							mRoomsRatesContainer.invalidate();
							return false;
						}
					}
				}
				}

				return true;
			}
		});

	}

	// Returns true if v is between a (inclusive) and b (exclusive)
	private boolean between(int v, int a, int b) {
		return a == b
			? a == v
			: a < b
			? a <= v && v < b
			: b < v && v <= a;
	}

	private void setupDescriptionSections(View view, Property property) {
		LinearLayout container = Ui.findView(view, R.id.description_details_sections_container);
		container.removeAllViews();

		List<HotelTextSection> sections = property.getAllHotelText();

		if (sections != null && sections.size() > 0) {
			for (int i = 0; i < sections.size(); i++) {
				HotelTextSection section = sections.get(i);
				View sectionContainer = Ui.inflate(R.layout.include_hotel_description_section,
					container, false);

				TextView titleText = Ui.findView(sectionContainer, R.id.title_text);
				TextView bodyText = Ui.findView(sectionContainer, R.id.body_text);
				// Hide the section title for the first description section
				if (i == 0) {
					titleText.setVisibility(View.GONE);
				}
				else {
					titleText.setVisibility(View.VISIBLE);
					titleText.setText(section.getNameWithoutHtml());
				}
				bodyText.setText(HtmlCompat.fromHtml(section.getContentFormatted(getActivity())));
				container.addView(sectionContainer);
			}
		}
	}

	// Silently don't do anything if our data in Db is messed up.
	private void addSelectedRoomToTrip() {
		scrollFragmentToTop();

		HotelSearch search = Db.getHotelSearch();
		if (search == null) {
			return;
		}

		Property property = search.getSelectedProperty();
		if (property == null) {
			return;
		}

		Rate rate = search.getSelectedRate();
		if (rate == null) {
			rate = property.getLowestRate();
			if (rate == null) {
				return;
			}
		}

		HotelAvailability availability = search.getAvailability(property.getPropertyId());
		if (availability == null) {
			return;
		}

		TripBucket bucket = Db.getTripBucket();
		bucket.clearHotel();
		bucket.add(search, rate, availability);
		Db.saveTripBucket(getActivity());

		mAddToBucketListener.onItemAddedToBucket();
	}

	private void scrollFragmentToTop() {
		mScrollView.scrollTo(0, 0);
	}

	public void saveScrollPosition() {
		mSavedScrollPosition = mScrollView.getScrollY();
	}

	public void setScrollBetweenSavedAndHeader(float percentage) {
		float header = getResources().getDimension(R.dimen.tablet_reviews_header_height);
		float image = getResources().getDimension(R.dimen.hotel_header_height);
		float shift = image - header;
		float target = (mSavedScrollPosition - shift) * -percentage + mSavedScrollPosition;
		mScrollView.scrollTo(0, (int) target);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Async loading of ExpediaServices.availability

	private final OnDownloadComplete<HotelOffersResponse> mInfoCallback = new OnDownloadComplete<HotelOffersResponse>() {
		@Override
		public void onDownload(HotelOffersResponse response) {
			HotelSearch search = Db.getHotelSearch();

			// Check if we got a better response elsewhere before loading up this data
			String selectedId = search.getSelectedPropertyId();
			HotelOffersResponse possibleBetterResponse = search.getHotelOffersResponse(selectedId);

			if (possibleBetterResponse != null) {
				response = possibleBetterResponse;
			}
			else {
				search.updateFrom(response);
			}

			if (response == null) {
				String message = Phrase
					.from(getActivity(), R.string.e3_error_hotel_offers_hotel_service_failure_TEMPLATE)
					.put("brand", BuildConfig.brand).format().toString();
				Log.w(message);
				return;
			}
			else if (response.hasErrors()) {
				if (response.isHotelUnavailable()) {
					String message = Phrase.from(getActivity(), R.string.error_hotel_is_now_sold_out_TEMPLATE)
						.put("brand", BuildConfig.brand).format().toString();
					Log.w(message);
				}
				else {
					String message = Phrase
						.from(getActivity(), R.string.e3_error_hotel_offers_hotel_service_failure_TEMPLATE)
						.put("brand", BuildConfig.brand).format().toString();
					Log.w(message);
				}
			}
			else if (search.getAvailability(selectedId) != null && search.getSearchParams() != null
				&& search.getAvailability(selectedId).getRateCount() == 0
				&& search.getSearchParams().getSearchType() != SearchType.HOTEL) {
				Log.w(Phrase.from(getActivity(), R.string.error_hotel_is_now_sold_out_TEMPLATE)
					.put("brand", BuildConfig.brand).format().toString());
			}

			// Notify affected child fragments to refresh.

			mResponse = response;
			toggleLoadingState(false);
			populateViews();
			Events.post(new Events.HotelAvailabilityUpdated());
		}
	};


	/*
	MEASUREMENT HELPER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			if (isLandscape) {
				//This attempts to replicate the global layout by doing 3 columns for our general results layout
				//and 2 rows (where the first one represents the actionbar).
				GridManager globalGm = new GridManager(2, 3);
				globalGm.setDimensions(totalWidth, totalHeight);
				globalGm.setRowSize(0, getActivity().getActionBar().getHeight());

				//Now we set up our local positions
				mGrid.setDimensions(globalGm.getColSpanWidth(1, 3), globalGm.getRowHeight(1));
				mGrid.setNumCols(3);
				mGrid.setNumRows(3);

				Resources res = getResources();
				int topSpaceSize = res.getDimensionPixelSize(R.dimen.tablet_hotel_details_top_padding);
				int bottomSpaceSize = res.getDimensionPixelSize(R.dimen.tablet_hotel_details_bottom_padding);
				float horizSpacePercentage = res.getFraction(R.fraction.tablet_hotel_details_horizontal_spacing_percentage, 1, 1);
				mGrid.setRowSize(0, topSpaceSize);
				mGrid.setRowSize(2, bottomSpaceSize);
				mGrid.setColumnPercentage(0, horizSpacePercentage);
				mGrid.setColumnPercentage(2, horizSpacePercentage);

				mGrid.setContainerToRow(mRootC, 1);
				mGrid.setContainerToColumn(mRootC, 1);
			}
			else {
				//This attempts to replicate the global layout by doing 2 columns for our general results layout
				//and 2 rows (where the first one represents the actionbar).
				GridManager globalGm = new GridManager(2, 2);
				globalGm.setDimensions(totalWidth, totalHeight);
				globalGm.setRowSize(0, getActivity().getActionBar().getHeight());

				//Now we set up our local positions
				mGrid.setDimensions(globalGm.getColSpanWidth(0, 2), globalGm.getRowHeight(1));
				mGrid.setNumCols(3);
				mGrid.setNumRows(3);

				Resources res = getResources();
				int topSpaceSize = res.getDimensionPixelSize(R.dimen.tablet_hotel_details_top_padding);
				int bottomSpaceSize = res.getDimensionPixelSize(R.dimen.tablet_hotel_details_bottom_padding);
				float horizSpacePercentage = res.getFraction(R.fraction.tablet_hotel_details_horizontal_spacing_percentage, 1, 1);
				mGrid.setRowSize(0, topSpaceSize);
				mGrid.setRowSize(2, bottomSpaceSize);
				mGrid.setColumnPercentage(0, horizSpacePercentage);
				mGrid.setColumnPercentage(2, horizSpacePercentage);

				mGrid.setContainerToRow(mRootC, 1);
				mGrid.setContainerToColumn(mRootC, 1);
			}
		}
	};
}
