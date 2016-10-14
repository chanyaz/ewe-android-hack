package com.expedia.bookings.fragment;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;
import com.squareup.phrase.Phrase;

// We can assume that if this fragment loaded we successfully booked, so most
// data we need to grab is available.
public class FlightConfirmationFragment extends ConfirmationFragment {

	public static final String TAG = FlightConfirmationFragment.class.getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// This can be invoked when the parent activity finishes itself (when it detects missing data, in the case of
		// a background kill). In this case, lets just return a null view because it won't be used anyway. Prevents NPE.
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
		String destinationCity = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());

		// Format the flight cards
		FrameLayout cardContainer = Ui.findView(v, R.id.flight_card_container);

		// Only display the animation the first time the page loads
		if (savedInstanceState == null) {
			LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(),
				R.anim.layout_card_slide);
			cardContainer.setLayoutAnimation(controller);
		}

		Resources res = getResources();

		// Measure the frontmost card - we need to know its height to correctly size the fake cards
		FlightLeg frontmostLeg = trip.getLeg(0);
		FlightLegSummarySection card = Ui.inflate(inflater, R.layout.section_flight_leg_summary,
			cardContainer, false);
		card.setShowNumberTicketsLeftView(false);
		card.bind(trip, frontmostLeg, Db.getBillingInfo(), Db.getTripBucket().getFlight());
		LayoutUtils.setBackgroundResource(card, R.drawable.bg_flight_conf_row);
		card.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		final int cardHeight = card.getMeasuredHeight();

		final float initialOffset = res.getDimension(R.dimen.flight_card_initial_offset);
		final float offset = res.getDimension(R.dimen.flight_card_overlap_offset);
		int numLegs = trip.getLegCount();
		for (int a = numLegs - 1; a >= 0; a--) {
			View view;

			if (a == 0) {
				cardContainer.addView(card);
				view = card;
			}
			else {
				view = new View(getActivity());
				cardContainer.addView(view);

				// Set a custom bg
				LayoutUtils.setBackgroundResource(view, R.drawable.bg_flight_conf_row);

				// For some reason, I can't get this layout to work unless I set an explicit
				// height (probably because of a RelativeLayout circular dependency).
				ViewGroup.LayoutParams params = view.getLayoutParams();
				params.height = cardHeight + Math.round(offset * a);
			}

			// Each card is offset below the last one
			MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
			params.topMargin = (int) (initialOffset + Math.round(offset * (numLegs - 1 - a)));
		}

		if (frontmostLeg.getDaySpan() > 0) {
			ViewGroup actionContainer = Ui.findView(v, R.id.action_container);
			((MarginLayoutParams) actionContainer.getLayoutParams()).topMargin = res
				.getDimensionPixelSize(R.dimen.flight_card_with_span_mask_offset);
		}

		Ui.findView(v, R.id.action_container).setBackgroundResource(R.drawable.bg_confirmation_mask_flights);

		// Fill out all the actions
		Ui.setText(v, R.id.going_to_text_view, getString(R.string.yay_going_somewhere_TEMPLATE, destinationCity));
		if (PointOfSale.getPointOfSale().showHotelCrossSell()) {
			// Check for air attach qualification
			if (Db.getTripBucket() != null && Db.getTripBucket().isUserAirAttachQualified()) {
				Ui.findView(v, R.id.hotels_action_text_view).setVisibility(View.GONE);
				Ui.findView(v, R.id.air_attach_confirmation_banner).setVisibility(View.VISIBLE);
				Ui.setOnClickListener(v, R.id.button_action_layout, new OnClickListener() {
					@Override
					public void onClick(View v) {
						searchForHotels();
						OmnitureTracking.trackAddHotelClick();
					}
				});
				// Set air attach expiration date
				DateTime expirationDate = Db.getTripBucket().getAirAttach().getExpirationDate();
				DateTime currentDate = new DateTime();
				int daysRemaining = JodaUtils.daysBetween(currentDate, expirationDate);
				TextView expirationDateTv = Ui.findView(v, R.id.itin_air_attach_expiration_date_text_view);
				expirationDateTv.setText(Phrase.from(getResources().getQuantityString(R.plurals.days_from_now, daysRemaining))
					.put("days", daysRemaining).format().toString());

			}
			else {
				Ui.findView(v, R.id.hotels_action_text_view).setVisibility(View.VISIBLE);
				Ui.findView(v, R.id.air_attach_confirmation_banner).setVisibility(View.GONE);
				Ui.setText(v, R.id.hotels_action_text_view, getString(R.string.hotels_in_TEMPLATE, destinationCity));
				Ui.setOnClickListener(v, R.id.hotels_action_text_view, new OnClickListener() {
					@Override
					public void onClick(View v) {
						searchForHotels();
						OmnitureTracking.trackCrossSellFlightToHotel();
					}
				});
			}
		}
		else {
			Ui.findView(v, R.id.hotels_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.get_a_room_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.get_a_room_divider).setVisibility(View.GONE);
		}

		if (PointOfSale.getPointOfSale().supports(LineOfBusiness.CARS)) {
			Ui.setText(v, R.id.get_a_room_text_view, getString(R.string.add_to_your_trip));
			Ui.findView(v, R.id.car_divider).setVisibility(View.VISIBLE);
			Ui.findView(v, R.id.cars_action_text_view).setVisibility(View.VISIBLE);
			Ui.setText(v, R.id.cars_action_text_view, getString(R.string.cars_in_TEMPLATE, destinationCity));
			Ui.setOnClickListener(v, R.id.cars_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					searchForCars();
					OmnitureTracking.trackAddCarClick();
				}
			});
		}

		// Show Share button only if sharing is supported.
		if (ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
			Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					share();
				}
			});
		}
		else {
			Ui.findView(v, R.id.share_action_text_view).setVisibility(View.GONE);
		}

		// Show Add to Calendar only if sharing is supported.
		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity()) && ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
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

		// Only display an insurance url if it exists. Currently only present for CA POS.
		final String insuranceUrl = PointOfSale.getPointOfSale().getInsuranceUrl();
		if (!TextUtils.isEmpty(insuranceUrl)) {
			Ui.setOnClickListener(v, R.id.ca_insurance_action_text_view, new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					addInsurance(insuranceUrl);
				}
			});
		}
		else {
			Ui.findView(v, R.id.ca_insurance_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.ca_insurance_divider).setVisibility(View.GONE);
		}

		// We need to capitalize in code because the all_caps field isn't until a later API
		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.get_a_room_text_view));
		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.more_actions_text_view));

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// ConfirmationFragment

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_flight_confirmation;
	}

	@Override
	protected int getActionsLayoutId() {
		return R.layout.include_confirmation_actions_flights;
	}

	@Override
	protected String getItinNumber() {
		return Db.getTripBucket().getFlight().getItinerary().getItineraryNumber();
	}

	//////////////////////////////////////////////////////////////////////////
	// Search for hotels

	private void searchForHotels() {
		HotelSearchParams sp = HotelSearchParams.fromFlightParams(Db.getTripBucket().getFlight());
		NavUtils.goToHotels(getActivity(), sp);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search for cars

	private void searchForCars() {
		CarSearchParam sp = CarDataUtils.fromFlightParams(Db.getTripBucket().getFlight().getFlightTrip());
		NavUtils.goToCars(getActivity(), null, sp, NavUtils.FLAG_OPEN_SEARCH);
	}

	private void searchForActivities() {
		LxSearchParams sp = LXDataUtils.fromFlightParams(getActivity(), Db.getTripBucket().getFlight().getFlightTrip());
		NavUtils.goToActivities(getActivity(), null, sp, NavUtils.FLAG_OPEN_SEARCH);
	}

	//////////////////////////////////////////////////////////////////////////
	// Add insurance

	private void addInsurance(String url) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
		builder.setUrl(url);
		builder.setTheme(R.style.FlightTheme);
		builder.setTitle(R.string.insurance);
		builder.setInjectExpediaCookies(true);
		startActivity(builder.getIntent());
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();

		int travelerCount = Db.getTravelers() == null ? 1 : Db.getTravelers().size();
		ShareUtils shareUtils = new ShareUtils(getActivity());
		String subject = shareUtils.getFlightShareSubject(trip, travelerCount);
		String body = shareUtils.getFlightShareEmail(trip, Db.getTravelers());

		SocialUtils.email(getActivity(), subject, body);

		OmnitureTracking.trackFlightConfirmationShareEmail();
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private void addToCalendar() {
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
}
