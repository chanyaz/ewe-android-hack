package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.bitmaps.BitmapUtils;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.BedType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelAvailability;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.interfaces.IAddToBucketListener;
import com.expedia.bookings.interfaces.IResultsHotelGalleryClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelReviewsClickedListener;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.HotelDetailsStickyHeaderLayout;
import com.expedia.bookings.widget.RingedCountView;
import com.expedia.bookings.widget.RowRoomRateLayout;
import com.expedia.bookings.widget.ScrollView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.TimingLogger;

/**
 * ResultsHotelDetailsFragment: The hotel details / rooms and rates
 * fragment designed for tablet results 2013
 */
public class ResultsHotelDetailsFragment extends Fragment {

	private int ROOM_RATE_ANIMATION_DURATION = 300;

	private boolean mIsDescriptionTextSpanned;
	private boolean mDoReScroll;

	public static ResultsHotelDetailsFragment newInstance() {
		ResultsHotelDetailsFragment frag = new ResultsHotelDetailsFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private View mUserRatingContainer;
	private View mRoomsLeftContainer;
	private View mMobileExclusiveContainer;
	private HotelDetailsStickyHeaderLayout mHeaderContainer;
	private ViewGroup mAmenitiesContainer;
	private LinearLayout mRatesContainer;
	private View mProgressContainer;
	private ViewGroup mReviewsC;
	private ScrollView mScrollView;

	private IAddToBucketListener mAddToBucketListener;
	private IResultsHotelReviewsClickedListener mHotelReviewsClickedListener;
	private IResultsHotelGalleryClickedListener mHotelGalleryClickedListener;

	private HotelOffersResponse mResponse;

	private GridManager mGrid = new GridManager();
	private Property mCurrentProperty;
	private int mSavedScrollPosition;

	private static final int ROOM_COUNT_URGENCY_CUTOFF = 5;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAddToBucketListener = Ui.findFragmentListener(this, IAddToBucketListener.class);
		mHotelReviewsClickedListener = Ui.findFragmentListener(this, IResultsHotelReviewsClickedListener.class);
		mHotelGalleryClickedListener = Ui.findFragmentListener(this, IResultsHotelGalleryClickedListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotel_details, null);
		mAmenitiesContainer = Ui.findView(mRootC, R.id.amenities_container);
		mRatesContainer = Ui.findView(mRootC, R.id.rooms_rates_container);
		mUserRatingContainer = Ui.findView(mRootC, R.id.user_rating_container);
		mRoomsLeftContainer = Ui.findView(mRootC, R.id.rooms_left_container);
		mMobileExclusiveContainer = Ui.findView(mRootC, R.id.mobile_exclusive_container);
		mHeaderContainer = Ui.findView(mRootC, R.id.header_container);
		mProgressContainer = Ui.findView(mRootC, R.id.progress_spinner_container);
		mUserRatingContainer = Ui.findView(mRootC, R.id.user_rating_container);
		mReviewsC = Ui.findView(mRootC, R.id.reviews_container);
		mScrollView = Ui.findView(mRootC, R.id.scrolling_content);
		toggleLoadingState(true);
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
			mHotelGalleryClickedListener.onHotelGalleryClicked();
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
		if (mSavedScrollPosition != 0) {
			mScrollView.scrollTo(0, mSavedScrollPosition);
		}
	}

	public void onHotelSelected() {
		Property property = Db.getHotelSearch().getSelectedProperty();
		if (mCurrentProperty == null || !mCurrentProperty.equals(property)) {
			// We actually have work to do
			mCurrentProperty = property;
			scrollFragmentToTop();
			toggleLoadingState(true);
			prepareDetailsForInfo(mRootC, property);
			downloadDetails();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mMeasurementHelper.unregisterWithProvider(this);
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

	public int getTailHeight() {
		return getResources().getDimensionPixelSize(R.dimen.tablet_hotel_details_vertical_padding);
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

		if (infoResponse != null) {
			// We may have been downloading the data here before getting it elsewhere, so cancel
			// our own download once we have data
			bd.cancelDownload(key);

			// Load the data
			mInfoCallback.onDownload(infoResponse);
			logger.addSplit("mInfoCallback.onDownload(infoResponse)");

		}
		else if (bd.isDownloading(key)) {
			bd.registerDownloadCallback(key, mInfoCallback);
			logger.addSplit("bd.registerDownloadCallback(key, mInfoCallback)");
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
			setupRoomRates(mRootC, property);
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
		ImageView hotelImage = Ui.findView(view, R.id.hotel_header_image);
		TextView hotelName = Ui.findView(view, R.id.hotel_header_hotel_name);

		hotelImage.setOnClickListener(mGalleryButtonClickedListener);

		// Hotel Name
		hotelName.setText(property.getName());

		// Holder image, son
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.hotelImagePlaceHolderDrawable);
		hotelImage.setImageResource(placeholderResId);

		RatingBar starRating = Ui.findView(view, R.id.star_rating_bar);
		starRating.setVisibility(View.INVISIBLE);

		TextView saleText = Ui.findView(view, R.id.sale_text_view);
		saleText.setVisibility(View.GONE);

		setupReviews(view, property);
	}

	private void setupHeader(View view, Property property) {
		ImageView hotelImage = Ui.findView(view, R.id.hotel_header_image);
		TextView hotelName = Ui.findView(view, R.id.hotel_header_hotel_name);
		TextView notRatedText = Ui.findView(view, R.id.not_rated_text_view);
		RatingBar starRating = Ui.findView(view, R.id.star_rating_bar);
		TextView vipText = Ui.findView(view, R.id.vip_badge);
		TextView saleText = Ui.findView(view, R.id.sale_text_view);

		// Hotel Name
		hotelName.setText(property.getName());

		// Hotel Image
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.hotelImagePlaceHolderDrawable);
		if (property.getThumbnail() != null) {
			property.getThumbnail().fillImageView(hotelImage, placeholderResId, mHeaderBitmapLoadedCallback);
		}
		else {
			hotelImage.setImageResource(placeholderResId);
		}

		// VIP Badge
		boolean shouldShowVipIcon = PointOfSale.getPointOfSale().supportsVipAccess()
			&& property.isVipAccess();
		vipText.setVisibility(shouldShowVipIcon ? View.VISIBLE : View.GONE);

		// "25% OFF"
		Rate rate = property.getLowestRate();
		if (rate.isOnSale() && rate.isSaleTenPercentOrBetter()) {
			saleText.setVisibility(View.VISIBLE);
			saleText.setText(getString(R.string.x_percent_OFF_TEMPLATE,
				rate.getDiscountPercent()));
		}
		else {
			saleText.setVisibility(View.GONE);
		}

		// Star Rating
		float starRatingValue = (float) property.getHotelRating();
		if (starRatingValue == 0f) {
			starRating.setVisibility(View.GONE);
			notRatedText.setVisibility(View.VISIBLE);
		}
		else {
			starRating.setVisibility(View.VISIBLE);
			notRatedText.setVisibility(View.GONE);
			starRating.setRating(starRatingValue);
			// Adjust it so it looks centered
			float pct = 1 - starRatingValue / starRating.getNumStars();
			starRating.setTranslationX(pct * starRating.getWidth() / 2);
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
		else if (roomsLeft <= 5 && roomsLeft >= 0) {
			mMobileExclusiveContainer.setVisibility(View.GONE);
			mRoomsLeftContainer.setVisibility(View.VISIBLE);
			int color = res.getColor(R.color.details_ring_red);
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
			roomsLeftRing.setPrimaryColor(res.getColor(R.color.details_ring_blue));
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

		// Center the amenities if they don't take up the full width
		float amenitiesWidth = LayoutUtils.estimateAmenitiesWidth(getActivity(), property);
		float desiredPadding = (mGrid.getColWidth(1) - amenitiesWidth) / 2;
		float minPadding = 0;
		int padding = (int) Math.max(minPadding, desiredPadding);
		mAmenitiesContainer.setPadding(padding, 0, 0, 0);

		LayoutUtils.addAmenities(getActivity(), property, amenitiesTableRow);

		// Hide the text that indicated no amenities because there are amenities
		view.findViewById(R.id.amenities_none_text).setVisibility(View.GONE);
		if (property.hasAmenities()) {
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.VISIBLE);
		}
		else {
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.GONE);
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
				if (!Db.getHotelSearch().getSelectedRate().equals(clickedRate)) {
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

	private void setupRoomRates(View view, Property property) {
		mRatesContainer.removeAllViews();

		if (mResponse == null) {
			return;
		}

		List<Rate> rates = mResponse.getRates();

		// TODO: I wonder if we should use RoomsAndRatesAdapter, or similar
		boolean first = true;
		LayoutInflater inflater = getActivity().getLayoutInflater();
		for (Rate rate : rates) {
			RowRoomRateLayout row = (RowRoomRateLayout) inflater.inflate(
				R.layout.row_tablet_room_rate, mRatesContainer, false);
			row.setRate(rate);
			row.setOnClickListener(mRateClickListener);
			TextView description = Ui.findView(row, R.id.text_room_description);
			TextView pricePerNight = Ui.findView(row, R.id.text_price_per_night);
			TextView bedType = Ui.findView(row, R.id.text_bed_type);

			// Separator
			if (!first) {
				View sep = inflater.inflate(R.layout.row_tablet_room_rate_separator, mRatesContainer, false);
				mRatesContainer.addView(sep);
			}

			// Description
			description.setText(rate.getRoomDescription());

			Set<BedType> bedTypes = rate.getBedTypes();
			if (bedTypes.iterator().hasNext()) {
				bedType.setVisibility(View.VISIBLE);
				bedType.setText(bedTypes.iterator().next().getBedTypeDescription());
			}

			String formattedRoomRate = rate.getDisplayPrice().getFormattedMoney(Money.F_NO_DECIMAL);
			pricePerNight.setText(Html.fromHtml(getString(R.string.room_rate_per_night_template, formattedRoomRate)));

			mRatesContainer.addView(row);
			first = false;
		}
	}

	private boolean mCurrentlyShowingLoadingProgress = false;
	private void toggleLoadingState(boolean enable) {
		LinearLayout descriptionsContainer = Ui.findView(mRootC, R.id.description_details_sections_container);
		if (enable) {
			mProgressContainer.setVisibility(View.VISIBLE);
			descriptionsContainer.setVisibility(View.GONE);
			mAmenitiesContainer.setVisibility(View.INVISIBLE);
			mRatesContainer.setVisibility(View.GONE);
		}
		else {
			mProgressContainer.setVisibility(View.VISIBLE);
			descriptionsContainer.setVisibility(View.VISIBLE);
			mAmenitiesContainer.setVisibility(View.VISIBLE);
			mRatesContainer.setVisibility(View.VISIBLE);

			long duration = getResources().getInteger(android.R.integer.config_longAnimTime);

			ObjectAnimator.ofFloat(mAmenitiesContainer, "alpha", 0.0f, 1.0f) //
				.setDuration(duration) //
				.start();
			ObjectAnimator.ofFloat(mRatesContainer, "alpha", 0.0f, 1.0f) //
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
		Rate rate = availability == null ? null : availability.getSelectedRate();
		if (rate == null) {
			if (mResponse == null || mResponse.getRates() == null || mResponse.getRateCount() == 0) {
				rate = Db.getHotelSearch().getSelectedProperty().getLowestRate();
			}
			else {
				rate = mResponse.getRates().get(0);
			}
		}

		mDoReScroll = false;
		setSelectedRate(rate);
	}

	/**
	 * Called in response to a user click on a different room rate. Makes sure the
	 * checkmark and relative prices are all in sync.
	 *
	 * @param selectedRate
	 */
	private void setSelectedRate(final Rate selectedRate) {
		Db.getHotelSearch().setSelectedRate(selectedRate);

		LinearLayout container = Ui.findView(getView(), R.id.rooms_rates_container);
		for (int i = 0; i < container.getChildCount(); i++) {
			// Skip if this is a separator row
			if (!(container.getChildAt(i) instanceof RowRoomRateLayout)) {
				continue;
			}

			RowRoomRateLayout row = (RowRoomRateLayout) container.getChildAt(i);
			final Rate rowRate = row.getRate();

			RelativeLayout roomRateDetailContainer = Ui.findView(row, R.id.room_rate_detail_container);
			final Button addRoomButton = Ui.findView(row, R.id.room_rate_button_add);
			final Button selectRoomButton = Ui.findView(row, R.id.room_rate_button_select);

			addRoomButton.setOnClickListener(mAddRoomClickListener);
			selectRoomButton.setOnClickListener(mRateClickListener);

			// Let's the width of both these buttons match.
			final ViewTreeObserver vto = addRoomButton.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					int addButtonWidth = addRoomButton.getMeasuredWidth();
					int selectButtonWidth = selectRoomButton.getMeasuredWidth();
					if (addButtonWidth == selectButtonWidth) {
						return;
					}
					else if (addButtonWidth > selectButtonWidth) {
						selectRoomButton.setWidth(addButtonWidth);
					}
					else {
						addRoomButton.setWidth(selectButtonWidth);
					}
				}
			});


			// Show renovation fees notice
			LinearLayout renovationNoticeContainer = Ui.findView(row, R.id.room_rate_renovation_container);
			Property property = Db.getHotelSearch().getSelectedProperty();
			final String renovationText;
			if (property.getRenovationText() != null && !TextUtils.isEmpty(property.getRenovationText().getContent())) {
				renovationNoticeContainer.setVisibility(View.VISIBLE);
				renovationText = property.getRenovationText().getContent();
				Ui.findView(row, R.id.room_rate_renovation_more_info).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						openWebView(getString(R.string.renovation_notice), renovationText);
					}
				});
			}
			else {
				renovationNoticeContainer.setVisibility(View.GONE);
			}

			// Show resort fees notice
			LinearLayout resortFeesContainer = Ui.findView(row, R.id.room_rate_resort_fees_container);
			Money mandatoryFees = selectedRate == null ? null : selectedRate.getTotalMandatoryFees();
			boolean hasMandatoryFees = mandatoryFees != null && !mandatoryFees.isZero();
			boolean hasResortFeesMessage = property.getMandatoryFeesText() != null
				&& !TextUtils.isEmpty(property.getMandatoryFeesText().getContent());
			boolean mandatoryFeePriceType =
				selectedRate.getCheckoutPriceType() == Rate.CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES;

			final String resortFeesMoreInfoText;
			if (hasMandatoryFees && hasResortFeesMessage && !mandatoryFeePriceType) {
				com.expedia.bookings.widget.TextView resortFeesNoticeText = Ui.findView(row,
					R.id.room_rate_resort_fees_text);
				resortFeesNoticeText.setText(Html.fromHtml(
					getString(R.string.tablet_room_rate_resort_fees_template, mandatoryFees.getFormattedMoney())));
				resortFeesContainer.setVisibility(View.VISIBLE);
				resortFeesMoreInfoText = property.getMandatoryFeesText().getContent();
				Ui.findView(row, R.id.room_rate_resort_fees_more_info).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						openWebView(getString(R.string.additional_fees), resortFeesMoreInfoText);
					}
				});
			}
			else {
				resortFeesContainer.setVisibility(View.GONE);
			}

			if (rowRate.equals(selectedRate)) {
				row.setSelected(true);
				// Let's set layout height to wrap content.
				row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
				if (roomRateDetailContainer.getVisibility() == View.GONE) {
					expand(row);
				}
				// Now let's bind the new room rate details.
				bindSelectedRoomDetails(roomRateDetailContainer, row, selectedRate);
			}
			else {
				row.setSelected(false);
				// Reset row height, hide the detail view container and change button text, color.
				if (roomRateDetailContainer.getVisibility() == View.VISIBLE) {
					collapse(row);
				}
				else {
					row.setBackgroundResource(android.R.color.white);
					addRoomButton.setVisibility(View.INVISIBLE);
					selectRoomButton.setVisibility(View.VISIBLE);
					int minHeightDimenValue = getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_list_height);
					LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) row.getLayoutParams();
					layoutParams.height = minHeightDimenValue;
					row.setLayoutParams(layoutParams);
				}
			}
		}


		container.requestLayout();
	}

	private void expand(final View row) {
		List<Animator> animators = new ArrayList<Animator>();

		final Button addRoomButton = Ui.findView(row, R.id.room_rate_button_add);
		final Button selectRoomButton = Ui.findView(row, R.id.room_rate_button_select);
		RelativeLayout container = Ui.findView(row, R.id.room_rate_detail_container);
		container.setVisibility(View.VISIBLE);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		container.measure(widthSpec, heightSpec);

		// Animation to collapse the container.
		ValueAnimator containerSlideAnimator = slideAnimator(container, 0, container.getMeasuredHeight());
		containerSlideAnimator.setDuration(ROOM_RATE_ANIMATION_DURATION);
		animators.add(containerSlideAnimator);

		// Animate the add button in.
		Animator addButtonAnimator = ObjectAnimator
			.ofFloat(addRoomButton, "alpha", 1)
			.setDuration(ROOM_RATE_ANIMATION_DURATION);
		addButtonAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				addRoomButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				addRoomButton.setVisibility(View.VISIBLE);
			}
		});
		animators.add(addButtonAnimator);

		// Animate the select button out.
		Animator selectButtonAnimator = ObjectAnimator
			.ofFloat(selectRoomButton, "alpha", 0)
			.setDuration(ROOM_RATE_ANIMATION_DURATION);
		selectButtonAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				selectRoomButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				selectRoomButton.setVisibility(View.INVISIBLE);
			}
		});
		animators.add(selectButtonAnimator);

		final ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.bg_row_state_pressed));
		row.setBackgroundDrawable(colorDrawable);

		Animator colorDrawableAnimator = ObjectAnimator
			.ofInt(colorDrawable, "alpha", 0, 255)
			.setDuration(ROOM_RATE_ANIMATION_DURATION);
		animators.add(colorDrawableAnimator);

		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// Let's not scroll the selected room rate if it's the default one. Since we want the user to look at the other info first.
				if (mDoReScroll) {
					LinearLayout rootContainer = Ui.findView(mRootC, R.id.rooms_rates_container);
					int headerHeight = getResources()
						.getDimensionPixelOffset(R.dimen.tablet_details_compact_header_height);
					mScrollView.smoothScrollTo(0, rootContainer.getTop() + row.getTop() - headerHeight);
				}
				else {
					// Let's reset this check so we rescroll to keep the selected room rate here on.
					mDoReScroll = true;
				}
			}
		});
		set.playTogether(animators);
		set.start();
	}

	private void collapse(final View row) {
		final Button addRoomButton = Ui.findView(row, R.id.room_rate_button_add);
		final Button selectRoomButton = Ui.findView(row, R.id.room_rate_button_select);

		List<Animator> animators = new ArrayList<Animator>();

		final RelativeLayout container = Ui.findView(row, R.id.room_rate_detail_container);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		container.measure(widthSpec, heightSpec);

		// Animation to collapse the container.
		ValueAnimator containerSlideAnimator = slideAnimator(container, container.getMeasuredHeight(), 0);
		containerSlideAnimator.setDuration(ROOM_RATE_ANIMATION_DURATION);

		containerSlideAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				container.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

		});
		animators.add(containerSlideAnimator);

		int minHeightDimenValue = getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_list_height);
		final int widthSpec1 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec1 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		row.measure(widthSpec1, heightSpec1);

		// Animation to set the row height appropriately.
		ValueAnimator mAnimator2 = slideAnimator(row, row.getMeasuredHeight(), minHeightDimenValue);
		mAnimator2.setDuration(ROOM_RATE_ANIMATION_DURATION);
		animators.add(mAnimator2);

		// Animate the add button out.
		Animator addButtonAnimator = ObjectAnimator
			.ofFloat(addRoomButton, "alpha", 0)
			.setDuration(ROOM_RATE_ANIMATION_DURATION);
		addButtonAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				addRoomButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				addRoomButton.setVisibility(View.INVISIBLE);
			}
		});
		animators.add(addButtonAnimator);

		// Animate the select button in.
		Animator selectButtonAnimator = ObjectAnimator
			.ofFloat(selectRoomButton, "alpha", 1)
			.setDuration(ROOM_RATE_ANIMATION_DURATION);
		selectButtonAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				selectRoomButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				selectRoomButton.setVisibility(View.VISIBLE);
			}
		});
		animators.add(selectButtonAnimator);

		final ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.bg_row_state_pressed));
		row.setBackgroundDrawable(colorDrawable);

		Animator colorDrawableAnimator = ObjectAnimator
			.ofInt(colorDrawable, "alpha", 255, 0)
			.setDuration(ROOM_RATE_ANIMATION_DURATION);
		animators.add(colorDrawableAnimator);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animators);
		set.start();

	}

	private ValueAnimator slideAnimator(final View row, int start, int end) {

		ValueAnimator animator = ValueAnimator.ofInt(start, end);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				//Update Height
				int value = (Integer) valueAnimator.getAnimatedValue();
				ViewGroup.LayoutParams layoutParams = row.getLayoutParams();
				layoutParams.height = value;
				row.setLayoutParams(layoutParams);
			}
		});
		return animator;
	}


	private void bindSelectedRoomDetails(final RelativeLayout container, final View row, Rate rate) {
		ImageView roomDetailImageView = Ui.findView(row, R.id.room_rate_image_view);
		TextView urgencyMessagingView = Ui.findView(row, R.id.room_rate_urgency_text);
		final TextView roomLongDescriptionTextView = Ui.findView(row, R.id.room_rate_description_text);
		TextView refundableTextView = Ui.findView(row, R.id.room_rate_refundable_text);
		TextView roomRateDiscountRibbon = Ui.findView(row, R.id.room_rate_discount_text);

		List<String> common = mResponse.getCommonValueAdds();

		// Value Adds
		List<String> unique = new ArrayList<String>(rate.getValueAdds());
		if (common != null) {
			unique.removeAll(common);
		}
		if (unique.size() > 0) {
			urgencyMessagingView.setText(Html.fromHtml(getActivity().getString(R.string.value_add_template,
				FormatUtils.series(getActivity(), unique, ",", null).toLowerCase(Locale.getDefault()))));
			urgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else if (showUrgencyMessaging(rate)) {
			String urgencyString = getResources()
				.getQuantityString(R.plurals.n_rooms_left_TEMPLATE, rate.getNumRoomsLeft(), rate.getNumRoomsLeft());
			urgencyMessagingView.setText(urgencyString);
			urgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else {
			urgencyMessagingView.setVisibility(View.GONE);
		}

		mIsDescriptionTextSpanned = false;
		final String description = rate.getRoomLongDescription().trim();
		String descriptionReduced;
		int lengthCutOff;
		// Let's try to show as much text to begin with as possible, without exceeding the row height.
		if (Ui.findView(row, R.id.room_rate_urgency_text).getVisibility() == View.VISIBLE) {
			lengthCutOff = getResources().getInteger(R.integer.room_rate_description_body_length_cutoff_less);
		}
		else {
			lengthCutOff = getResources().getInteger(R.integer.room_rate_description_body_length_cutoff_more);
		}

		if (description.length() > lengthCutOff) {
			descriptionReduced = description.substring(0, lengthCutOff);
			descriptionReduced += "...";
			SpannableBuilder builder = new SpannableBuilder();
			builder.append(descriptionReduced);
			builder.append(" ");
			builder.append(getResources().getString(R.string.more), new ForegroundColorSpan(0xFF245FB3),
				FontCache.getSpan(FontCache.Font.ROBOTO_BOLD));
			mIsDescriptionTextSpanned = true;
			roomLongDescriptionTextView.setText(builder.build());
		}
		else {
			roomLongDescriptionTextView.setText(description);
		}

		// #817. Let user tap to expand or contract the room description text.
		roomLongDescriptionTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// We need to reset the layout params for the container and the row.
				// So that we are ready to expand the textView when user wants it.
				if (mIsDescriptionTextSpanned) {
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					int marginDP = getResources()
						.getDimensionPixelSize(R.dimen.hotel_room_rate_detail_container_margin);
					layoutParams.bottomMargin = marginDP;
					layoutParams.topMargin = marginDP;
					container.setLayoutParams(layoutParams);
					row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));

					roomLongDescriptionTextView.setText(description);
				}
			}
		});

		// Refundable text visibility check
		refundableTextView.setVisibility(rate.isNonRefundable() ? View.VISIBLE : View.GONE);

		// Rooms and Rates detail image media
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.hotelImagePlaceHolderDrawable);
		if (rate.getThumbnail() != null) {
			rate.getThumbnail().fillImageView(roomDetailImageView, placeholderResId);
		}
		else {
			roomDetailImageView.setImageResource(placeholderResId);
		}

		// Room discount ribbon
		if (rate.getDiscountPercent() > 0) {
			roomRateDiscountRibbon.setVisibility(View.VISIBLE);
			roomRateDiscountRibbon.setText(getString(R.string.percent_minus_template, rate.getDiscountPercent()));
		}
		else {
			roomRateDiscountRibbon.setVisibility(View.GONE);
		}
	}

	private void openWebView(String title, String text) {
		String html;
		html = HtmlUtils.wrapInHeadAndBodyWithStandardTabletMargins(text);
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
		Intent intent = builder.setTitle(title).setHtmlData(html).setTheme(
			R.style.Theme_Phone_WebView_WithTitle).getIntent();
		startActivity(intent);
	}

	private void setupDescriptionSections(View view, Property property) {
		LinearLayout container = Ui.findView(view, R.id.description_details_sections_container);
		container.removeAllViews();

		List<HotelTextSection> sections = property.getAllHotelText(getActivity());

		if (sections != null && sections.size() > 0) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			for (int i = 0; i < sections.size(); i++) {
				HotelTextSection section = sections.get(i);
				View sectionContainer = inflater.inflate(R.layout.include_hotel_description_section,
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
				bodyText.setText(Html.fromHtml(section.getContentFormatted(getActivity())));
				container.addView(sectionContainer);
			}
		}
	}

	private void addSelectedRoomToTrip() {
		scrollFragmentToTop();

		HotelSearch search = Db.getHotelSearch();
		Property property = search.getSelectedProperty();
		Rate rate = search.getSelectedRate();
		if (rate == null) {
			rate = property.getLowestRate();
		}
		Db.getTripBucket().clearHotel();
		Db.getTripBucket().add(property, rate);
		Db.saveTripBucket(getActivity());

		mAddToBucketListener.onItemAddedToBucket();
	}

	private void scrollFragmentToTop() {
		mScrollView.scrollTo(0, 0);
	}

	private boolean showUrgencyMessaging(Rate rate) {
		int roomsLeft = rate.getNumRoomsLeft();
		return roomsLeft > 0 && roomsLeft < ROOM_COUNT_URGENCY_CUTOFF;
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
				Log.w(getString(R.string.e3_error_hotel_offers_hotel_service_failure));
				//showErrorDialog(R.string.e3_error_hotel_offers_hotel_service_failure);
				return;
			}
			else if (response.hasErrors()) {
				int messageResId;
				if (response.isHotelUnavailable()) {
					messageResId = R.string.error_room_is_now_sold_out;
				}
				else {
					messageResId = R.string.e3_error_hotel_offers_hotel_service_failure;
				}
				Log.w(getString(messageResId));
				//showErrorDialog(messageResId);
			}
			else if (search.getAvailability(selectedId) != null && search.getSearchParams() != null
				&& search.getAvailability(selectedId).getRateCount() == 0
				&& search.getSearchParams().getSearchType() != SearchType.HOTEL) {
				Log.w(getString(R.string.error_hotel_is_now_sold_out));
				//showErrorDialog(R.string.error_hotel_is_now_sold_out);
			}
			else {
				Db.kickOffBackgroundHotelSearchSave(getActivity());
			}

			// Notify affected child fragments to refresh.

			mResponse = response;
			populateViews();
			toggleLoadingState(false);
			Events.post(new Events.HotelAvailabilityUpdated());
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Async handling of Header / ColorScheme

	L2ImageCache.OnBitmapLoaded mHeaderBitmapLoadedCallback = new L2ImageCache.OnBitmapLoaded() {
		@Override
		public void onBitmapLoaded(String url, Bitmap bitmap) {
			ColorBuilder builder = new ColorBuilder(BitmapUtils.getAvgColorOnePixelTrick(bitmap)).darkenBy(0.4f);
			mHeaderContainer.setDominantColor(builder.build());
		}

		@Override
		public void onBitmapLoadFailed(String url) {
			mHeaderContainer.resetDominantColor();
		}
	};

	/*
	MEASUREMENT HELPER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			//This attempts to replicate the global layout by doing 3 columns for our general results layout
			//and 2 rows (where the first one represents the actionbar).
			GridManager globalGm = new GridManager(2, 3);
			globalGm.setDimensions(totalWidth, totalHeight);
			globalGm.setRowSize(0, getActivity().getActionBar().getHeight());

			//Now we set up our local positions
			mGrid.setDimensions(globalGm.getColSpanWidth(1, 3), globalGm.getRowHeight(1));
			mGrid.setNumCols(3);
			mGrid.setNumRows(3);

			int topBottomSpaceSize = getResources()
				.getDimensionPixelSize(R.dimen.tablet_hotel_details_vertical_padding);
			float leftRightSpacePerc = getResources()
				.getFraction(R.fraction.tablet_hotel_details_horizontal_spacing_percentage, 1, 1);
			mGrid.setRowSize(0, topBottomSpaceSize);
			mGrid.setRowSize(2, topBottomSpaceSize);
			mGrid.setColumnPercentage(0, leftRightSpacePerc);
			mGrid.setColumnPercentage(2, leftRightSpacePerc);

			mGrid.setContainerToRow(mRootC, 1);
			mGrid.setContainerToColumn(mRootC, 1);

			int halfContentSize = mGrid.getRowHeight(1) / 2;
			if (mHeaderContainer.getLayoutParams() != null) {
				mHeaderContainer.getLayoutParams().height = halfContentSize;
				mHeaderContainer.setLayoutParams(mHeaderContainer.getLayoutParams());
			}
		}
	};
}
