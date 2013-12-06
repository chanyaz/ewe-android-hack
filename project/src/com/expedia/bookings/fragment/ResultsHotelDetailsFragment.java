package com.expedia.bookings.fragment;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.RingedCountView;
import com.expedia.bookings.widget.ScrollView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

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

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		populateViews();
		downloadDetails();
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
		TextView hotelName = Ui.findView(view, R.id.hotel_header_hotel_name);
		RatingBar starRating = Ui.findView(view, R.id.star_rating_bar);
		RatingBar userRating = Ui.findView(view, R.id.user_rating_bar);
		TextView starRatingText = Ui.findView(view, R.id.star_rating_text);
		TextView userRatingText = Ui.findView(view, R.id.user_rating_text);

		hotelName.setText(property.getName());
		starRating.setRating((float) property.getHotelRating());
		userRating.setRating((float) property.getAverageExpediaRating());
		starRatingText.setText(getString(R.string.n_stars_TEMPLATE, property.getHotelRating()));
		userRatingText.setText(getString(R.string.n_reviews_TEMPLATE, property.getTotalReviews()));
	}

	private void setupReviews(View view, Property property) {
		RingedCountView roomsLeftRing = Ui.findView(view, R.id.rooms_left_ring);
		RingedCountView userRatingRing = Ui.findView(view, R.id.user_rating_ring);
		TextView roomsLeftText = Ui.findView(view, R.id.rooms_left_ring_text);

		int roomsLeft = property.getRoomsLeftAtThisRate();
		Log.e("DOUG: roomsLeft = " + roomsLeft);
		if (roomsLeft <= 5 && roomsLeft >= 0) {
			int color = getResources().getColor(R.color.details_ring_red);
			roomsLeftRing.setPrimaryColor(color);
			roomsLeftRing.setCountTextColor(color);
			roomsLeftRing.setPercent(roomsLeft / 10f);
			roomsLeftRing.setCount(roomsLeft);
			roomsLeftText.setText(R.string.rooms_left);
		}
		else {
			roomsLeftRing.setPrimaryColor(getResources().getColor(R.color.details_ring_blue));
			roomsLeftRing.setCountTextColor(getResources().getColor(R.color.details_ring_text));
			roomsLeftRing.setPercent(property.getPercentRecommended() / 100f);
			roomsLeftText.setText(R.string.recommend);
			//TODO: set count text to i.e. "90%"
		}

		float percent = (float) property.getAverageExpediaRating() / 5f;
		userRatingRing.setPercent(percent);
		//TODO: set count text to i.e. "4.5"
		userRatingRing.setCount((float) Math.round(property.getAverageExpediaRating()));
	}

	private void setupAmenities(View view, Property property) {
		// Disable some aspects of the horizontal scrollview so it looks pretty
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) view.findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);
		amenitiesScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

		ViewGroup amenitiesContainer = (ViewGroup) view.findViewById(R.id.amenities_table_row);
		amenitiesContainer.removeAllViews();
		LayoutUtils.addAmenities(getActivity(), property, amenitiesContainer);

		// Hide the text that indicated no amenities because there are amenities
		view.findViewById(R.id.amenities_none_text).setVisibility(View.GONE);
		if (property.hasAmenities()) {
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.VISIBLE);
		}
		else {
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.GONE);
		}
	}

	private void setupDescriptionSections(View view, Property property) {
		LinearLayout allSectionsContainer = Ui.findView(view, R.id.description_details_sections_container);
		allSectionsContainer.removeAllViews();

		List<HotelTextSection> sections = property.getAllHotelText(getActivity());

		if (sections != null && sections.size() > 1) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			for (int i = 1; i < sections.size(); i++) {
				HotelTextSection section = sections.get(i);
				View sectionContainer = inflater.inflate(R.layout.include_hotel_description_section,
						allSectionsContainer, false);

				TextView titleText = Ui.findView(sectionContainer, R.id.title_text);
				TextView bodyText = Ui.findView(sectionContainer, R.id.body_text);
				titleText.setVisibility(View.VISIBLE);
				titleText.setText(section.getNameWithoutHtml());
				bodyText.setText(Html.fromHtml(section.getContentFormatted(getActivity())));
				allSectionsContainer.addView(sectionContainer);
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
			else if (search.getAvailability(selectedId).getRateCount() == 0
					&& search.getSearchParams().getSearchType() != SearchType.HOTEL) {
				Log.w(getString(R.string.error_hotel_is_now_sold_out));
				//showErrorDialog(R.string.error_hotel_is_now_sold_out);
			}
			else {
				Db.kickOffBackgroundHotelSearchSave(getActivity());
			}

			// Notify affected child fragments to refresh.

			populateViews();
		}
	};

}
