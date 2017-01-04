package com.expedia.bookings.fragment;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.HotelCrossSellUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.SettingUtils;
import com.squareup.phrase.Phrase;

public class TabletFlightConfirmationFragment extends TabletConfirmationFragment {

	public static final String TAG = TabletFlightConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	private static final String DESTINATION_IMAGE_INFO_DOWNLOAD_KEY = "DESTINATION_IMAGE_INFO_DOWNLOAD_KEY";

	private ViewGroup mFlightCard;
	private ViewGroup mAddHotelContainer;
	private ViewGroup mDoneBookingContainerRight;
	private ViewGroup mDoneBookingContainerStandalone;
	private ViewGroup mAirAttachContainer;
	private TextView mConfirmationTitleText;
	private TextView mShareButtonText;
	private TextView mAddHotelTextView;
	private TextView mWithDiscountsTextView;
	private TextView mAirAttachTextView;
	private TextView mAirAttachSavingsTextView;
	private TextView mAirAttachExpiresTextView;
	private TextView mAirAttachExpirationDateTextView;

	private ImageView mDestinationImageView;
	private HeaderBitmapDrawable mHeaderBitmapDrawable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		mConfirmationTitleText = Ui.findView(v, R.id.confirmation_title_text);
		mConfirmationTitleText.setText(R.string.tablet_confirmation_flight_booked);

		mShareButtonText = Ui.findView(v, R.id.share_action_text_view);
		mShareButtonText.setText(R.string.tablet_confirmation_share_flight);

		mAddHotelContainer = Ui.findView(v, R.id.confirmation_add_hotel_container);
		mAddHotelContainer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				OmnitureTracking.trackAddHotelClick();

				HotelSearchParams hotelSearchParams = HotelSearchParams.fromFlightParams(Db.getTripBucket().getFlight());
				HotelCrossSellUtils.deepLinkHotels(getActivity(), hotelSearchParams);
			}
		});

		mDoneBookingContainerRight = Ui.findView(v, R.id.confirmation_done_booking_container);
		mDoneBookingContainerStandalone = Ui.findView(v, R.id.confirmation_done_booking_standalone_container);

		mAddHotelTextView = Ui.findView(v, R.id.add_hotel_text_view);
		mWithDiscountsTextView = Ui.findView(v, R.id.with_discounts_text_view);

		mAirAttachContainer = Ui.findView(v, R.id.air_attach_banner_container);
		mAirAttachTextView = Ui.findView(v, R.id.air_attach_text_view);
		mAirAttachSavingsTextView = Ui.findView(v, R.id.air_attach_savings_text_view);
		mAirAttachExpiresTextView = Ui.findView(v, R.id.air_attach_expires_text_view);
		mAirAttachExpirationDateTextView = Ui.findView(v, R.id.air_attach_expiration_date_text_view);

		// Set up air attach banner if applicable
		// Currently only handling US POS
		if (Db.getTripBucket() != null && Db.getTripBucket().isUserAirAttachQualified()) {
			if (getNextBookingItem() == null) {
				mAirAttachContainer.setVisibility(View.VISIBLE);
				setAirAttachText(getString(R.string.air_attach_potential_savings));

				mAddHotelContainer.setVisibility(View.VISIBLE);
				mAddHotelTextView.setText(R.string.air_attach_add_hotel);
				mWithDiscountsTextView.setText(R.string.air_attach_with_discounts);

				mDoneBookingContainerRight.setVisibility(View.VISIBLE);
				mDoneBookingContainerStandalone.setVisibility(View.GONE);
			}
			else if (getNextBookingItem() == LineOfBusiness.HOTELS) {
				TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
				if (hotel.getCreateTripResponse() != null && hotel.getCreateTripResponse().getAirAttachRate() != null) {
					mAirAttachContainer.setVisibility(View.VISIBLE);

					Rate originalRate = hotel.getCreateTripResponse().getNewRate();
					Rate airAttachRate = hotel.getCreateTripResponse().getAirAttachRate();
					Money savings = new Money(originalRate.getTotalAmountAfterTax());
					savings.subtract(airAttachRate.getTotalAmountAfterTax());

					setAirAttachText(getString(R.string.air_attach_amount_discounted_TEMPLATE, savings.getFormattedMoney()));
				}
			}
		}

		// Construct the destination card
		mDestinationImageView = Ui.findView(v, R.id.confirmation_image_view);
		mHeaderBitmapDrawable = new HeaderBitmapDrawable();
		mHeaderBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		mHeaderBitmapDrawable.setCornerMode(CornerMode.ALL);
		mHeaderBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		mHeaderBitmapDrawable.setOverlayDrawable(getResources().getDrawable(R.drawable.card_top_lighting));
		mDestinationImageView.setImageDrawable(mHeaderBitmapDrawable);

		mHeaderBitmapDrawable.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));

		final String code = Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId();
		int imageWidth = getResources().getDimensionPixelSize(R.dimen.confirmation_width);
		int imageHeight = getResources().getDimensionPixelSize(R.dimen.confirmation_image_height);
		// FIXME tablet destination image
		final String url = new Akeakamai(Images.getTabletDestination(code)) //
			.resizeExactly(imageWidth, imageHeight) //
			.build();

		new PicassoHelper.Builder(getActivity()).setPlaceholder(R.drawable.bg_itin_placeholder)
			.setTarget(mHeaderBitmapDrawable.getCallBack()).build().load(url);
		setLob(LineOfBusiness.FLIGHTS);

		SettingUtils.save(getActivity(), R.string.preference_user_has_booked_hotel_or_flight, true);
		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// TabletConfirmationFragment

	protected void setAirAttachText(String savingsString) {
		mAirAttachTextView.setText(R.string.air_attach_alert);
		mAirAttachSavingsTextView.setText(savingsString);

		DateTime currentDate = new DateTime();
		int numDays = JodaUtils.daysBetween(currentDate, Db.getTripBucket().getAirAttach().getExpirationDate());
		mAirAttachExpiresTextView.setText(R.string.air_attach_expires);
		mAirAttachExpirationDateTextView.setText(
			Phrase.from(getResources().getQuantityString(R.plurals.days_from_now, numDays))
				.put("days", numDays).format().toString());

	}

	@Override
	protected String getItinNumber() {
		return Db.getTripBucket().getFlight().getItinerary().getItineraryNumber();
	}

	@Override
	protected String getConfirmationSummaryText() {
		FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
		String duration = DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		String fromTo = getString(R.string.tablet_confirmation_flights_from_to,
				params.getDepartureLocation().getCity(), params.getArrivalLocation().getCity());
		return getString(R.string.tablet_confirmation_summary, fromTo, duration);
	}

	@Override
	protected void shareItinerary() {
		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();

		int travelerCount = Db.getTravelers() == null ? 1 : Db.getTravelers().size();
		ShareUtils shareUtils = new ShareUtils(getActivity());
		String subject = shareUtils.getFlightShareSubject(trip, travelerCount);
		String body = shareUtils.getFlightShareEmail(trip, Db.getTravelers());

		SocialUtils.email(getActivity(), subject, body);

		OmnitureTracking.trackFlightConfirmationShareEmail();
	}

	@Override
	protected void addItineraryToCalendar() {
		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
		for (int a = 0; a < trip.getLegCount(); a++) {
			Intent intent = generateCalendarInsertIntent(trip.getLeg(a));
			startActivity(intent);
		}

		OmnitureTracking.trackFlightConfirmationAddToCalendar();
	}

	@SuppressLint("NewApi")
	private Intent generateCalendarInsertIntent(FlightLeg leg) {
		PointOfSale pointOfSale = PointOfSale.getPointOfSale();
		String itineraryNumber = Db.getTripBucket().getFlight().getFlightTrip().getItineraryNumber();
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
