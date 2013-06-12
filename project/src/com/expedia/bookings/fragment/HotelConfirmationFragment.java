package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.widget.ItinHeaderImageView;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;
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

		Ui.setText(v, R.id.hotel_name_text_view, Db.getHotelSearch().getSelectedProperty().getName());

		// Construct the hotel card
		Property property = Db.getHotelSearch().getSelectedProperty();
		ItinHeaderImageView hotelImageView = Ui.findView(v, R.id.hotel_image_view);
		hotelImageView.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
		Rate selectedRate = Db.getHotelSearch().getAvailability(selectedId).getSelectedRate();
		Media media = HotelUtils.getRoomMedia(property, selectedRate);
		if (media != null) {
			UrlBitmapDrawable.loadImageView(media.getHighResUrls(), hotelImageView,
					R.drawable.bg_itin_placeholder);
		}
		else {
			hotelImageView.setImageResource(R.drawable.bg_itin_placeholder);
		}

		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		int numGuests = params.getNumAdults() + params.getNumChildren();
		String guests = getResources().getQuantityString(R.plurals.number_of_guests, numGuests, numGuests);
		String duration = CalendarUtils.formatDateRange2(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		Ui.setText(v, R.id.stay_summary_text_view, getString(R.string.stay_summary_TEMPLATE, guests, duration));

		// Setup a dropping animation with the hotel card.  Only animate on versions of Android
		// that will allow us to make the animation nice and smooth.
		mHotelCard = Ui.findView(v, R.id.hotel_card);
		if (savedInstanceState == null && Build.VERSION.SDK_INT >= 12) {
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
		if (pos.showHotelCrossSell() && pos.supportsFlights()) {
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
		return Db.getBookingResponse().getItineraryId();
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

		if (Build.VERSION.SDK_INT >= 16) {
			animator.withLayer();
		}
		else {
			mHotelCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			animator.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mHotelCard.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			});
		}

		animator.start();
	}

	//////////////////////////////////////////////////////////////////////////
	// Cross-sell

	private void searchForFlights() {
		// Load the search params
		FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
		flightSearchParams.reset();

		Location loc = new Location();
		loc.setDestinationId(Db.getHotelSearch().getSelectedProperty().getLocation().toLongFormattedString());
		flightSearchParams.setArrivalLocation(loc);

		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		flightSearchParams.setDepartureDate(new Date(params.getCheckInDate()));
		flightSearchParams.setReturnDate(new Date(params.getCheckOutDate()));

		// Go to flights
		NavUtils.goToFlights(getActivity(), true);

		OmnitureTracking.trackHotelConfirmationFlightsXSell(getActivity());
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
		Context context = getActivity();

		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
		Property property = Db.getHotelSearch().getSelectedProperty();
		BookingResponse bookingResponse = Db.getBookingResponse();
		BillingInfo billingInfo = Db.getBillingInfo();
		String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
		Rate rate = Db.getHotelSearch().getAvailability(selectedId).getSelectedRate();
		Rate discountRate = Db.getCouponDiscountRate();

		ShareUtils socialUtils = new ShareUtils(context);
		String subject = socialUtils.getHotelConfirmationShareSubject(searchParams, property);
		String body = socialUtils.getHotelConfirmationShareText(searchParams, property, bookingResponse, billingInfo,
				rate, discountRate);

		SocialUtils.email(context, subject, body);

		OmnitureTracking.trackHotelConfirmationShareEmail(getActivity());
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private void addToCalendar() {
		// Go in reverse order, so that "check in" is shown to the user first
		startActivity(generateHotelCalendarIntent(false));
		startActivity(generateHotelCalendarIntent(true));

		OmnitureTracking.trackHotelConfirmationAddToCalendar(getActivity());
	}

	private Intent generateHotelCalendarIntent(boolean checkIn) {
		Calendar cal = checkIn ? Db.getHotelSearch().getSearchParams().getCheckInDate() : Db.getHotelSearch()
				.getSearchParams().getCheckOutDate();
		Property property = Db.getHotelSearch().getSelectedProperty();
		BookingResponse bookingResponse = Db.getBookingResponse();

		int titleResId = checkIn ? R.string.calendar_hotel_title_checkin_TEMPLATE
				: R.string.calendar_hotel_title_checkout_TEMPLATE;

		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setData(Events.CONTENT_URI);

		intent.putExtra(Events.TITLE, getString(titleResId, property.getName()));
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
		intent.putExtra(Events.EVENT_LOCATION, property.getLocation().toLongFormattedString());

		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(bookingResponse.getHotelConfNumber())) {
			sb.append(getString(R.string.confirmation_number) + ": " + bookingResponse.getHotelConfNumber());
			sb.append("\n");
		}
		sb.append(getString(R.string.itinerary_number) + ": " + bookingResponse.getItineraryId());
		sb.append("\n\n");
		sb.append(ConfirmationUtils.determineContactText(getActivity()));
		intent.putExtra(Events.DESCRIPTION, sb.toString());

		return intent;
	}
}
