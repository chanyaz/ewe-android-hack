package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.widget.ItinHeaderImageView;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;

public class HotelConfirmationFragment extends ConfirmationFragment {

	public static final String TAG = HotelConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// This can be invoked when the parent activity finishes itself (when it detects missing data, in the case of
		// a background kill). In this case, lets just return a null view because it won't be used anyway. Prevents NPE.
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		Ui.setText(v, R.id.hotel_name_text_view, Db.getSelectedProperty().getName());

		// Construct the hotel card
		Property property = Db.getSelectedProperty();
		ItinHeaderImageView hotelImageView = Ui.findView(v, R.id.hotel_image_view);
		hotelImageView.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		UrlBitmapDrawable.loadImageView(property.getThumbnail().getHighResUrls(), hotelImageView,
				R.drawable.bg_itin_placeholder);

		SearchParams params = Db.getSearchParams();
		int numGuests = params.getNumAdults() + params.getNumChildren();
		String guests = getResources().getQuantityString(R.plurals.number_of_guests, numGuests, numGuests);
		String duration = CalendarUtils.formatDateRange2(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		Ui.setText(v, R.id.stay_summary_text_view, getString(R.string.stay_summary_TEMPLATE, guests, duration));

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
	// Cross-sell

	private void searchForFlights() {
		// Load the search params
		FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
		flightSearchParams.reset();

		Location loc = new Location();
		loc.setDestinationId(Db.getSelectedProperty().getLocation().toLongFormattedString());
		flightSearchParams.setArrivalLocation(loc);

		SearchParams params = Db.getSearchParams();
		flightSearchParams.setDepartureDate(new Date(params.getCheckInDate()));
		flightSearchParams.setReturnDate(new Date(params.getCheckOutDate()));

		// Go to flights
		NavUtils.goToFlights(getActivity(), true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
		Context context = getActivity();

		SearchParams searchParams = Db.getSearchParams();
		Property property = Db.getSelectedProperty();
		BookingResponse bookingResponse = Db.getBookingResponse();
		BillingInfo billingInfo = Db.getBillingInfo();
		Rate rate = Db.getSelectedRate();
		Rate discountRate = Db.getCouponDiscountRate();

		ShareUtils socialUtils = new ShareUtils(context);
		String subject = socialUtils.getHotelConfirmationShareSubject(searchParams, property);
		String body = socialUtils.getHotelConfirmationShareText(searchParams, property, bookingResponse, billingInfo,
				rate, discountRate);

		SocialUtils.email(context, subject, body);

		// Track the share
		Log.d("Tracking \"CKO.CP.ShareBooking\" onClick");
		OmnitureTracking.trackSimpleEvent(context, null, null, "Shopper", "CKO.CP.ShareBooking");
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private void addToCalendar() {
		// TODO
		Ui.showToast(getActivity(), "TODO: #1254");
	}
}
