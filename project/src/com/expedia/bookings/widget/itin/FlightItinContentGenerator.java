package com.expedia.bookings.widget.itin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.TerminalMapActivity;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.FlightConfirmation;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CalendarAPIUtils;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightMapImageView;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Delay;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class FlightItinContentGenerator extends ItinContentGenerator<ItinCardDataFlight> {

	private static final String FRAG_TAG_AIRPORT_ACTION_CHOOSER = "FRAG_TAG_AIRPORT_ACTION_CHOOSER";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public FlightItinContentGenerator(Context context, ItinCardDataFlight data) {
		super(context, data);
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
	public String getShareSubject() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getFlightShareSubject(getItinCardData().getFlightLeg());
	}

	@Override
	public String getShareTextShort() {
		final ItinCardDataFlight itinCardData = getItinCardData();

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

			Calendar departureCal = leg.getFirstWaypoint().getBestSearchDateTime();
			Calendar arrivalCal = leg.getLastWaypoint().getBestSearchDateTime();

			Date departureDate = DateTimeUtils.getTimeInLocalTimeZone(departureCal);
			Date arrivalDate = DateTimeUtils.getTimeInLocalTimeZone(arrivalCal);

			String departureTzString = FormatUtils.formatTimeZone(leg.getFirstWaypoint().getAirport(), departureDate);
			String arrivalTzString = FormatUtils.formatTimeZone(leg.getLastWaypoint().getAirport(), arrivalDate);

			//The story contains format strings, but we don't want to bone our international customers
			DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
			DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM);

			if (PointOfSale.getPointOfSale().getThreeLetterCountryCode().equalsIgnoreCase("USA")) {
				String dateFormatStr = "M/dd/yy";
				String timeFormatStr = "h:mma";

				((SimpleDateFormat) dateFormat).applyPattern(dateFormatStr);
				((SimpleDateFormat) timeFormat).applyPattern(timeFormatStr);
			}

			String departureDateStr = dateFormat.format(departureDate);
			String departureTimeStr = timeFormat.format(departureDate) + " " + departureTzString;
			String departureDateTimeStr = departureTimeStr + " " + departureDateStr;

			String arrivalDateStr = dateFormat.format(departureDate);
			String arrivalTimeStr = timeFormat.format(arrivalDate) + " " + arrivalTzString;
			String arrivalDateTimeStr = arrivalTimeStr + " " + arrivalDateStr;

			//single day
			if (leg.getDaySpan() == 0) {
				String template = getContext().getString(R.string.share_template_short_flight_sameday);

				return String.format(template, airlineAndFlightNumber, destinationCity, departureDateStr,
						originAirportCode,
						departureTimeStr, destinationAirportCode, arrivalTimeStr, destinationGateTerminal);
			}
			//multi day
			else {
				String template = getContext().getString(R.string.share_template_short_flight_multiday);

				return String.format(template, airlineAndFlightNumber, destinationCity, originAirportCode,
						departureDateTimeStr, destinationAirportCode, arrivalDateTimeStr, destinationGateTerminal);
			}
		}
		return null;
	}

	@Override
	public String getShareTextLong() {
		final ItinCardDataFlight itinCardData = getItinCardData();
		TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getFlightShareEmail(itinCardData.getFlightLeg(), tripFlight.getTravelers());
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.default_flights_background;
	}

	@Override
	public String getHeaderImageUrl() {
		final ItinCardDataFlight itinCardData = getItinCardData();

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
	public String getHeaderText() {
		final ItinCardDataFlight itinCardData = getItinCardData();

		if (itinCardData != null && itinCardData.getFlightLeg() != null
				&& itinCardData.getFlightLeg().getLastWaypoint() != null
				&& itinCardData.getFlightLeg().getLastWaypoint().getAirport() != null
				&& !TextUtils.isEmpty(itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity)) {
			return itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity;
		}

		return "Flight Card";
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_flight);
	}

	@Override
	public View getTitleView(ViewGroup container) {
		TextView view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_title_generic, container,
				false);
		view.setText(getHeaderText());
		return view;
	}

	@Override
	public View getDetailsView(ViewGroup container) {
		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_flight, container, false);

		ItinCardDataFlight data = getItinCardData();
		TripFlight tripFlight = (TripFlight) data.getTripComponent();

		if (tripFlight != null && tripFlight.getFlightTrip() != null && tripFlight.getFlightTrip().getLegCount() > 0) {
			Resources res = getResources();
			FlightLeg leg = data.getFlightLeg();

			FlightMapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);

			TextView departureTimeTv = Ui.findView(view, R.id.departure_time);
			TextView departureTimeTzTv = Ui.findView(view, R.id.departure_time_tz);
			TextView arrivalTimeTv = Ui.findView(view, R.id.arrival_time);
			TextView arrivalTimeTzTv = Ui.findView(view, R.id.arrival_time_tz);
			TextView passengerNameListTv = Ui.findView(view, R.id.passenger_name_list);

			ViewGroup flightLegContainer = Ui.findView(view, R.id.flight_leg_container);
			ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);

			//Map
			staticMapImageView.setFlights(data.getFlightLeg().getSegments());

			//Arrival / Departure times
			Calendar departureTimeCal = leg.getFirstWaypoint().getBestSearchDateTime();
			Calendar arrivalTimeCal = leg.getLastWaypoint().getBestSearchDateTime();

			String departureTime = formatTime(departureTimeCal);
			String departureTz = res.getString(R.string.depart_tz_TEMPLATE,
					FormatUtils.formatTimeZone(leg.getFirstWaypoint().getAirport(), departureTimeCal.getTime()));
			String arrivalTime = formatTime(arrivalTimeCal);
			String arrivalTz = res.getString(R.string.arrive_tz_TEMPLATE,
					FormatUtils.formatTimeZone(leg.getLastWaypoint().getAirport(), arrivalTimeCal.getTime()));

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

			//Add the flight stuff
			Flight prevSegment = null;
			int divPadding = getResources().getDimensionPixelSize(R.dimen.itin_flight_segment_divider_padding);
			for (int j = 0; j < leg.getSegmentCount(); j++) {
				Flight segment = leg.getSegment(j);
				boolean isFirstSegment = (j == 0);
				boolean isLastSegment = (j == leg.getSegmentCount() - 1);

				if (isFirstSegment) {
					flightLegContainer
							.addView(getWayPointView(segment.mOrigin, null, WaypointType.DEPARTURE));
					flightLegContainer.addView(getHorizontalDividerView(divPadding));
				}
				else {
					flightLegContainer.addView(getWayPointView(prevSegment.mDestination, segment.mOrigin,
							WaypointType.LAYOVER));
					flightLegContainer.addView(getHorizontalDividerView(divPadding));
				}

				flightLegContainer.addView(getFlightView(segment, departureTimeCal, arrivalTimeCal));
				flightLegContainer.addView(getHorizontalDividerView(divPadding));

				if (isLastSegment) {
					flightLegContainer.addView(getWayPointView(segment.mDestination, null, WaypointType.ARRIVAL));
				}

				prevSegment = segment;
			}

			//Add shared data
			addSharedGuiElements(commonItinDataContainer);
		}

		return view;
	}

	@Override
	public View getSummaryView(ViewGroup container) {
		final ItinCardDataFlight itinCardData = getItinCardData();

		if (itinCardData == null || itinCardData.getStartDate() == null || itinCardData.getEndDate() == null) {
			//Bad data (we don't show any summary view in this case)
			return null;
		}

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_summary_flight, container, false);
		TextView topLine = Ui.findView(view, R.id.flight_status_top_line);
		FontCache.setTypeface(topLine, FontCache.Font.ROBOTO_REGULAR);
		TextView bottomLine = Ui.findView(view, R.id.flight_status_bottom_line);
		ImageView bulb = Ui.findView(view, R.id.flight_status_bulb);
		ImageView glowBulb = Ui.findView(view, R.id.flight_status_bulb_glow);

		Resources res = getResources();
		Calendar now = Calendar.getInstance();
		Flight flight = itinCardData.getMostRelevantFlightSegment();
		Calendar departure = flight.mOrigin.getMostRelevantDateTime();

		if (flight.isRedAlert()) {
			boolean shouldPulseBulb = false;
			if (Flight.STATUS_CANCELLED.equals(flight.mStatusCode)) {
				topLine.setText(res.getString(R.string.flight_to_city_cancelled_TEMPLATE,
						FormatUtils.getCityName(flight.getArrivalWaypoint(), getContext())));
				if ((departure.getTimeInMillis() + (12 * DateUtils.HOUR_IN_MILLIS)) > now.getTimeInMillis()) {
					shouldPulseBulb = true;
				}
			}
			else if (Flight.STATUS_DIVERTED.equals(flight.mStatusCode)) {
				topLine.setText(R.string.flight_diverted);
			}
			else if (Flight.STATUS_REDIRECTED.equals(flight.mStatusCode)) {
				topLine.setText(R.string.flight_redirected);
				shouldPulseBulb = true;
			}
			bottomLine.setText(FormatUtils.formatFlightNumber(flight, getContext()));
			bulb.setImageResource(R.drawable.ic_flight_status_cancelled);

			if (shouldPulseBulb) {
				glowBulb.setImageResource(R.drawable.ic_flight_status_cancelled_glow);
				glowBulb.setVisibility(View.VISIBLE);
				glowBulb.startAnimation(getGlowAnimation());
			}
		}
		else {
			Calendar arrival = flight.getArrivalWaypoint().getMostRelevantDateTime();
			Waypoint summaryWaypoint = null;
			int bottomLineTextId = 0;
			int bottomLineFallbackId = 0;

			if (arrival.before(now)) {
				//flight complete
				if (flight.mFlightHistoryId == -1) {
					// no FS data
					topLine.setText(R.string.flight_arrived);
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}
				else {
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
				}

				summaryWaypoint = flight.getArrivalWaypoint();
				bottomLineTextId = R.string.at_airport_terminal_gate_TEMPLATE;
				bottomLineFallbackId = R.string.at_airport_TEMPLATE;
			}
			else if (departure.before(now) && (flight.mFlightHistoryId != -1)) {
				//flight in progress AND we have FS data, show arrival info
				int delay = getDelayForWaypoint(flight.getArrivalWaypoint());
				CharSequence timeSpanString = DateUtils.getRelativeTimeSpanString(arrival.getTimeInMillis(),
						now.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, 0);

				if (delay > 0) {
					topLine.setText(res.getString(R.string.flight_arrives_late_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_delayed);
					glowBulb.setImageResource(R.drawable.ic_flight_status_delayed_glow);
				}
				else if (delay < 0) {
					topLine.setText(res.getString(R.string.flight_arrives_early_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
					glowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}
				else {
					topLine.setText(res.getString(R.string.flight_arrives_on_time_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
					glowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}

				glowBulb.setVisibility(View.VISIBLE);
				glowBulb.startAnimation(getGlowAnimation());

				summaryWaypoint = flight.getArrivalWaypoint();
				bottomLineTextId = R.string.at_airport_terminal_gate_TEMPLATE;
				bottomLineFallbackId = R.string.at_airport_TEMPLATE;
			}
			else if ((departure.getTimeInMillis() - now.getTimeInMillis() > (3 * DateUtils.DAY_IN_MILLIS))
					|| (flight.mFlightHistoryId == -1)) {
				//More than 72 hours away or no FS data yet
				String dateStr = DateUtils.formatDateTime(getContext(), DateTimeUtils.getTimeInLocalTimeZone(departure)
						.getTime(), DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR);
				topLine.setText(res.getString(R.string.flight_departs_on_TEMPLATE, dateStr));
				bulb.setImageResource(R.drawable.ic_flight_status_on_time);
				bottomLine.setText(Html.fromHtml(res.getString(R.string.from_airport_time_TEMPLATE,
						flight.mOrigin.mAirportCode,
						formatTime(flight.mOrigin.getMostRelevantDateTime()))));
			}
			else {
				//Less than 72 hours in the future and has FS data
				int delay = getDelayForWaypoint(flight.mOrigin);
				CharSequence timeSpanString = DateUtils.getRelativeTimeSpanString(departure.getTimeInMillis(),
						now.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, 0);

				if (delay > 0) {
					topLine.setText(res.getString(R.string.flight_departs_late_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_delayed);
					glowBulb.setImageResource(R.drawable.ic_flight_status_delayed_glow);
				}
				else if (delay < 0) {
					topLine.setText(res.getString(R.string.flight_departs_early_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
					glowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}
				else {
					topLine.setText(res.getString(R.string.flight_departs_on_time_TEMPLATE, timeSpanString));
					bulb.setImageResource(R.drawable.ic_flight_status_on_time);
					glowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}

				glowBulb.setVisibility(View.VISIBLE);
				glowBulb.startAnimation(getGlowAnimation());

				summaryWaypoint = flight.mOrigin;
				bottomLineTextId = R.string.from_airport_terminal_gate_TEMPLATE;
				bottomLineFallbackId = R.string.from_airport_TEMPLATE;
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
					bottomLine
							.setText(Html.fromHtml(res.getString(bottomLineFallbackId, summaryWaypoint.mAirportCode)));
				}
			}
		}
		return view;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public SummaryButton getSummaryLeftButton() {
		return new SummaryButton(R.drawable.ic_direction, getContext().getString(R.string.directions),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Airport airport = getItinCardData().getFlightLeg().getFirstWaypoint().getAirport();
						Intent intent = getAirportDirectionsIntent(airport);
						getContext().startActivity(intent);
					}
				});
	}

	@SuppressLint("DefaultLocale")
	@Override
	public SummaryButton getSummaryRightButton() {
		final ItinCardDataFlight itinCardData = getItinCardData();

		final String firstConfCode = getFirstFlightConfirmationCodeString((TripFlight) itinCardData.getTripComponent());
		if (!TextUtils.isEmpty(firstConfCode)) {
			return new SummaryButton(R.drawable.ic_confirmation_checkmark_light, firstConfCode, new OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardUtils.setText(getContext(), firstConfCode);
					Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
					OmnitureTracking.trackItinFlightCopyPNR(getContext());
				}
			});
		}
		else {
			if (!CalendarAPIUtils.deviceSupportsCalendarAPI(getContext())) {
				return null;
			}
			else {
				return new SummaryButton(R.drawable.ic_clock_small, getContext().getString(R.string.set_alert),
						new OnClickListener() {
							//NOTE: CalendarAPIUtils.deviceSupportsCalendarAPI(getContext()) will block old phones, so NewApi is ok here
							@SuppressLint("NewApi")
							@Override
							public void onClick(View v) {
								Resources res = getResources();
								FlightLeg leg = itinCardData.getFlightLeg();
								Calendar startCal = itinCardData.getStartDate().getCalendar();
								Calendar endCal = itinCardData.getEndDate().getCalendar();
								Waypoint origin = itinCardData.getFlightLeg().getFirstWaypoint();
								Waypoint destination = itinCardData.getFlightLeg().getLastWaypoint();
								Intent intent = new Intent(Intent.ACTION_INSERT);
								intent.setData(Events.CONTENT_URI);
								intent.putExtra(
										Events.TITLE,
										res.getString(R.string.flight_calendar_event_title_TEMPLATE,
												StrUtils.getWaypointCityOrCode(origin),
												StrUtils.getWaypointCityOrCode(destination)));
								intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.getTimeInMillis());
								intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis());

								if (leg != null && leg.getSegmentCount() > 0) {
									// Don't lie to me!
									Flight firstFlight = leg.getSegment(0);
									boolean isIndividualFlight = true;
									if (isIndividualFlight && leg.getSegmentCount() != 1) {
										isIndividualFlight = false;
									}

									if (isIndividualFlight) {
										intent.putExtra(Events.EVENT_LOCATION,
												FormatUtils.formatFlightNumber(firstFlight, getContext()));
									}
									else {
										intent.putExtra(Events.EVENT_LOCATION, leg.getAirlinesFormatted());
									}

								}
								else {
									intent.putExtra(Events.EVENT_LOCATION,
											res.getString(R.string.calendar_flight_location_TEMPLATE,
													origin.getAirport().mName, StrUtils.getWaypointCityOrCode(origin)));
								}
								getContext().startActivity(intent);
							}
						});
			}
		}
	}

	@Override
	protected boolean addConfirmationNumber(ViewGroup container) {
		Log.d("ITIN: addConfirmationNumber");
		if (hasConfirmationNumber()) {
			TripFlight tripFlight = (TripFlight) getItinCardData().getTripComponent();
			List<FlightConfirmation> confs = tripFlight.getConfirmations();
			if (confs.size() <= 1) {
				return super.addConfirmationNumber(container);
			}
			else if (confs.size() > 1) {
				Resources res = getResources();
				for (FlightConfirmation conf : confs) {
					View view = getClickToCopyItinDetailItem(
							res.getString(R.string.flight_carrier_confirmation_code_label_TEMPLATE, conf.getCarrier()),
							conf.getConfirmationCode(), true);
					if (view != null) {
						container.addView(view);
					}
				}
				return true;
			}
		}
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

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

	private View getWayPointView(final Waypoint primaryWaypoint, Waypoint secondaryWaypoint, WaypointType type) {
		View v = getLayoutInflater().inflate(R.layout.snippet_itin_waypoint_row, null);
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
		boolean primaryWaypointHasGate = primaryWaypointExists && primaryWaypoint.hasGate();
		boolean primaryWaypointHasTerm = primaryWaypointExists && primaryWaypoint.hasTerminal();
		boolean primaryWaypointHasAll = primaryWaypointExists && primaryWaypointHasGate && primaryWaypointHasTerm;

		String airportName = primaryWaypoint.getAirport().mName;
		Ui.setText(v, R.id.layover_airport_name, airportName);
		if (type.equals(WaypointType.LAYOVER)) {

			secondRowText.setVisibility(View.VISIBLE);

			boolean secondaryWaypointExists = secondaryWaypoint != null;
			boolean secondaryWaypointHasGate = secondaryWaypointExists
					&& secondaryWaypoint.hasGate();
			boolean secondaryWaypointHasTerm = secondaryWaypointExists
					&& secondaryWaypoint.hasTerminal();
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

			if (primaryText != null) {
				firstRowText.setText(primaryText);
			}
			else {
				firstRowText.setVisibility(View.GONE);
			}

			if (secondaryText != null) {
				secondRowText.setText(secondaryText);
			}
			else {
				secondRowText.setVisibility(View.GONE);
			}

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

			if (primaryText != null) {
				firstRowText.setText(primaryText);
			}
			else {
				firstRowText.setVisibility(View.GONE);
			}
		}

		terminalMapDirectionsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Airport airport = primaryWaypoint.getAirport();

				TerminalMapsOrDirectionsDialogFragment fragment = TerminalMapsOrDirectionsDialogFragment
						.newInstance(airport);
				FragmentManager fragManager = ((SherlockFragmentActivity) getContext()).getSupportFragmentManager();
				fragment.show(fragManager, FRAG_TAG_AIRPORT_ACTION_CHOOSER);
			}
		});

		return v;
	}

	private View getFlightView(Flight flight, Calendar minTime, Calendar maxTime) {
		FlightLegSummarySection v = (FlightLegSummarySection) getLayoutInflater().inflate(
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
				tv.setText(R.string.flight_cancelled);
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

		//Seems silly but there is a bug in HONEYCOMB where if you set a view below a view that is invisible,
		//it will end up aligning to that views top.
		TextView multiDayTextView = Ui.findView(v, R.id.multi_day_text_view);
		if (multiDayTextView.getVisibility() != View.VISIBLE) {
			RelativeLayout.LayoutParams params = (LayoutParams) tv.getLayoutParams();
			params.addRule(RelativeLayout.BELOW, R.id.flight_trip_view);
			tv.setLayoutParams(params);
		}

		return v;
	}

	private String getFirstFlightConfirmationCodeString(TripFlight tripFlight) {
		if (tripFlight.getConfirmations() != null && tripFlight.getConfirmations().size() > 0) {
			List<FlightConfirmation> confirmations = tripFlight.getConfirmations();
			return confirmations.get(0).getConfirmationCode();
		}
		return "";
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(getContext());
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}

	private Animation getGlowAnimation() {
		Animation anim = new AlphaAnimation(0, 1);
		anim.setDuration(800);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		return anim;
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
		if (!waypoint.hasGate() && !waypoint.hasTerminal()) {
			//no gate or terminal info
			return res.getString(R.string.gate_number_only_TEMPLATE, res.getString(R.string.to_be_determined_abbrev));
		}
		else if (waypoint.hasGate()) {
			//gate only
			return res.getString(R.string.gate_number_only_TEMPLATE, waypoint.getGate());
		}
		else if (waypoint.hasTerminal()) {
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

			ArrayList<CharSequence> optionsList = new ArrayList<CharSequence>();
			optionsList.add(directions);
			if (mAirport.hasAirportMaps()) {
				optionsList.add(terminalMaps);
			}

			final CharSequence[] finalOptions = new CharSequence[optionsList.size()];
			optionsList.toArray(finalOptions);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setItems(finalOptions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (finalOptions[which].equals(directions)) {
						Intent intent = getAirportDirectionsIntent(mAirport);
						getActivity().startActivity(intent);
						TerminalMapsOrDirectionsDialogFragment.this.dismissAllowingStateLoss();

						OmnitureTracking.trackItinFlightDirections(getActivity());
					}
					else if (finalOptions[which].equals(terminalMaps)) {
						Intent intent = TerminalMapActivity.createIntent(getActivity(), mAirport.mAirportCode);
						getActivity().startActivity(intent);
						TerminalMapsOrDirectionsDialogFragment.this.dismissAllowingStateLoss();

						OmnitureTracking.trackItinFlightTerminalMaps(getActivity());
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
