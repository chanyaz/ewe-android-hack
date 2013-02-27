package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.TerminalMapActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.FlightConfirmation;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.utils.CalendarAPIUtils;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.maps.GoogleMap;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.ViewUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Delay;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.maps.FlightMarkerManager;
import com.mobiata.flightlib.maps.MapCameraManager;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class FlightItinCard extends ItinCard<ItinCardDataFlight> {

	private static final String FRAG_TAG_FLIGHT_MAP = "FRAG_TAG_FLIGHT_MAP";
	private static final String FRAG_TAG_AIRPORT_ACTION_CHOOSER = "FRAG_TAG_AIRPORT_ACTION_CHOOSER";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

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
	protected String getShareSubject(ItinCardDataFlight itinCardData) {
		if (itinCardData != null && itinCardData.getFlightLeg() != null
				&& itinCardData.getFlightLeg().getLastWaypoint() != null
				&& itinCardData.getFlightLeg().getLastWaypoint().getAirport() != null) {
			String template = getContext().getString(R.string.share_template_subject_flight);
			String destination = itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity;
			Calendar startDate = itinCardData.getFlightLeg().getFirstWaypoint().getMostRelevantDateTime();
			String deparuteDate = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM).format(startDate.getTime());

			return String.format(template, destination, deparuteDate);
		}
		return null;
	}

	@Override
	protected String getShareTextShort(ItinCardDataFlight itinCardData) {
		if (itinCardData != null && itinCardData.getFlightLeg() != null
				&& itinCardData.getFlightLeg().getLastWaypoint() != null
				&& itinCardData.getFlightLeg().getLastWaypoint().getAirport() != null) {

			FlightLeg leg = itinCardData.getFlightLeg();
			String airlineAndFlightNumber = FormatUtils.formatFlightNumberShort(
					leg.getSegment(leg.getSegmentCount() - 1), getContext());
			String destinationCity = leg.getLastWaypoint().getAirport().mCity;
			String destinationAirportCode = leg.getLastWaypoint().getAirport().mAirportCode;
			String originAirportCode = leg.getFirstWaypoint().getAirport().mAirportCode;
			String destinationGateTerminal = getTerminalGateString(leg.getLastWaypoint());

			Calendar departureCal = leg.getFirstWaypoint().getMostRelevantDateTime();
			Calendar arrivalCal = leg.getLastWaypoint().getMostRelevantDateTime();

			//The story contains format strings, but we don't want to bone our international customers
			DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
			DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM);
			DateFormat dateTimeFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

			if (PointOfSale.getPointOfSale().getThreeLetterCountryCode().equalsIgnoreCase("USA")) {
				String dateFormatStr = "M/dd/yy";
				String timeFormatStr = "h:mma zz";
				String dateTimeFormatStr = timeFormatStr + " " + dateFormatStr;

				((SimpleDateFormat) dateFormat).applyPattern(dateFormatStr);
				((SimpleDateFormat) timeFormat).applyPattern(timeFormatStr);
				((SimpleDateFormat) dateTimeFormat).applyPattern(dateTimeFormatStr);
			}

			dateFormat.setTimeZone(departureCal.getTimeZone());
			timeFormat.setTimeZone(departureCal.getTimeZone());
			dateTimeFormat.setTimeZone(departureCal.getTimeZone());
			String departureDate = dateFormat.format(departureCal.getTime());
			String departureTime = timeFormat.format(departureCal.getTime());
			String departureDateTime = dateTimeFormat.format(departureCal.getTime());

			dateFormat.setTimeZone(arrivalCal.getTimeZone());
			timeFormat.setTimeZone(arrivalCal.getTimeZone());
			dateTimeFormat.setTimeZone(arrivalCal.getTimeZone());
			String arrivalTime = timeFormat.format(arrivalCal.getTime());
			String arrivalDateTime = dateTimeFormat.format(arrivalCal.getTime());

			//single day
			if (leg.getDaySpan() == 0) {
				String template = getContext().getString(R.string.share_template_short_flight_sameday);

				return String.format(template, airlineAndFlightNumber, destinationCity, departureDate,
						originAirportCode,
						departureTime, destinationAirportCode, arrivalTime, destinationGateTerminal);
			}
			//multi day
			else {
				String template = getContext().getString(R.string.share_template_short_flight_multiday);

				return String.format(template, airlineAndFlightNumber, destinationCity, originAirportCode,
						departureDateTime, destinationAirportCode, arrivalDateTime, destinationGateTerminal);
			}
		}
		return null;
	}

	@Override
	protected String getShareTextLong(ItinCardDataFlight itinCardData) {
		if (itinCardData != null && itinCardData.getFlightLeg() != null
				&& itinCardData.getFlightLeg().getLastWaypoint() != null
				&& itinCardData.getFlightLeg().getLastWaypoint().getAirport() != null) {

			FlightLeg leg = itinCardData.getFlightLeg();
			TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();

			String airlineAndFlightNumber = FormatUtils.formatFlightNumberFull(
					leg.getSegment(leg.getSegmentCount() - 1), getContext());

			String originCity = leg.getFirstWaypoint().getAirport().mCity;
			String destinationCity = leg.getLastWaypoint().getAirport().mCity;
			String originAirportCode = leg.getFirstWaypoint().getAirport().mCity;
			String destinationAirportCode = leg.getLastWaypoint().getAirport().mAirportCode;
			String originTerminalGate = getTerminalGateString(leg.getFirstWaypoint());
			String destinationTerminalGate = getTerminalGateString(leg.getLastWaypoint());

			Calendar departureCal = leg.getFirstWaypoint().getMostRelevantDateTime();
			Calendar arrivalCal = leg.getLastWaypoint().getMostRelevantDateTime();

			//The story contains format strings, but we don't want to bone our international customers
			DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
			DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM);

			if (PointOfSale.getPointOfSale().getThreeLetterCountryCode().equalsIgnoreCase("USA")) {
				String dateFormatStr = "EEEE MMMM dd, yyyy";
				String timeFormatStr = "h:mm a zz";

				((SimpleDateFormat) dateFormat).applyPattern(dateFormatStr);
				((SimpleDateFormat) timeFormat).applyPattern(timeFormatStr);
			}

			dateFormat.setTimeZone(departureCal.getTimeZone());
			timeFormat.setTimeZone(departureCal.getTimeZone());
			String departureDate = dateFormat.format(departureCal.getTime());
			String departureTime = timeFormat.format(departureCal.getTime());

			dateFormat.setTimeZone(arrivalCal.getTimeZone());
			timeFormat.setTimeZone(arrivalCal.getTimeZone());
			String arrivalDate = dateFormat.format(arrivalCal.getTime());
			String arrivalTime = timeFormat.format(arrivalCal.getTime());

			//Traveler names
			StringBuilder travelerSb = new StringBuilder();
			for (Traveler trav : tripFlight.getTravelers()) {
				if (travelerSb.length() > 0) {
					travelerSb.append("\n");
				}
				travelerSb.append(trav.getFullName());
			}
			String travString = travelerSb.toString();

			//single day
			if (leg.getDaySpan() == 0) {
				String template = getContext().getString(R.string.share_template_long_flight_sameday);

				return String.format(template, originCity, destinationCity, airlineAndFlightNumber, departureDate,
						originAirportCode, departureTime, originTerminalGate, destinationAirportCode, arrivalTime,
						destinationTerminalGate, travString);

			}
			//multi day
			else {
				String template = getContext().getString(R.string.share_template_long_flight_multiday);

				return String.format(template, originCity, destinationCity, airlineAndFlightNumber,
						originAirportCode, departureTime, departureDate, originTerminalGate, destinationAirportCode,
						arrivalTime, arrivalDate, destinationTerminalGate, travString);
			}
		}
		return null;
	}

	@Override
	public void bind(ItinCardData itinCardData) {
		super.bind(itinCardData);
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataFlight itinCardData) {
		TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();
		if (tripFlight != null && itinCardData != null
				&& tripFlight.getLegDestinationImageUrl(itinCardData.getLegNumber()) != null) {
			return tripFlight.getLegDestinationImageUrl(itinCardData.getLegNumber());
		}
		else {
			return "";
		}
	}

	@Override
	protected String getHeaderText(ItinCardDataFlight itinCardData) {
		if (itinCardData != null && itinCardData.getFlightLeg() != null
				&& itinCardData.getFlightLeg().getLastWaypoint() != null
				&& itinCardData.getFlightLeg().getLastWaypoint().getAirport() != null
				&& !TextUtils.isEmpty(itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity)) {
			return itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity;
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

		ItinCardDataFlight data = (ItinCardDataFlight) itinCardData;
		TripFlight tripFlight = (TripFlight) data.getTripComponent();

		if (tripFlight != null && tripFlight.getFlightTrip() != null && tripFlight.getFlightTrip().getLegCount() > 0) {
			Resources res = getResources();
			FlightLeg leg = data.getFlightLeg();

			TextView confirmationCodeLabel = Ui.findView(view, R.id.confirmation_code_label);
			TextView passengersLabel = Ui.findView(view, R.id.passengers_label);
			TextView bookingInfoLabel = Ui.findView(view, R.id.booking_info_label);
			TextView insuranceLabel = Ui.findView(view, R.id.insurance_label);

			TextView departureTimeTv = Ui.findView(view, R.id.departure_time);
			TextView departureTimeTzTv = Ui.findView(view, R.id.departure_time_tz);
			TextView arrivalTimeTv = Ui.findView(view, R.id.arrival_time);
			TextView arrivalTimeTzTv = Ui.findView(view, R.id.arrival_time_tz);
			TextView passengerNameListTv = Ui.findView(view, R.id.passenger_name_list);
			TextView confirmationCodeListTv = Ui.findView(view, R.id.confirmation_code);

			View bookingInfoView = Ui.findView(view, R.id.booking_info);
			ViewGroup insuranceContainer = Ui.findView(view, R.id.insurance_container);
			ViewGroup flightLegContainer = Ui.findView(view, R.id.flight_leg_container);

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

			departureTimeTv.setText(departureTime);
			departureTimeTzTv.setText(departureTz);
			arrivalTimeTv.setText(arrivalTime);
			arrivalTimeTzTv.setText(arrivalTz);

			//Traveler names
			StringBuilder travelerSb = new StringBuilder();
			for (Traveler trav : tripFlight.getTravelers()) {
				if (travelerSb.length() > 0) {
					travelerSb.append(",");
					travelerSb.append(" ");
				}
				travelerSb.append(trav.getFullName());
			}
			String travString = travelerSb.toString().trim();
			passengerNameListTv.setText(travString);

			//Booking info (View receipt and polocies)
			final String infoUrl = tripFlight.getParentTrip().getDetailsUrl();
			if (!TextUtils.isEmpty(infoUrl)) {
				bookingInfoView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
						builder.setUrl(infoUrl);
						builder.setTitle(R.string.booking_info);
						builder.setTheme(R.style.FlightTheme);
						getContext().startActivity(builder.getIntent());
					}
				});
			}

			//Confirmation code list
			if (tripFlight.getConfirmations() != null && tripFlight.getConfirmations().size() > 0) {
				List<FlightConfirmation> confirmations = tripFlight.getConfirmations();
				String confirmationString = confirmations.get(0).getConfirmationCode();
				for (int i = 1; i < confirmations.size(); i++) {
					if (!TextUtils.isEmpty(confirmations.get(i).getConfirmationCode())) {
						confirmationString += ", " + confirmations.get(i).getConfirmationCode();
					}
				}
				confirmationCodeListTv.setText(confirmationString);

				final String clipboardText = confirmationString;
				confirmationCodeListTv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ClipboardUtils.setText(getContext(), clipboardText);
						Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
					}
				});
			}
			else {
				confirmationCodeListTv.setText(R.string.missing_booking_code);
			}

			//Insurance
			boolean hasInsurance = hasInsurance();
			int insuranceVisibility = hasInsurance ? View.VISIBLE : View.GONE;
			insuranceLabel.setVisibility(insuranceVisibility);
			insuranceContainer.setVisibility(insuranceVisibility);
			if (hasInsurance) {
				addInsuranceRows(inflater, insuranceContainer);
			}

			//Add the flight stuff
			Flight prevSegment = null;
			int divPadding = getResources().getDimensionPixelSize(R.dimen.itin_flight_segment_divider_padding);
			for (int j = 0; j < leg.getSegmentCount(); j++) {
				Flight segment = leg.getSegment(j);
				boolean isFirstSegment = (j == 0);
				boolean isLastSegment = (j == leg.getSegmentCount() - 1);

				if (isFirstSegment) {
					flightLegContainer
							.addView(getWayPointView(segment.mOrigin, null, WaypointType.DEPARTURE, inflater));
					flightLegContainer.addView(getHorizontalDividerView(divPadding));
				}
				else {
					flightLegContainer.addView(getWayPointView(prevSegment.mDestination, segment.mOrigin,
							WaypointType.LAYOVER, inflater));
					flightLegContainer.addView(getHorizontalDividerView(divPadding));
				}

				flightLegContainer.addView(getFlightView(segment, departureTimeCal, arrivalTimeCal, inflater));
				flightLegContainer.addView(getHorizontalDividerView(divPadding));

				if (isLastSegment) {
					flightLegContainer.addView(getWayPointView(segment.mDestination, null, WaypointType.ARRIVAL,
							inflater));
				}

				prevSegment = segment;
			}
		}

		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataFlight itinCardData) {

		if (itinCardData == null || itinCardData.getStartDate() == null || itinCardData.getEndDate() == null) {
			//Bad data (we don't show any summary view in this case)
			return null;
		}

		View view = inflater.inflate(R.layout.include_itin_card_summary_flight, container, false);
		TextView topLine = Ui.findView(view, R.id.flight_status_top_line);
		TextView bottomLine = Ui.findView(view, R.id.flight_status_bottom_line);
		ImageView bulb = Ui.findView(view, R.id.flight_status_bulb);

		Resources res = getResources();
		Calendar now = Calendar.getInstance();
		Flight flight = itinCardData.getMostRelevantFlightSegment();

		if (flight.isRedAlert()) {
			if (Flight.STATUS_CANCELLED.equals(flight.mStatusCode)) {
				topLine.setText(res.getString(R.string.flight_to_city_canceled_TEMPLATE,
						FormatUtils.getCityName(flight.getArrivalWaypoint(), getContext())));
			}
			else if (Flight.STATUS_DIVERTED.equals(flight.mStatusCode)) {
				topLine.setText(R.string.flight_diverted);
			}
			else if (Flight.STATUS_REDIRECTED.equals(flight.mStatusCode)) {
				topLine.setText(R.string.flight_redirected);
			}
			bottomLine.setText(FormatUtils.formatFlightNumber(flight, getContext()));
			bulb.setImageResource(R.drawable.ic_flight_status_cancelled);
		}
		else {
			Calendar departure = flight.mOrigin.getMostRelevantDateTime();
			Calendar arrival = flight.getArrivalWaypoint().getMostRelevantDateTime();
			Waypoint summaryWaypoint = null;
			int bottomLineTextId = -1;

			if (arrival.before(now)) {
				//flight complete
				String timeString = formatTime(arrival);
				int delay = getDelayForWaypoint(flight.getArrivalWaypoint());
				if (delay > 0) {
					topLine.setText(res.getString(R.string.flight_arrived_late_at_TEMPLATE, timeString));
					bulb.setImageResource(R.drawable.ic_flight_status_delayed);
				}
				else if (delay < 0) {
					topLine.setText(res.getString(R.string.flight_arrived_early_at_TEMPLATE, timeString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}
				else {
					topLine.setText(res.getString(R.string.flight_arrived_on_time_at_TEMPLATE, timeString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}

				summaryWaypoint = flight.getArrivalWaypoint();
				bottomLineTextId = R.string.at_airport_TEMPLATE;
			}
			else if (departure.before(now)) {
				//flight in progress, show arrival info
				int delay = getDelayForWaypoint(flight.getArrivalWaypoint());
				CharSequence timeSpanString = DateUtils.getRelativeTimeSpanString(arrival.getTimeInMillis(),
						now.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, 0);

				if (delay > 0) {
					topLine.setText(res.getString(R.string.flight_arrives_late_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_delayed);
				}
				else if (delay < 0) {
					topLine.setText(res.getString(R.string.flight_arrives_early_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}
				else {
					topLine.setText(res.getString(R.string.flight_arrives_on_time_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}

				summaryWaypoint = flight.getArrivalWaypoint();
				bottomLineTextId = R.string.at_airport_TEMPLATE;
			}
			else if (departure.getTimeInMillis() - now.getTimeInMillis() > DateUtils.DAY_IN_MILLIS) {
				//More than 24 hours away
				String dateStr = DateUtils.formatDateTime(getContext(), DateTimeUtils.getTimeInLocalTimeZone(departure)
						.getTime(), DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR);
				topLine.setText(res.getString(R.string.flight_departs_on_TEMPLATE, dateStr));
				bulb.setImageResource(R.drawable.ic_flight_status_on_time);

				summaryWaypoint = flight.mOrigin;
				bottomLineTextId = R.string.from_airport_TEMPLATE;
			}
			else {
				//Less than 24 hours in the future...
				int delay = getDelayForWaypoint(flight.mOrigin);
				CharSequence timeSpanString = DateUtils.getRelativeTimeSpanString(departure.getTimeInMillis(),
						now.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, 0);

				if (delay > 0) {
					topLine.setText(res.getString(R.string.flight_departs_late_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_delayed);
				}
				else if (delay < 0) {
					topLine.setText(res.getString(R.string.flight_departs_early_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}
				else {
					topLine.setText(res.getString(R.string.flight_departs_on_time_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}

				summaryWaypoint = flight.mOrigin;
				bottomLineTextId = R.string.from_airport_TEMPLATE;
			}

			if (summaryWaypoint != null) {
				boolean hasGate = summaryWaypoint.hasGate();
				boolean hasTerminal = summaryWaypoint.hasTerminal();
				if (hasGate && hasTerminal) {
					bottomLine.setText(Html.fromHtml(res.getString(bottomLineTextId,
							summaryWaypoint.mAirportCode,
							res.getString(R.string.generic_terminal_TEMPLATE, summaryWaypoint.getTerminal(),
									summaryWaypoint.getGate()))));
				}
				else if (hasTerminal) {
					bottomLine.setText(Html.fromHtml(res.getString(bottomLineTextId,
							summaryWaypoint.mAirportCode,
							res.getString(R.string.terminal_but_no_gate_TEMPLATE,
									summaryWaypoint.getTerminal()))));
				}
				else if (hasGate) {
					bottomLine.setText(Html.fromHtml(res.getString(bottomLineTextId,
							summaryWaypoint.mAirportCode,
							res.getString(R.string.gate_number_only_TEMPLATE, summaryWaypoint.getGate()))));
				}
				else {
					bottomLine.setVisibility(View.GONE);
				}
			}
			else {
				bottomLine.setVisibility(View.GONE);
			}
		}
		return view;
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected SummaryButton getSummaryLeftButton(final ItinCardDataFlight itinCardData) {
		return new SummaryButton(R.drawable.ic_direction, R.string.directions,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						//TODO: Investigate compatibility
						Airport airport = itinCardData.getFlightLeg().getFirstWaypoint().getAirport();
						Intent intent = getAirportDirectionsIntent(airport);
						getContext().startActivity(intent);
					}
				});
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected SummaryButton getSummaryRightButton(final ItinCardDataFlight itinCardData) {
		if (!CalendarAPIUtils.deviceSupportsCalendarAPI(getContext())) {
			return null;
		}
		else {
			return new SummaryButton(R.drawable.ic_add_event, R.string.add_event, new OnClickListener() {
				@SuppressLint("NewApi")
				@Override
				public void onClick(View v) {
					Resources res = getResources();
					Calendar startCal = itinCardData.getStartDate().getCalendar();
					Calendar endCal = itinCardData.getEndDate().getCalendar();
					Waypoint origin = itinCardData.getFlightLeg().getFirstWaypoint();
					Waypoint destination = itinCardData.getFlightLeg().getLastWaypoint();
					Intent intent = new Intent(Intent.ACTION_INSERT);
					intent.setData(Events.CONTENT_URI);
					intent.putExtra(Events.TITLE,
							res.getString(R.string.flight_to_TEMPLATE, StrUtils.getWaypointCityOrCode(destination)));
					intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.getTimeInMillis());
					intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis());
					intent.putExtra(Events.EVENT_LOCATION, res.getString(R.string.calendar_flight_location_TEMPLATE,
							origin.getAirport().mName, StrUtils.getWaypointCityOrCode(origin)));
					getContext().startActivity(intent);
				}
			});
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void attachFlightMap() {
		final ItinCardDataFlight cardData = getItinCardData();
		if (cardData != null && getContext() instanceof SherlockFragmentActivity) {
			FragmentManager fragManager = ((SherlockFragmentActivity) getContext()).getSupportFragmentManager();

			setHardwareAccelerationEnabled(false);
			ViewGroup mapContainer = Ui.findView(this, R.id.flight_map_container);
			mapContainer.setId(R.id.itin_flight_map_container_active);

			SupportMapFragment mapFrag = new SupportMapFragment() {
				@Override
				public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle data) {
					View view = super.onCreateView(inflater, root, data);
					GoogleMap map = getMap();
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
						flightMarkerManager.setFlights(cardData.getFlightLeg().getSegments());
						mapCameraManager.showFlight(cardData.getFlightLeg().getSegment(0),
								FlightItinCard.this.getWidth(),
								getResources().getDimensionPixelSize(R.dimen.itin_map_total_size), 40);

					}
					return view;
				}
			};

			FragmentTransaction transaction = fragManager.beginTransaction();
			if (!mapFrag.isAdded()) {
				transaction.add(R.id.itin_flight_map_container_active, mapFrag, FRAG_TAG_FLIGHT_MAP);
				transaction.commit();
			}
		}
	}

	@SuppressLint("NewApi")
	public void removeFlightMap() {
		setHardwareAccelerationEnabled(true);
		if (getContext() instanceof SherlockFragmentActivity) {
			FragmentManager fragManager = ((SherlockFragmentActivity) getContext()).getSupportFragmentManager();
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

	public static Intent getAirportDirectionsIntent(Airport airport) {
		String format = "geo:0,0?q=%f,%f (%s)";
		String uriStr = String.format(format, airport.getLatitude(), airport.getLongitude(),
				airport.mName);
		Uri airportUri = Uri.parse(uriStr);
		Intent intent = new Intent(Intent.ACTION_VIEW, airportUri);

		intent.setComponent(new ComponentName("com.google.android.apps.maps",
				"com.google.android.maps.MapsActivity"));
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private enum WaypointType {
		DEPARTURE, ARRIVAL, LAYOVER
	}

	private View getWayPointView(final Waypoint primaryWaypoint, Waypoint secondaryWaypoint, WaypointType type,
			LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.snippet_itin_waypoint_row, null);
		TextView firstRowText = Ui.findView(v, R.id.layover_terminal_gate_one);
		TextView secondRowText = Ui.findView(v, R.id.layover_terminal_gate_two);
		View terminalMapDirectionsBtn = Ui.findView(v, R.id.terminal_map_or_directions_btn);

		Resources res = getResources();

		ImageView waypointTypeIcon = Ui.findView(v, R.id.waypoint_type_image);
		switch (type) {
		case DEPARTURE:
			waypointTypeIcon.setImageResource(R.drawable.ic_departure_details);
			break;
		case LAYOVER:
			//TODO: We are waiting on the asset for layovers
			waypointTypeIcon.setImageResource(R.drawable.ic_layover_details);
			break;
		case ARRIVAL:
			waypointTypeIcon.setImageResource(R.drawable.ic_arrival_details);
			break;
		}

		boolean primaryWaypointExists = primaryWaypoint != null;
		boolean primaryWaypointHasGate = primaryWaypointExists && !TextUtils.isEmpty(primaryWaypoint.getGate());
		boolean primaryWaypointHasTerm = primaryWaypointExists && !TextUtils.isEmpty(primaryWaypoint.getTerminal());
		boolean primaryWaypointHasAll = primaryWaypointExists && primaryWaypointHasGate && primaryWaypointHasTerm;

		String airportName = primaryWaypoint.getAirport().mName;
		Ui.setText(v, R.id.layover_airport_name, airportName);
		if (type.equals(WaypointType.LAYOVER)) {

			secondRowText.setVisibility(View.VISIBLE);

			boolean secondaryWaypointExists = secondaryWaypoint != null;
			boolean secondaryWaypointHasGate = secondaryWaypointExists
					&& !TextUtils.isEmpty(secondaryWaypoint.getGate());
			boolean secondaryWaypointHasTerm = secondaryWaypointExists
					&& !TextUtils.isEmpty(secondaryWaypoint.getTerminal());
			boolean secondaryWaypointHasAll = secondaryWaypointExists && secondaryWaypointHasGate
					&& secondaryWaypointHasTerm;

			String primaryText = null;
			if (primaryWaypointHasAll) {
				primaryText = res.getString(R.string.arrival_terminal_TEMPLATE, primaryWaypoint.getTerminal(),
						primaryWaypoint.getGate());
			}
			else if (primaryWaypointHasTerm) {
				primaryText = res.getString(R.string.arrive_terminal_but_no_gate_TEMPLATE,
						primaryWaypoint.getTerminal());
			}
			else if (primaryWaypointHasGate) {
				primaryText = res.getString(R.string.arrive_gate_number_only_TEMPLATE, primaryWaypoint.getGate());
			}
			else {
				primaryText = res.getString(R.string.no_arrival_terminal_gate_information);
			}

			String secondaryText = null;
			if (secondaryWaypointHasAll) {
				secondaryText = res.getString(R.string.departure_terminal_TEMPLATE, secondaryWaypoint.getTerminal(),
						secondaryWaypoint.getGate());
			}
			else if (secondaryWaypointHasTerm) {
				secondaryText = res.getString(R.string.depart_terminal_but_no_gate_TEMPLATE,
						secondaryWaypoint.getTerminal());
			}
			else if (secondaryWaypointHasGate) {
				secondaryText = res.getString(R.string.depart_gate_number_only_TEMPLATE, secondaryWaypoint.getGate());
			}
			else {
				secondaryText = res.getString(R.string.no_departure_terminal_gate_information);
			}

			firstRowText.setText(primaryText);
			secondRowText.setText(secondaryText);

		}
		else {
			secondRowText.setVisibility(View.GONE);

			String primaryText = null;
			if (primaryWaypointHasAll) {
				primaryText = res.getString(R.string.generic_terminal_TEMPLATE, primaryWaypoint.getTerminal(),
						primaryWaypoint.getGate());
			}
			else if (primaryWaypointHasTerm) {
				primaryText = res.getString(R.string.terminal_but_no_gate_TEMPLATE, primaryWaypoint.getTerminal());
			}
			else if (primaryWaypointHasGate) {
				primaryText = res.getString(R.string.gate_number_only_TEMPLATE, primaryWaypoint.getGate());
			}
			else {
				primaryText = res.getString(R.string.no_terminal_gate_information);
			}

			firstRowText.setText(primaryText);
		}

		terminalMapDirectionsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Airport airport = primaryWaypoint.getAirport();
				if (primaryWaypoint.getAirport().hasAirportMaps()) {
					TerminalMapsOrDirectionsDialogFragment fragment = TerminalMapsOrDirectionsDialogFragment
							.newInstance(airport);
					FragmentManager fragManager = ((SherlockFragmentActivity) getContext()).getSupportFragmentManager();
					fragment.show(fragManager, FRAG_TAG_AIRPORT_ACTION_CHOOSER);
				}
				else {
					Intent intent = FlightItinCard.getAirportDirectionsIntent(airport);
					getContext().startActivity(intent);
				}
			}
		});

		return v;
	}

	private View getFlightView(Flight flight, Calendar minTime, Calendar maxTime, LayoutInflater inflater) {
		FlightLegSummarySection v = (FlightLegSummarySection) inflater.inflate(
				R.layout.section_flight_leg_summary_itin, null);
		v.bindFlight(flight, minTime, maxTime);

		Calendar now = Calendar.getInstance();
		TextView tv = Ui.findView(v, R.id.delay_text_view);
		Resources res = getResources();

		if (flight.isRedAlert()) {
			if (Flight.STATUS_DIVERTED.equals(flight.mStatusCode)) {
				if (flight.mDiverted != null) {
					tv.setText(res.getString(R.string.flight_diverted_TEMPLATE,
							flight.getArrivalWaypoint().mAirportCode));
				}
				else {
					tv.setText(R.string.flight_diverted);
				}
			}
			else if (Flight.STATUS_REDIRECTED.equals(flight.mStatusCode)) {
				tv.setText(R.string.flight_redirected);
			}
			else {
				tv.setText(R.string.flight_canceled);
			}
			tv.setTextColor(res.getColor(R.color.itin_flight_canceled_color));
			tv.setVisibility(View.VISIBLE);
		}
		else if (flight.mFlightHistoryId != -1) {
			// only make the delay view visible if we've got data from FS
			if (now.before(flight.mOrigin.getMostRelevantDateTime())) {
				int delay = getDelayForWaypoint(flight.mOrigin);
				if (delay > 0) {
					tv.setTextColor(res.getColor(R.color.itin_flight_delayed_color));
					tv.setText(res.getString(R.string.flight_departs_x_late_TEMPLATE,
							DateTimeUtils.formatDuration(res, delay)));
				}
				else if (delay < 0) {
					tv.setTextColor(res.getColor(R.color.itin_flight_on_time_color));
					tv.setText(res.getString(R.string.flight_departs_x_early_TEMPLATE,
							DateTimeUtils.formatDuration(res, delay)));
				}
				else {
					tv.setTextColor(res.getColor(R.color.itin_flight_on_time_color));
					tv.setText(R.string.flight_departs_on_time);
				}
			}
			else if (now.before(flight.getArrivalWaypoint().getMostRelevantDateTime())) {
				int delay = getDelayForWaypoint(flight.getArrivalWaypoint());
				if (delay > 0) {
					tv.setTextColor(res.getColor(R.color.itin_flight_delayed_color));
					tv.setText(res.getString(R.string.flight_arrives_x_late_TEMPLATE,
							DateTimeUtils.formatDuration(res, delay)));
				}
				else if (delay < 0) {
					tv.setTextColor(res.getColor(R.color.itin_flight_on_time_color));
					tv.setText(res.getString(R.string.flight_arrives_x_early_TEMPLATE,
							DateTimeUtils.formatDuration(res, delay)));
				}
				else {
					tv.setTextColor(res.getColor(R.color.itin_flight_on_time_color));
					tv.setText(R.string.flight_arrives_on_time);
				}
			}
			else {
				// flight has arrived
				int delay = getDelayForWaypoint(flight.getArrivalWaypoint());
				if (delay > 0) {
					tv.setTextColor(res.getColor(R.color.itin_flight_delayed_color));
					tv.setText(res.getString(R.string.flight_arrived_x_late_TEMPLATE,
							DateTimeUtils.formatDuration(res, delay)));
				}
				else if (delay < 0) {
					tv.setTextColor(res.getColor(R.color.itin_flight_on_time_color));
					tv.setText(res.getString(R.string.flight_arrived_x_early_TEMPLATE,
							DateTimeUtils.formatDuration(res, delay)));
				}
				else {
					tv.setTextColor(res.getColor(R.color.itin_flight_on_time_color));
					tv.setText(R.string.flight_arrived_on_time);
				}
			}
			tv.setVisibility(View.VISIBLE);
		}
		else if (now.after(flight.getArrivalWaypoint().getMostRelevantDateTime())) {
			// last chance: we don't have FS data, but it seems like this flight should have landed already
			tv.setTextColor(res.getColor(R.color.itin_flight_on_time_color));
			tv.setText(R.string.flight_arrived);
			tv.setVisibility(View.VISIBLE);
		}

		return v;
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(getContext());
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}

	@SuppressLint("NewApi")
	private void setHardwareAccelerationEnabled(boolean enabled) {
		if (AndroidUtils.getSdkVersion() >= 11) {
			int layerType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

			setLayerType(layerType, null);
			Ui.findView(this, R.id.card_layout).setLayerType(layerType, null);
			Ui.findView(this, R.id.itin_type_image_view).setLayerType(layerType, null);
		}
	}

	private int getDelayForWaypoint(Waypoint wp) {
		Delay delay = wp.getDelay();
		if (delay.mDelayType == Delay.DELAY_GATE_ACTUAL || delay.mDelayType == Delay.DELAY_GATE_ESTIMATED) {
			return delay.mDelay;
		}
		else {
			return 0;
		}
	}

	private String getTerminalGateString(Waypoint waypoint) {
		Resources res = getResources();
		if (TextUtils.isEmpty(waypoint.getGate()) && TextUtils.isEmpty(waypoint.getTerminal())) {
			//no gate or terminal info
			return res.getString(R.string.gate_number_only_TEMPLATE, res.getString(R.string.to_be_determined_abbrev));
		}
		else if (!TextUtils.isEmpty(waypoint.getGate())) {
			//gate only
			return res.getString(R.string.gate_number_only_TEMPLATE, waypoint.getGate());
		}
		else if (!TextUtils.isEmpty(waypoint.getTerminal())) {
			//terminal only
			return res.getString(R.string.terminal_but_no_gate_TEMPLATE, waypoint.getTerminal());
		}
		else {
			//We have gate and terminal info
			return res.getString(R.string.generic_terminal_TEMPLATE, waypoint.getTerminal(), waypoint.getGate());
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	public static class TerminalMapsOrDirectionsDialogFragment extends DialogFragment {

		private Airport mAirport;

		public static TerminalMapsOrDirectionsDialogFragment newInstance(Airport airport) {
			TerminalMapsOrDirectionsDialogFragment frag = new TerminalMapsOrDirectionsDialogFragment();
			frag.setAirport(airport);
			return frag;
		}

		public void setAirport(Airport airport) {
			mAirport = airport;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			final CharSequence directions = getString(R.string.directions);
			final CharSequence terminalMaps = getString(R.string.terminal_maps);
			final CharSequence[] options = { directions, terminalMaps };

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setItems(options, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (options[which].equals(directions)) {
						Intent intent = FlightItinCard.getAirportDirectionsIntent(mAirport);
						getActivity().startActivity(intent);
						TerminalMapsOrDirectionsDialogFragment.this.dismissAllowingStateLoss();
					}
					else if (options[which].equals(terminalMaps)) {
						Intent intent = new Intent(getActivity(), TerminalMapActivity.class);
						intent.putExtra(TerminalMapActivity.ARG_AIRPORT_CODE, mAirport.mAirportCode);
						getActivity().startActivity(intent);
						TerminalMapsOrDirectionsDialogFragment.this.dismissAllowingStateLoss();
					}
					else {
						TerminalMapsOrDirectionsDialogFragment.this.dismissAllowingStateLoss();
					}
				}
			});
			return builder.create();
		}
	}
}
