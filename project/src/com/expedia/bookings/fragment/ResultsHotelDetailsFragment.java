package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
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
import com.expedia.bookings.interfaces.IResultsHotelReviewsClickedListener;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.RingedCountView;
import com.expedia.bookings.widget.ScrollView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.Log;

/**
 * ResultsHotelDetailsFragment: The hotel details / rooms and rates
 * fragment designed for tablet results 2013
 */
@TargetApi(11)
public class ResultsHotelDetailsFragment extends Fragment {

	public static ResultsHotelDetailsFragment newInstance() {
		ResultsHotelDetailsFragment frag = new ResultsHotelDetailsFragment();
		return frag;
	}

	private ViewGroup mRootC;

	private IAddToBucketListener mAddToBucketListener;
	private IResultsHotelReviewsClickedListener mHotelReviewsClickedListener;

	HotelOffersResponse mResponse;

	private static final int ROOM_COUNT_URGENCY_CUTOFF = 5;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mAddToBucketListener = Ui.findFragmentListener(this, IAddToBucketListener.class);
		mHotelReviewsClickedListener = Ui.findFragmentListener(this, IResultsHotelReviewsClickedListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotel_details, null);

		// TODO: figure out how to use resource system for all of this instead
		mRootC.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				int paddingx = (int) (mRootC.getWidth() * 0.14f);
				mRootC.setPadding(paddingx, mRootC.getPaddingTop(), paddingx, mRootC.getPaddingBottom());

				int headerHeight = (mRootC.getHeight() - mRootC.getPaddingTop() - mRootC.getPaddingBottom()) / 2;
				ViewGroup.LayoutParams params = Ui.findView(mRootC, R.id.header_container).getLayoutParams();
				params.height = headerHeight;

				RelativeLayout.LayoutParams reviewsParams = (RelativeLayout.LayoutParams)(Ui.findView(mRootC, R.id.reviews_container).getLayoutParams());
				reviewsParams.topMargin = headerHeight;
			}
		});
		return mRootC;
	}

	private OnClickListener mReviewsButtonClickedListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mHotelReviewsClickedListener.onHotelReviewsClicked();
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		populateViews();
		downloadDetails();
	}

	public void onHotelSelected() {
		downloadDetails();
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}
		else {
			bd.unregisterDownloadCallback(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}
	}

	private void downloadDetails() {
		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		if (TextUtils.isEmpty(selectedId)) {
			return;
		}

		final BackgroundDownloader bd = BackgroundDownloader.getInstance();
		final String key = CrossContextHelper.KEY_INFO_DOWNLOAD;
		final HotelOffersResponse infoResponse = Db.getHotelSearch().getHotelOffersResponse(selectedId);
		if (infoResponse != null) {
			// We may have been downloading the data here before getting it elsewhere, so cancel
			// our own download once we have data
			bd.cancelDownload(key);

			// Load the data
			mInfoCallback.onDownload(infoResponse);
		}
		else if (bd.isDownloading(key)) {
			bd.registerDownloadCallback(key, mInfoCallback);
		}
		else {
			bd.startDownload(key, CrossContextHelper.getHotelOffersDownload(getActivity(), key), mInfoCallback);
		}

	}

	/**
	 * This could be called when not much information is available, or after
	 * details have been downloaded from e3. We should handle both cases gracefully
	 * in here.
	 */
	private void populateViews() {
		Property property = Db.getHotelSearch().getSelectedProperty();
		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		if (property != null) {
			setupHeader(mRootC, property);
			setupReviews(mRootC, property);
			setupAmenities(mRootC, property);
			setupRoomRates(mRootC, property);
			setupDescriptionSections(mRootC, property);
			if (Db.getHotelSearch().getAvailability(selectedId) != null) {
				setDefaultSelectedRate();
			}
		}
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

	private void setupHeader(View view, Property property) {
		ImageView hotelImage = Ui.findView(view, R.id.hotel_header_image);
		TextView hotelName = Ui.findView(view, R.id.hotel_header_hotel_name);
		TextView notRatedText = Ui.findView(view, R.id.not_rated_text_view);
		RatingBar starRating = Ui.findView(view, R.id.star_rating_bar);
		RatingBar userRating = Ui.findView(view, R.id.user_rating_bar);
		TextView userRatingText = Ui.findView(view, R.id.user_rating_text);
		TextView vipText = Ui.findView(view, R.id.vip_badge);
		TextView saleText = Ui.findView(view, R.id.sale_text_view);

		// Hotel Image
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.hotelImagePlaceHolderDrawable);
		if (property.getThumbnail() != null) {
			property.getThumbnail().fillImageView(hotelImage, placeholderResId);
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

		// Hotel Name
		hotelName.setText(property.getName());

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
		}

		// User Rating
		float userRatingValue = (float) property.getAverageExpediaRating();
		int totalReviews = property.getTotalReviews();
		if (totalReviews == 0) {
			userRating.setVisibility(View.GONE);
			userRatingText.setVisibility(View.GONE);
		}
		else {
			userRating.setVisibility(View.VISIBLE);
			userRatingText.setVisibility(View.VISIBLE);
			userRating.setRating(userRatingValue);
			userRatingText.setText(getString(R.string.n_reviews_TEMPLATE, totalReviews));
		}
	}

	private void setupReviews(View view, Property property) {
		RingedCountView roomsLeftRing = Ui.findView(view, R.id.rooms_left_ring);
		TextView roomsLeftText = Ui.findView(view, R.id.rooms_left_ring_text);

		boolean userRatingAvailable = property.getTotalReviews() != 0;

		Resources res = getResources();
		int roomsLeft = property.getRoomsLeftAtThisRate();
		if (roomsLeft <= 5 && roomsLeft >= 0) {
			int color = res.getColor(R.color.details_ring_red);
			roomsLeftRing.setVisibility(View.VISIBLE);
			roomsLeftText.setVisibility(View.VISIBLE);
			roomsLeftRing.setPrimaryColor(color);
			roomsLeftRing.setCountTextColor(color);
			roomsLeftRing.setPercent(roomsLeft / 10f);
			roomsLeftRing.setCountText("");
			roomsLeftText.setText(res.getQuantityString(R.plurals.n_rooms_left_TEMPLATE, roomsLeft, roomsLeft));
		}
		else if (property.getPercentRecommended() == 0f) {
			roomsLeftRing.setVisibility(View.INVISIBLE);
			roomsLeftText.setVisibility(View.INVISIBLE);
		}
		else {
			roomsLeftRing.setVisibility(View.VISIBLE);
			roomsLeftText.setVisibility(View.VISIBLE);
			roomsLeftRing.setPrimaryColor(res.getColor(R.color.details_ring_blue));
			roomsLeftRing.setPercent(property.getPercentRecommended() / 100f);
			roomsLeftRing.setCountText("");
			int percent = Math.round(property.getPercentRecommended());
			roomsLeftText.setText(getString(R.string.n_recommend_TEMPLATE, percent));
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
		ViewGroup amenitiesContainer = Ui.findView(view, R.id.amenities_container);
		float desiredPadding = (amenitiesContainer.getWidth() - amenitiesWidth) / 2;
		float minPadding = 0;
		int padding = (int) Math.max(minPadding, desiredPadding);
		amenitiesContainer.setPadding(padding, 0, padding, 0);

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
			Rate clickedRate = (Rate) v.getTag();
			setSelectedRate(clickedRate);
		}
	};

	private void setupRoomRates(View view, Property property) {
		LinearLayout container = Ui.findView(view, R.id.rooms_rates_container);
		container.removeAllViews();

		if (mResponse == null) {
			return;
		}

		List<Rate> rates = mResponse.getRates();

		// TODO: I wonder if we should use RoomsAndRatesAdapter, or similar
		boolean first = true;
		LayoutInflater inflater = getActivity().getLayoutInflater();
		for (Rate rate : rates) {
			View row = inflater.inflate(R.layout.row_tablet_room_rate, container, false);
			row.setTag(rate);
			row.setOnClickListener(mRateClickListener);
			TextView description = Ui.findView(row, R.id.text_room_description);
			TextView pricePerNight = Ui.findView(row, R.id.text_price_per_night);
			TextView bedType = Ui.findView(row, R.id.text_bed_type);

			// Separator
			if (!first) {
				View sep = inflater.inflate(R.layout.row_tablet_room_rate_separator, container, false);
				container.addView(sep);
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

			container.addView(row);
			first = false;
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
		setSelectedRate(rate);
	}

	/**
	 * Called in response to a user click on a different room rate. Makes sure the
	 * checkmark and relative prices are all in sync.
	 *
	 * @param selectedRate
	 */
	@TargetApi(14)
	private void setSelectedRate(final Rate selectedRate) {
		Db.getHotelSearch().setSelectedRate(selectedRate);

		LinearLayout container = Ui.findView(getView(), R.id.rooms_rates_container);
		for (int i = 0; i < container.getChildCount(); i++) {
			View row = container.getChildAt(i);
			final Rate rowRate = (Rate) row.getTag();
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) row.getLayoutParams();

			// Skip if this is a separator row
			if (rowRate == null) {
				continue;
			}

			LinearLayout mRoomRateDetailContainer = Ui.findView(row, R.id.room_rate_detail_container);
			Button mAddSelectRoomButton = Ui.findView(row, R.id.room_rate_button_add_select);
			mAddSelectRoomButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (rowRate.equals(selectedRate)) {
						addSelectedRoomToTrip();
					}
					else {
						setSelectedRate(rowRate);
					}
				}
			});

			if (rowRate.equals(selectedRate)) {
				// Let's set layout height to wrap content.
				row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				mRoomRateDetailContainer.setVisibility(View.VISIBLE);
				// Change button color and text
				mAddSelectRoomButton.setText(getString(R.string.room_rate_button_add_to_trip));
				mAddSelectRoomButton.setBackgroundColor(getResources().getColor(R.color.hotel_room_rate_add_room_button));
				// Now let's bind the new room rate details.
				bindSelectedRoomDetails(row, selectedRate);
			}
			else {
				// Reset row height, hide the detail view container and change button text, color.
				int minHeightDimenValue = getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_list_height);
				layoutParams.height = minHeightDimenValue;
				row.setLayoutParams(layoutParams);
				mRoomRateDetailContainer.setVisibility(View.INVISIBLE);
				mAddSelectRoomButton.setText(getString(R.string.room_rate_button_select_room));
				mAddSelectRoomButton.setBackgroundColor(getResources().getColor(R.color.hotel_room_rate_select_room_button));
			}
		}

		container.requestLayout();
	}

	private void bindSelectedRoomDetails(View row, Rate rate) {
		ImageView mRoomDetailImageView = Ui.findView(row, R.id.room_rate_image_view);
		TextView mUrgencyMessagingView = Ui.findView(row, R.id.room_rate_urgency_text);
		final TextView mRoomLongDescriptionTextView = Ui.findView(row, R.id.room_rate_description_text);
		TextView mRefundableTextView = Ui.findView(row, R.id.room_rate_refundable_text);

		mRoomLongDescriptionTextView.setText(rate.getRoomLongDescription());
		// #817. Let user tap to expand or contract the room description text.
		mRoomLongDescriptionTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mRoomLongDescriptionTextView.getEllipsize() != null) {
					mRoomLongDescriptionTextView.setEllipsize(null);
					mRoomLongDescriptionTextView.setMaxLines(Integer.MAX_VALUE);
				}
				else {
					mRoomLongDescriptionTextView.setEllipsize(TextUtils.TruncateAt.END);
					mRoomLongDescriptionTextView.setMaxLines(5);
				}
			}
		});

		List<String> common = mResponse.getCommonValueAdds();

		// Value Adds
		List<String> unique = new ArrayList<String>(rate.getValueAdds());
		if (common != null) {
			unique.removeAll(common);
		}
		if (unique.size() > 0) {
			mUrgencyMessagingView.setText(Html.fromHtml(getActivity().getString(R.string.value_add_template,
				FormatUtils.series(getActivity(), unique, ",", null).toLowerCase(Locale.getDefault()))));
			mUrgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else if (showUrgencyMessaging(rate)) {
			String urgencyString = getResources().getQuantityString(R.plurals.n_rooms_left_TEMPLATE, rate.getNumRoomsLeft(), rate.getNumRoomsLeft());
			mUrgencyMessagingView.setText(urgencyString);
			mUrgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else {
			mUrgencyMessagingView.setVisibility(View.GONE);
		}

		// Refundable text visibility check
		mRefundableTextView.setVisibility(rate.isNonRefundable() ? View.VISIBLE : View.GONE);

		// Rooms and Rates detail image media
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.hotelImagePlaceHolderDrawable);
		if (rate.getThumbnail() != null) {
			rate.getThumbnail().fillImageView(mRoomDetailImageView, placeholderResId);
		}
		else {
			mRoomDetailImageView.setImageResource(placeholderResId);
		}
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
		ScrollView scrollView = Ui.findView(mRootC, R.id.scrolling_content);
		scrollView.smoothScrollTo(0, 0);

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

	private boolean showUrgencyMessaging(Rate rate) {
		int roomsLeft = rate.getNumRoomsLeft();
		return roomsLeft > 0 && roomsLeft < ROOM_COUNT_URGENCY_CUTOFF;
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
		}
	};

}
