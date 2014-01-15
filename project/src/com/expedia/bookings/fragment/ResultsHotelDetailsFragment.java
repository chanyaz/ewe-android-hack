package com.expedia.bookings.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
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
	private ViewGroup mHotelHeaderContainer;
	private Button mAddToTripButton;

	private IAddToTripListener mAddToTripListener;

	HotelOffersResponse mResponse;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mAddToTripListener = Ui.findFragmentListener(this, IAddToTripListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotel_details, null);
		mHotelHeaderContainer = Ui.findView(mRootC, R.id.hotel_header_container);
		mAddToTripButton = Ui.findView(mRootC, R.id.button_add_to_trip);

		mAddToTripButton.setPivotY(0f);
		mAddToTripButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mAddToTripListener.beginAddToTrip(getSelectedData(), getDestinationRect(), 0);
			}

		});

		mRootC.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				int paddingx = (int) (mRootC.getWidth() * 0.14f);
				mRootC.setPadding(paddingx, mRootC.getPaddingTop(), paddingx, mRootC.getPaddingBottom());

				int headerHeight = (mRootC.getHeight() - mRootC.getPaddingTop() - mRootC.getPaddingBottom()) / 2;
				Ui.findView(mRootC, R.id.hotel_header_container).getLayoutParams().height = headerHeight;
			}
		});
		return mRootC;
	}

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
		if (property != null) {
			setupHeader(mRootC, property);
			setupReviews(mRootC, property);
			setupAmenities(mRootC, property);
			setupRoomRates(mRootC, property);
			setupDescriptionSections(mRootC, property);
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

	public Object getSelectedData() {
		return "SOME DATA";
	}

	public Rect getDestinationRect() {
		return ScreenPositionUtils.getGlobalScreenPosition(mHotelHeaderContainer);
	}

	private void setupHeader(View view, Property property) {
		ImageView hotelImage = Ui.findView(view, R.id.hotel_header_image);
		TextView hotelName = Ui.findView(view, R.id.hotel_header_hotel_name);
		TextView notRatedText = Ui.findView(view, R.id.not_rated_text_view);
		RatingBar starRating = Ui.findView(view, R.id.star_rating_bar);
		RatingBar userRating = Ui.findView(view, R.id.user_rating_bar);
		TextView starRatingText = Ui.findView(view, R.id.star_rating_text);
		TextView userRatingText = Ui.findView(view, R.id.user_rating_text);

		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.hotelImagePlaceHolderDrawable);
		if (property.getThumbnail() != null) {
			property.getThumbnail().fillImageView(hotelImage, placeholderResId);
		}
		else {
			hotelImage.setImageResource(placeholderResId);
		}

		hotelName.setText(property.getName());

		float starRatingValue = (float) property.getHotelRating();
		if (starRatingValue == 0f) {
			starRating.setVisibility(View.GONE);
			starRatingText.setVisibility(View.GONE);
			notRatedText.setVisibility(View.VISIBLE);
		}
		else {
			starRating.setVisibility(View.VISIBLE);
			starRatingText.setVisibility(View.VISIBLE);
			notRatedText.setVisibility(View.GONE);
			starRating.setRating(starRatingValue);
			starRatingText.setText(getString(R.string.n_stars_TEMPLATE, starRatingValue));
		}

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
		RingedCountView userRatingRing = Ui.findView(view, R.id.user_rating_ring);
		TextView roomsLeftText = Ui.findView(view, R.id.rooms_left_ring_text);
		TextView userRatingText = Ui.findView(view, R.id.user_rating_ring_text);
		TextView readReviewsText = Ui.findView(view, R.id.read_reviews_text);

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
			roomsLeftRing.setCount(roomsLeft);
			roomsLeftText.setText(res.getQuantityText(R.plurals.rooms_left, roomsLeft));
		}
		else if (property.getPercentRecommended() == 0f) {
			roomsLeftRing.setVisibility(View.INVISIBLE);
			roomsLeftText.setVisibility(View.INVISIBLE);
		}
		else {
			roomsLeftRing.setVisibility(View.VISIBLE);
			roomsLeftText.setVisibility(View.VISIBLE);
			roomsLeftRing.setPrimaryColor(res.getColor(R.color.details_ring_blue));
			roomsLeftRing.setCountTextColor(res.getColor(R.color.details_ring_text));
			roomsLeftRing.setPercent(property.getPercentRecommended() / 100f);
			roomsLeftText.setText(R.string.recommend);
			roomsLeftRing.setCountText(Math.round(property.getPercentRecommended()) + "%");
		}

		if (userRatingAvailable) {
			float percent = (float) property.getAverageExpediaRating() / 5f;
			userRatingRing.setVisibility(View.VISIBLE);
			userRatingText.setVisibility(View.VISIBLE);
			userRatingRing.setPercent(percent);
			DecimalFormat fmt = new DecimalFormat("0.#");
			userRatingRing.setCountText(fmt.format(property.getAverageExpediaRating()));
			readReviewsText.setText(R.string.Read_Reviews);
		}
		else {
			userRatingRing.setVisibility(View.INVISIBLE);
			userRatingText.setVisibility(View.INVISIBLE);
			readReviewsText.setText(R.string.No_Reviews);
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
		float minPadding = getResources().getDimension(R.dimen.tablet_detail_padding);
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

	private void setupRoomRates(View view, Property property) {
		LinearLayout container = Ui.findView(view, R.id.rooms_rates_container);
		container.removeAllViews();

		if (mResponse == null) {
			return;
		}

		Rate lowestRate = property.getLowestRate();

		Button addToTrip = Ui.findView(view, R.id.button_add_to_trip);
		String addTripStr = getString(R.string.add_for_TEMPLATE,
				lowestRate.getDisplayPrice().getFormattedMoney(Money.F_NO_DECIMAL));
		addToTrip.setText(addTripStr);

		List<Rate> rates = mResponse.getRates();

		List<String> common = mResponse.getCommonValueAdds();

		// TODO: I wonder if we should use RoomsAndRatesAdapter, or similar
		boolean first = true;
		LayoutInflater inflater = getActivity().getLayoutInflater();
		for (Rate rate : rates) {
			View row = inflater.inflate(R.layout.row_tablet_room_rate, container, false);
			TextView description = Ui.findView(row, R.id.text_room_description);
			TextView valueAdds = Ui.findView(row, R.id.text_value_adds);
			TextView nonRefundable = Ui.findView(row, R.id.text_non_refundable);
			ImageView checkmark = Ui.findView(row, R.id.image_checkmark);
			Button select = Ui.findView(row, R.id.button_select_new_room_rate);

			// Separator
			if (!first) {
				View sep = new View(getActivity());
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				sep.setLayoutParams(lp);
				sep.setBackgroundColor(Color.rgb(0xd9, 0xdc, 0xe0));
				container.addView(sep);
			}

			// Description
			description.setText(rate.getRoomDescription());

			// Value Adds
			List<String> unique = new ArrayList<String>(rate.getValueAdds());
			if (common != null) {
				unique.removeAll(common);
			}
			if (unique.size() > 0) {
				valueAdds.setText(Html.fromHtml(getActivity().getString(R.string.value_add_template,
						FormatUtils.series(getActivity(), unique, ",", null).toLowerCase(Locale.getDefault()))));
				valueAdds.setVisibility(View.VISIBLE);
			}
			else {
				valueAdds.setVisibility(View.GONE);
			}

			// Non Refundable
			nonRefundable.setVisibility(rate.isNonRefundable() ? View.VISIBLE : View.GONE);

			// Selected / +$20
			checkmark.setVisibility(/*rate.equals(lowestRate)*/first ? View.VISIBLE : View.INVISIBLE);
			select.setVisibility(/*rate.equals(lowestRate)*/first ? View.INVISIBLE : View.VISIBLE);
			select.setText(rate.getRelativeDisplayPriceString(lowestRate));
			container.addView(row);
			first = false;
		}
	}

	private void setupDescriptionSections(View view, Property property) {
		LinearLayout container = Ui.findView(view, R.id.description_details_sections_container);
		container.removeAllViews();

		List<HotelTextSection> sections = property.getAllHotelText(getActivity());

		if (sections != null && sections.size() > 1) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			for (int i = 1; i < sections.size(); i++) {
				HotelTextSection section = sections.get(i);
				View sectionContainer = inflater.inflate(R.layout.include_hotel_description_section,
						container, false);

				TextView titleText = Ui.findView(sectionContainer, R.id.title_text);
				TextView bodyText = Ui.findView(sectionContainer, R.id.body_text);
				titleText.setVisibility(View.VISIBLE);
				titleText.setText(section.getNameWithoutHtml());
				bodyText.setText(Html.fromHtml(section.getContentFormatted(getActivity())));
				container.addView(sectionContainer);
			}
		}
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
