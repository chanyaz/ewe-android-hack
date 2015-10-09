package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.ViewUtils;

public class HotelConfirmationFragment extends ConfirmationFragment {

	public static final String TAG = HotelConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	private ViewGroup mHotelCard;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// This can be invoked when the parent activity finishes itself (when it detects missing data, in the case of
		// a background kill). In this case, lets just return a null view because it won't be used anyway. Prevents NPE.
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		Property property = Db.getTripBucket().getHotel().getBookingResponse().getProperty();
		Ui.setText(v, R.id.hotel_name_text_view, property.getName());

		// Construct the hotel card
		ImageView hotelImageView = Ui.findView(v, R.id.hotel_image_view);
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		headerBitmapDrawable.setCornerMode(CornerMode.ALL);
		headerBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		headerBitmapDrawable.setOverlayDrawable(getResources().getDrawable(R.drawable.card_top_lighting));
		hotelImageView.setImageDrawable(headerBitmapDrawable);

		HotelMedia hotelMedia = HotelUtils.getRoomMedia(Db.getTripBucket().getHotel());
		int placeholderId = Ui.obtainThemeResID(getActivity(), R.attr.skin_hotelConfirmationPlaceholderDrawable);
		if (hotelMedia != null) {
			new PicassoHelper.Builder(getActivity()).setPlaceholder(placeholderId)
				.setTarget(headerBitmapDrawable.getCallBack()).build().load(hotelMedia.getHighResUrls());
		}
		else {
			headerBitmapDrawable.setBitmap(BitmapFactory.decodeResource(getResources(), placeholderId));
		}

		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		int numGuests = params.getNumAdults() + params.getNumChildren();
		String guests = getResources().getQuantityString(R.plurals.number_of_guests, numGuests, numGuests);
		String duration = DateFormatUtils.formatRangeDateToDate(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		Ui.setText(v, R.id.stay_summary_text_view, getString(R.string.stay_summary_TEMPLATE, guests, duration));

		// Setup a dropping animation with the hotel card. Only on the first show, not on rotation
		if (savedInstanceState == null) {
			mHotelCard = Ui.findView(v, R.id.hotel_card);
			mHotelCard.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					mHotelCard.getViewTreeObserver().removeOnPreDrawListener(this);
					animateHotelCard();
					return false;
				}
			});
		}

		Ui.findView(v, R.id.action_container).setBackgroundResource(R.drawable.bg_confirmation_mask_hotels);

		PointOfSale pos = PointOfSale.getPointOfSale();
		// 1373: Need to hide cross sell until we can fix the poor search results
		// TODO: 1370: When you enable this, please make sure to disable flights cross sell if its a VSC build.
		//if (pos.showHotelCrossSell() && pos.supportsFlights()) {
		if (false) {
			ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.get_there_text_view));

			String city = property.getLocation().getCity();
			Ui.setText(v, R.id.flights_action_text_view, getString(R.string.flights_to_TEMPLATE, city));
			Ui.setOnClickListener(v, R.id.flights_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					searchForFlights();
				}
			});
		}
		else {
			Ui.findView(v, R.id.get_there_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.get_there_text_divider).setVisibility(View.GONE);
			Ui.findView(v, R.id.flights_action_text_view).setVisibility(View.GONE);

			// #1391: If there are no additional actions above, then only show "actions" instead of "more actions"
			Ui.setText(v, R.id.more_actions_text_view, R.string.actions);
		}

		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.more_actions_text_view));

		Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});
		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity())) {
			Ui.setOnClickListener(v, R.id.calendar_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					addToCalendar();
				}
			});
		}
		else {
			Ui.findView(v, R.id.calendar_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.calendar_divider).setVisibility(View.GONE);
		}

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// ConfirmationFragment

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_hotel_confirmation;
	}

	@Override
	protected int getActionsLayoutId() {
		return R.layout.include_confirmation_actions_hotels;
	}

	@Override
	protected String getItinNumber() {
		return Db.getTripBucket().getHotel().getBookingResponse().getItineraryId();
	}

	//////////////////////////////////////////////////////////////////////////
	// Animation

	private void animateHotelCard() {
		// Animate the card from -height to 0
		mHotelCard.setTranslationY(-mHotelCard.getHeight());

		ViewPropertyAnimator animator = mHotelCard.animate();
		animator.translationY(0);
		animator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
		animator.setInterpolator(new OvershootInterpolator());
		animator.withLayer();
		animator.start();
	}

	//////////////////////////////////////////////////////////////////////////
	// Cross-sell

	private void searchForFlights() {
		// Load the search params
		FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
		flightSearchParams.reset();

		Property property = Db.getTripBucket().getHotel().getBookingResponse().getProperty();

		Location loc = new Location();
		loc.setDestinationId(property.getLocation().toLongFormattedString());
		flightSearchParams.setArrivalLocation(loc);

		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		flightSearchParams.setDepartureDate(params.getCheckInDate());
		flightSearchParams.setReturnDate(params.getCheckOutDate());

		// Go to flights
		NavUtils.goToFlights(getActivity(), true);

		OmnitureTracking.trackHotelConfirmationFlightsXSell();
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
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

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private void addToCalendar() {
		// Go in reverse order, so that "check in" is shown to the user first
		startActivity(generateHotelCalendarIntent(false));
		startActivity(generateHotelCalendarIntent(true));

		OmnitureTracking.trackHotelConfirmationAddToCalendar();
	}

	private Intent generateHotelCalendarIntent(boolean checkIn) {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		HotelBookingResponse bookingResponse = Db.getTripBucket().getHotel().getBookingResponse();
		Property property = bookingResponse.getProperty();
		LocalDate date = checkIn ? hotel.getHotelSearchParams().getCheckInDate() :
			hotel.getHotelSearchParams().getCheckOutDate();
		String confNumber = bookingResponse.getHotelConfNumber();
		String itinNumber = bookingResponse.getItineraryId();

		return AddToCalendarUtils.generateHotelAddToCalendarIntent(getActivity(), property, date, checkIn, confNumber,
				itinNumber);
	}
}
