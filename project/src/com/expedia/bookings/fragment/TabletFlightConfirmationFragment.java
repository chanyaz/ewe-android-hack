package com.expedia.bookings.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.ShareUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;

public class TabletFlightConfirmationFragment extends TabletConfirmationFragment {

	public static final String TAG = TabletFlightConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	private static final String DESTINATION_IMAGE_INFO_DOWNLOAD_KEY = "DESTINATION_IMAGE_INFO_DOWNLOAD_KEY";

	private ViewGroup mFlightCard;
	private TextView mConfirmationTitleText;
	private TextView mShareButtonText;

	private ImageView mDestinationImageView;
	private HeaderBitmapDrawable mHeaderBitmapDrawable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		mConfirmationTitleText = Ui.findView(v, R.id.confirmation_title_text);
		mConfirmationTitleText.setText(R.string.tablet_confirmation_flight_booked);

		mShareButtonText = Ui.findView(v, R.id.share_action_text_view);
		mShareButtonText.setText(R.string.tablet_confirmation_share_flight);

		// Construct the destination card
		mDestinationImageView = Ui.findView(v, R.id.confirmation_image_view);
		mHeaderBitmapDrawable = new HeaderBitmapDrawable();
		mHeaderBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		mHeaderBitmapDrawable.setCornerMode(CornerMode.ALL);
		mHeaderBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		mHeaderBitmapDrawable.setOverlayDrawable(getResources().getDrawable(R.drawable.card_top_lighting));
		mDestinationImageView.setImageDrawable(mHeaderBitmapDrawable);

		mHeaderBitmapDrawable.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));

		final String code = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();
		int imageWidth = getResources().getDimensionPixelSize(R.dimen.confirmation_width);
		int imageHeight = getResources().getDimensionPixelSize(R.dimen.confirmation_image_height);
		// FIXME tablet destination image
		final String url = new Akeakamai(Images.getFlightDestination(code)) //
			.resizeExactly(imageWidth, imageHeight) //
			.build();

		mHeaderBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), url, R.drawable.bg_itin_placeholder));
		setLob(LineOfBusiness.FLIGHTS);

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// TabletConfirmationFragment

	@Override
	protected String getItinNumber() {
		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();
		Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());
		return itinerary.getItineraryNumber();
	}

	@Override
	protected String getConfirmationSummaryText() {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		String duration = CalendarUtils.formatDateRange(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		String fromTo = getString(R.string.tablet_confirmation_flights_from_to,
				params.getDepartureLocation().getCity(), params.getArrivalLocation().getCity());
		return getString(R.string.tablet_confirmation_summary, fromTo, duration);
	}

	@Override
	protected void shareItinerary() {
		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();

		int travelerCount = Db.getTravelers() == null ? 1 : Db.getTravelers().size();
		ShareUtils shareUtils = new ShareUtils(getActivity());
		String subject = shareUtils.getFlightShareSubject(trip, travelerCount);
		String body = shareUtils.getFlightShareEmail(trip, Db.getTravelers());

		SocialUtils.email(getActivity(), subject, body);

		OmnitureTracking.trackFlightConfirmationShareEmail(getActivity());
	}

	@Override
	protected void addItineraryToCalendar() {
		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		for (int a = 0; a < trip.getLegCount(); a++) {
			Intent intent = generateCalendarInsertIntent(trip.getLeg(a));
			startActivity(intent);
		}

		OmnitureTracking.trackFlightConfirmationAddToCalendar(getActivity());
	}

	@SuppressLint("NewApi")
	private Intent generateCalendarInsertIntent(FlightLeg leg) {
		PointOfSale pointOfSale = PointOfSale.getPointOfSale();
		String itineraryNumber = Db.getFlightSearch().getSelectedFlightTrip().getItineraryNumber();
		return AddToCalendarUtils.generateFlightAddToCalendarIntent(getActivity(), pointOfSale, itineraryNumber, leg);
	}

	@Override
	protected LineOfBusiness getNextBookingItem() {
		if (Db.getTripBucket().getHotel() != null && Db.getTripBucket().getHotel().canBePurchased()) {
			return LineOfBusiness.HOTELS;
		}
		else {
			return null;
		}
	}
}
