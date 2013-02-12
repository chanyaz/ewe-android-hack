package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.Insurance.InsuranceLineOfBusiness;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.maps.GoogleMap;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.ViewUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.maps.FlightMarkerManager;
import com.mobiata.flightlib.maps.MapCameraManager;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightItinCard extends ItinCard<ItinCardDataFlight> {

	private static final String FRAG_TAG_FLIGHT_MAP = "FRAG_TAG_FLIGHT_MAP";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinCardDataFlight mData;
	private TripFlight mTripFlight;
	private FlightMapProvider mFlightMapProvider;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public FlightItinCard(Context context) {
		this(context, null);
	}

	public FlightItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_flight;
	}

	@Override
	public Type getType() {
		return Type.FLIGHT;
	}

	@Override
	public void bind(ItinCardData itinCardData) {
		mData = (ItinCardDataFlight) itinCardData;
		mTripFlight = (TripFlight) mData.getTripComponent();

		super.bind(itinCardData);
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataFlight itinCardData) {
		TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();
		if (tripFlight != null && mData != null && tripFlight.getLegDestinationImageUrl(mData.getLegNumber()) != null) {
			return tripFlight.getLegDestinationImageUrl(mData.getLegNumber());
		}
		else {
			return "";
		}
	}

	@Override
	protected String getHeaderText(ItinCardDataFlight itinCardData) {
		if (mData != null) {
			return mData.getFlightLeg().getLastWaypoint().getAirport().mCity;
		}

		return "Flight Card";
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataFlight itinCardData) {
		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_title_generic, container, false);
		view.setText(getHeaderText(itinCardData));
		return view;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, ItinCardDataFlight itinCardData) {
		View view = inflater.inflate(R.layout.include_itin_card_details_flight, container, false);

		if (mTripFlight != null && mTripFlight.getFlightTrip() != null && mTripFlight.getFlightTrip().getLegCount() > 0) {
			Resources res = getResources();
			FlightLeg leg = mData.getFlightLeg();

			TextView confirmationCodeLabel = Ui.findView(view, R.id.confirmation_code_label);
			TextView passengersLabel = Ui.findView(view, R.id.passengers_label);
			TextView bookingInfoLabel = Ui.findView(view, R.id.booking_info_label);
			TextView insuranceLabel = Ui.findView(view, R.id.insurance_label);

			ViewUtils.setAllCaps(confirmationCodeLabel);
			ViewUtils.setAllCaps(passengersLabel);
			ViewUtils.setAllCaps(bookingInfoLabel);
			ViewUtils.setAllCaps(insuranceLabel);

			//Arrival / Departure times
			Calendar departureTimeCal = leg.getFirstWaypoint().getMostRelevantDateTime();
			Calendar arrivalTimeCal = leg.getLastWaypoint().getMostRelevantDateTime();

			String departureTime = formatTime(departureTimeCal);
			String departureTz = res.getString(R.string.depart_tz_TEMPLATE, departureTimeCal.getTimeZone()
					.getDisplayName(false, TimeZone.SHORT));
			String arrivalTime = formatTime(arrivalTimeCal);
			String arrivalTz = res.getString(R.string.arrive_tz_TEMPLATE,
					arrivalTimeCal.getTimeZone().getDisplayName(false, TimeZone.SHORT));

			Ui.setText(view, R.id.departure_time, departureTime);
			Ui.setText(view, R.id.departure_time_tz, departureTz);
			Ui.setText(view, R.id.arrival_time, arrivalTime);
			Ui.setText(view, R.id.arrival_time_tz, arrivalTz);

			//Traveler names
			StringBuilder travelerSb = new StringBuilder();
			for (Traveler trav : mTripFlight.getTravelers()) {
				travelerSb.append(",");
				travelerSb.append(" ");
				if (!TextUtils.isEmpty(trav.getFirstName())) {
					travelerSb.append(trav.getFirstName().trim());
					travelerSb.append(" ");
				}
				if (!TextUtils.isEmpty(trav.getMiddleName())) {
					travelerSb.append(trav.getMiddleName().trim());
					travelerSb.append(" ");
				}
				if (!TextUtils.isEmpty(trav.getLastName())) {
					travelerSb.append(trav.getLastName().trim());
				}
			}
			String travString = travelerSb.toString().replaceFirst(",", "").trim();
			Ui.setText(view, R.id.passenger_name_list, travString);

			//Booking info (View receipt and polocies)
			View bookingInfo = Ui.findView(view, R.id.booking_info);
			final String infoUrl = mTripFlight.getParentTrip().getDetailsUrl();
			if (!TextUtils.isEmpty(infoUrl)) {
				bookingInfo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent bookingInfoIntent = WebViewActivity.getIntent(getContext(), infoUrl,
								R.style.FlightTheme, R.string.booking_info, true);
						getContext().startActivity(bookingInfoIntent);
					}
				});
			}

			//Confirmation code
			if (mTripFlight.getConfirmations() != null && mTripFlight.getConfirmations().size() > 0
					&& !TextUtils.isEmpty(mTripFlight.getConfirmations().get(0).getConfirmationCode())) {
				//TODO: Confirmation codes come in an array, I think there should only ever be one, but in the event of more than one, we should do something...
				Ui.setText(view, R.id.confirmation_code, mTripFlight.getConfirmations().get(0).getConfirmationCode());
			}
			else {
				Ui.setText(view, R.id.confirmation_code, R.string.missing_booking_code);
			}

			//Insurance
			boolean hasInsurance = (this.mTripFlight.getParentTrip().getTripInsurance() != null && this.mTripFlight
					.getParentTrip().getTripInsurance().size() > 0);
			int insuranceVisibility = hasInsurance ? View.VISIBLE : View.GONE;
			View insuranceContainer = Ui.findView(view, R.id.insurance_container);
			insuranceLabel.setVisibility(insuranceVisibility);
			insuranceContainer.setVisibility(insuranceVisibility);
			if (hasInsurance) {
				Insurance insurance = null;
				for (int i = 0; i < this.mTripFlight.getParentTrip().getTripInsurance().size(); i++) {
					if (mTripFlight.getParentTrip().getTripInsurance().get(i).getLineOfBusiness()
							.equals(InsuranceLineOfBusiness.AIR)) {
						insurance = mTripFlight.getParentTrip().getTripInsurance().get(i);
					}
				}
				if (insurance != null) {
					TextView insuranceName = Ui.findView(view, R.id.insurance_name);
					insuranceName.setText(insurance.getPolicyName());
					final Insurance finalInsurance = insurance;

					View insuranceLinkView = Ui.findView(view, R.id.insurance_button);
					insuranceLinkView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent insuranceIntent = WebViewActivity.getIntent(getContext(),
									finalInsurance.getTermsUrl(), R.style.FlightTheme, R.string.insurance, true);
							getContext().startActivity(insuranceIntent);
						}
					});
				}
			}

			//Add the flight stuff
			ViewGroup flightLegContainer = Ui.findView(view, R.id.flight_leg_container);
			for (int j = 0; j < leg.getSegmentCount(); j++) {
				Flight segment = leg.getSegment(j);

				boolean isFirstSegment = (j == 0);
				boolean isLastSegment = (j == leg.getSegmentCount() - 1);

				if (isFirstSegment) {
					flightLegContainer.addView(getWayPointView(segment.mOrigin, WaypointType.DEPARTURE, inflater));
					flightLegContainer.addView(getDividerView());
				}

				flightLegContainer.addView(getFlightView(segment, departureTimeCal, arrivalTimeCal, inflater));
				flightLegContainer.addView(getDividerView());

				if (isLastSegment) {
					flightLegContainer.addView(getWayPointView(segment.mDestination, WaypointType.ARRIVAL, inflater));
				}
				else {
					flightLegContainer.addView(getWayPointView(segment.mDestination, WaypointType.LAYOVER, inflater));
					flightLegContainer.addView(getDividerView());
				}

			}

		}

		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataFlight itinCardData) {

		if (mData == null || mData.getStartDate() == null || mData.getEndDate() == null) {
			//Bad data (we don't show any summary view in this case)
			return null;
		}

		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_summary_flight, container, false);
		Resources res = getResources();
		Calendar now = Calendar.getInstance();

		if (mData.getStartDate().getCalendar().before(now) && mData.getEndDate().getCalendar().before(now)) {
			//flight complete
			String dateStr = DateUtils.formatDateTime(getContext(), mData.getEndDate().getCalendar().getTimeInMillis(),
					DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR);
			view.setText(res.getString(R.string.flight_landed_on_TEMPLATE, dateStr));
		}
		else if (mData.getStartDate().getCalendar().before(now) && mData.getEndDate().getCalendar().after(now)) {
			//flight in progress
			view.setText(res.getString(R.string.flight_in_progress));
		}
		else if (mData.getStartDate().getCalendar().getTimeInMillis() - now.getTimeInMillis() > DateUtils.DAY_IN_MILLIS) {
			//More than 24 hours away
			String dateStr = DateUtils.formatDateTime(getContext(), mData.getStartDate().getCalendar()
					.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR);
			view.setText(res.getString(R.string.flight_departs_on_TEMPLATE, dateStr));
		}
		else {
			//Less than 24 hours in the future...
			long duration = mData.getStartDate().getCalendar().getTimeInMillis() - now.getTimeInMillis();
			int minutes = (int) ((int) (duration % DateUtils.HOUR_IN_MILLIS) / (DateUtils.MINUTE_IN_MILLIS));
			int hours = (int) Math.floor(duration / DateUtils.HOUR_IN_MILLIS);

			view.setText(res.getString(R.string.flight_departs_in_hours_minutes_TEMPLATE,
					res.getQuantityString(R.plurals.hour_span, hours, hours),
					res.getQuantityString(R.plurals.minute_span, minutes, minutes)));
		}
		return view;
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected SummaryButton getSummaryLeftButton(ItinCardDataFlight itinCardData) {
		return new SummaryButton(R.drawable.ic_direction, getResources().getString(R.string.directions).toUpperCase(),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						//TODO: Investigate compatibility
						Airport airport = mData.getFlightLeg().getFirstWaypoint().getAirport();
						String format = "geo:0,0?q=%f,%f (%s)";
						String uriStr = String.format(format, airport.getLatitude(), airport.getLongitude(),
								airport.mName);
						Uri airportUri = Uri.parse(uriStr);
						Intent intent = new Intent(Intent.ACTION_VIEW, airportUri);

						intent.setComponent(new ComponentName("com.google.android.apps.maps",
								"com.google.android.maps.MapsActivity"));

						getContext().startActivity(intent);
					}
				});
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected SummaryButton getSummaryRightButton(ItinCardDataFlight itinCardData) {
		return new SummaryButton(R.drawable.ic_add_event, getResources().getString(R.string.add_event).toUpperCase(),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						//TODO: Investigate compatibility... works on my 2.2 and 4.2 devices with default android cal
						Resources res = getResources();
						Calendar startCal = mData.getStartDate().getCalendar();
						Calendar endCal = mData.getEndDate().getCalendar();
						Intent intent = new Intent(Intent.ACTION_EDIT);
						intent.setType("vnd.android.cursor.item/event");
						intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
						intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.getTimeInMillis());
						intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis());
						intent.putExtra(
								Events.TITLE,
								res.getString(R.string.flight_to_TEMPLATE, mData.getFlightLeg().getLastWaypoint()
										.getAirport().mCity));
						getContext().startActivity(intent);
					}
				});
	}

	@Override
	protected void onShareButtonClick(ItinCardDataFlight itinCardData) {
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////
	
	public void attachFlightMap() {
		if (mFlightMapProvider != null) {
			setHardwareAccelerationEnabled(false);
			ViewGroup mapContainer = Ui.findView(this, R.id.flight_map_container);
			mapContainer.setId(R.id.itin_flight_map_container_active);

			FragmentManager fragManager = mFlightMapProvider.getFlightMapFragmentManager();
			SupportMapFragment mapFrag = mFlightMapProvider.getFlightMapFragment(new MapInstantiatedListener() {
				@Override
				public void onMapInstantiated(SupportMapFragment mapFrag) {
					GoogleMap map = mapFrag.getMap();
					if (map != null) {
						map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
						map.setMyLocationEnabled(false);
						map.getUiSettings().setAllGesturesEnabled(false);
						map.getUiSettings().setZoomControlsEnabled(false);
						map.getUiSettings().setZoomGesturesEnabled(false);
						map.getUiSettings().setMyLocationButtonEnabled(false);

						FlightMarkerManager flightMarkerManager = new FlightMarkerManager(map,
								R.drawable.map_pin_normal, R.drawable.map_pin_normal, R.drawable.map_pin_sale);

						MapCameraManager mapCameraManager = new MapCameraManager(map);
						flightMarkerManager.setFlights(mData.getFlightLeg().getSegments());
						mapCameraManager.showFlight(mData.getFlightLeg().getSegment(0), FlightItinCard.this.getWidth(),
								getResources()
										.getDimensionPixelSize(R.dimen.itin_map_total_size), 40);

					}

				}
			});

			FragmentTransaction transaction = fragManager.beginTransaction();
			if (!mapFrag.isAdded()) {
				transaction.add(R.id.itin_flight_map_container_active, mapFrag, FRAG_TAG_FLIGHT_MAP);
				transaction.commit();
			}
		}
	}

	@SuppressLint("NewApi")
	public void removeFlightMap() {
		if (mFlightMapProvider != null) {
			setHardwareAccelerationEnabled(true);
			FragmentManager fragManager = mFlightMapProvider.getFlightMapFragmentManager();
			SupportMapFragment mapFrag = (SupportMapFragment) fragManager.findFragmentByTag(FRAG_TAG_FLIGHT_MAP);
			if (mapFrag != null) {
				FragmentTransaction transaction = fragManager.beginTransaction();
				transaction.remove(mapFrag);
				transaction.commitAllowingStateLoss();
				fragManager.executePendingTransactions();
			}

			ViewGroup mapContainer = Ui.findView(this, R.id.itin_flight_map_container_active);

			if (mapContainer != null) {
				mapContainer.setId(R.id.flight_map_container);
				mapContainer.removeAllViews();
			}
		}
	}

	public void setFlightMapProvider(FlightMapProvider flightMapProvider) {
		mFlightMapProvider = flightMapProvider;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private enum WaypointType {
		DEPARTURE, ARRIVAL, LAYOVER
	}

	private View getWayPointView(Waypoint waypoint, WaypointType type, LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.snippet_itin_waypoint_row, null);
		Resources res = getResources();

		ImageView waypointTypeIcon = Ui.findView(v, R.id.waypoint_type_image);
		switch (type) {
		case DEPARTURE:
			waypointTypeIcon.setImageResource(R.drawable.ic_departure_details);
			break;
		case LAYOVER:
			//TODO: We are waiting on the asset for layovers
			waypointTypeIcon.setImageResource(R.drawable.ic_departure_details);
			break;
		case ARRIVAL:
			waypointTypeIcon.setImageResource(R.drawable.ic_arrival_details);
			break;
		}

		String airportName = waypoint.getAirport().mName;
		Ui.setText(v, R.id.layover_airport_name, airportName);
		if (type.equals(WaypointType.LAYOVER)) {
			//TODO: Need to get a different set of gates, so we will need another waypoint object...

			if (TextUtils.isEmpty(waypoint.getTerminal()) || TextUtils.isEmpty(waypoint.getGate())) {
				Ui.setText(v, R.id.layover_terminal_gate_one, R.string.no_terminal_gate_information);
				Ui.findView(v, R.id.layover_terminal_gate_two).setVisibility(View.GONE);
			}
			else {
				String arrivalGate = res.getString(R.string.arrival_terminal_TEMPLATE, waypoint.getTerminal(),
						waypoint.getGate());
				String departureGate = res.getString(R.string.arrival_terminal_TEMPLATE, waypoint.getTerminal(),
						waypoint.getGate());

				Ui.setText(v, R.id.layover_terminal_gate_one, arrivalGate);
				Ui.setText(v, R.id.layover_terminal_gate_two, departureGate);
			}

		}
		else {
			Ui.findView(v, R.id.layover_terminal_gate_two).setVisibility(View.GONE);

			if (TextUtils.isEmpty(waypoint.getTerminal()) || TextUtils.isEmpty(waypoint.getGate())) {
				Ui.setText(v, R.id.layover_terminal_gate_one, R.string.no_terminal_gate_information);
			}
			else {
				String termGate = res.getString(R.string.generic_terminal_TEMPLATE, waypoint.getTerminal(),
						waypoint.getGate());
				Ui.setText(v, R.id.layover_terminal_gate_one, termGate);
			}
		}

		return v;
	}

	private View getFlightView(Flight flight, Calendar minTime, Calendar maxTime, LayoutInflater inflater) {
		FlightLegSummarySection v = (FlightLegSummarySection) inflater.inflate(
				R.layout.section_flight_leg_summary_itin, null);
		v.bindFlight(flight, minTime, maxTime);
		return v;
	}

	private View getDividerView() {
		//TODO: WHY U NO USE DIMENS!?!?!
		View v = new View(this.getContext());
		v.setBackgroundColor(getResources().getColor(R.color.itin_divider_color));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
		lp.leftMargin = 10;
		lp.rightMargin = 10;
		lp.topMargin = 10;
		lp.bottomMargin = 10;
		v.setLayoutParams(lp);
		return v;
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(getContext());
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}
	
	@SuppressLint("NewApi")
	private void setHardwareAccelerationEnabled(boolean enabled){
		if (AndroidUtils.getSdkVersion() >= 11) {
				int layerType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
				
				setLayerType(layerType, null);
				Ui.findView(this, R.id.card_layout).setLayerType(layerType, null);
				Ui.findView(this, R.id.itin_type_image_view).setLayerType(layerType, null);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////
	public interface FlightMapProvider {
		public FragmentManager getFlightMapFragmentManager();

		public SupportMapFragment getFlightMapFragment(MapInstantiatedListener listener);
	}

	public interface MapInstantiatedListener {
		public void onMapInstantiated(SupportMapFragment mapFrag);
	}
}