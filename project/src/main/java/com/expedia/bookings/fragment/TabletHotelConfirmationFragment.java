package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.tracking.AdImpressionTracking;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.SettingUtils;

public class TabletHotelConfirmationFragment extends TabletConfirmationFragment {

	public static final String TAG = TabletHotelConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	private TextView mConfirmationTitleText;
	private TextView mShareButtonText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		mConfirmationTitleText = Ui.findView(v, R.id.confirmation_title_text);
		mConfirmationTitleText.setText(R.string.tablet_confirmation_hotel_booked);

		mShareButtonText = Ui.findView(v, R.id.share_action_text_view);
		mShareButtonText.setText(R.string.tablet_confirmation_share_hotel);

		// Construct the hotel card
		ImageView hotelImageView = Ui.findView(v, R.id.confirmation_image_view);
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		headerBitmapDrawable.setCornerMode(CornerMode.ALL);
		headerBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		headerBitmapDrawable.setOverlayDrawable(getResources().getDrawable(R.drawable.card_top_lighting));
		hotelImageView.setImageDrawable(headerBitmapDrawable);

		HotelMedia hotelMedia = Db.getTripBucket().getHotel().getProperty().getThumbnail();
		if (hotelMedia != null) {
			new PicassoHelper.Builder(getActivity())
				.setPlaceholder(R.drawable.bg_itin_placeholder).setTarget(headerBitmapDrawable.getCallBack()).build().load(hotelMedia.getHighResUrls());
		}
		else {
			headerBitmapDrawable
					.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));
		}

		setLob(LineOfBusiness.HOTELS);

		CreateTripResponse tripResponse = Db.getTripBucket().getHotel().getCreateTripResponse();
		if (tripResponse != null) {
			AdImpressionTracking.trackAdConversion(getActivity(), tripResponse.getTripId());
			SettingUtils.save(getActivity(), R.string.preference_user_has_booked_hotel_or_flight, true);
		}

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// TabletConfirmationFragment

	@Override
	protected String getItinNumber() {
		return Db.getTripBucket().getHotel().getBookingResponse().getItineraryId();
	}

	@Override
	protected String getConfirmationSummaryText() {
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		String hotelName = Db.getTripBucket().getHotel().getProperty().getName();
		String duration = DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		return getString(R.string.tablet_confirmation_summary, hotelName, duration);
	}

	@Override
	protected void shareItinerary() {
		Context context = getActivity();

		HotelSearchParams searchParams = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getBookingResponse().getProperty();

		ShareUtils socialUtils = new ShareUtils(context);
		LocalDate checkIn = searchParams.getCheckInDate();
		LocalDate checkOut = searchParams.getCheckOutDate();
		String address = StrUtils.formatAddress(property.getLocation());
		String phone = Db.getTripBucket().getHotel().getBookingResponse().getPhoneNumber();

		//In this screen isShared & travelerName would not be relevant. So just set to false and null and pass it on to ShareUtils.
		String subject = socialUtils.getHotelShareSubject(property.getLocation().getCity(), checkIn, checkOut, false,
				null);
		String body = socialUtils.getHotelShareTextLong(property.getName(), address, phone, checkIn, checkOut, null,
				false, null);

		SocialUtils.email(context, subject, body);

		OmnitureTracking.trackHotelConfirmationShareEmail();
	}

	@Override
	protected void addItineraryToCalendar() {
		// Go in reverse order, so that "check in" is shown to the user first
		startActivity(generateHotelCalendarIntent(false));
		startActivity(generateHotelCalendarIntent(true));

		OmnitureTracking.trackHotelConfirmationAddToCalendar();
	}

	private Intent generateHotelCalendarIntent(boolean checkIn) {
		Property property = Db.getTripBucket().getHotel().getBookingResponse().getProperty();
		LocalDate date = checkIn ? Db.getTripBucket().getHotel().getHotelSearchParams().getCheckInDate() :
			Db.getTripBucket().getHotel().getHotelSearchParams().getCheckOutDate();
		HotelBookingResponse bookingResponse = Db.getTripBucket().getHotel().getBookingResponse();
		String confNumber = bookingResponse.getHotelConfNumber();
		String itinNumber = bookingResponse.getItineraryId();

		return AddToCalendarUtils.generateHotelAddToCalendarIntent(getActivity(), property, date, checkIn, confNumber,
				itinNumber);
	}

	@Override
	protected LineOfBusiness getNextBookingItem() {
		if (Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().canBePurchased()) {
			return LineOfBusiness.FLIGHTS;
		}
		else {
			return null;
		}
	}

}
