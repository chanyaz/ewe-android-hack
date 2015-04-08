package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TerminalMapActivity;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.AirlineCheckInIntervals;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.FlightConfirmation;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.LeanPlumFlags;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightMapImageView;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Delay;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.AddFlightsIntentUtils;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class FlightItinContentGenerator extends ItinContentGenerator<ItinCardDataFlight> {

	private static final String FRAG_TAG_AIRPORT_ACTION_CHOOSER = "FRAG_TAG_AIRPORT_ACTION_CHOOSER";

	private static final int MAX_TIMEZONE_LENGTH = 6;

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
		if (isSharedItin()) {
			return R.drawable.ic_itin_shared_placeholder_flights;
		}
		else {
			return Ui.obtainThemeResID(getContext(), R.attr.skin_icTypeCircleFlight);
		}
	}

	@Override
	public Type getType() {
		return Type.FLIGHT;
	}

	@Override
	public String getShareSubject() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getShareSubject(getItinCardData());
	}

	@Override
	public String getShareTextShort() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getShareTextShort(getItinCardData());
	}

	@Override
	public String getShareTextLong() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getShareTextLong(getItinCardData());
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return Ui.obtainThemeResID(getContext(), R.attr.skin_itinFlightPlaceholderDrawable);
	}

	@Override
	public void getHeaderBitmapDrawable(int width, int height, HeaderBitmapDrawable target) {
		final String code = getItinCardData().getFlightLeg().getLastWaypoint().mAirportCode;

		final String url = new Akeakamai(Images.getFlightDestination(code)) //
			.resizeExactly(width, height) //
			.build();

		new PicassoHelper.Builder(getContext()).setPlaceholder(getHeaderImagePlaceholderResId()).setTarget(
			target.getCallBack()).build().load(url);
		setSharableImageURL(url);
	}

	@Override
	public String getHeaderText() {
		if (isSharedItin()) {
			ItinCardDataFlight itinCardData = getItinCardData();
			TripFlight flight = (TripFlight) itinCardData.getTripComponent();
			List<Traveler> travelers = flight.getTravelers();
			String name = travelers.get(0).getFirstName();
			if (TextUtils.isEmpty(name)) {
				name = getResources().getString(R.string.sharedItin_card_fallback_name_flight);
			}
			return getContext().getString(R.string.SharedItin_Title_Flight_TEMPLATE, name,
					itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity);
		}
		else {

			final ItinCardDataFlight itinCardData = getItinCardData();

			if (itinCardData != null && itinCardData.getFlightLeg() != null
					&& itinCardData.getFlightLeg().getLastWaypoint() != null
					&& itinCardData.getFlightLeg().getLastWaypoint().getAirport() != null
					&& !TextUtils.isEmpty(itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity)) {
				return itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity;
			}

		}

		return "Flight Card";
	}

	@Override
	public String getHeaderTextDate() {
		return super.getHeaderTextDate();
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_flight);
	}

	@Override
	public String getSharedItinName() {
		ItinCardDataFlight itinCardData = getItinCardData();
		TripFlight flight = (TripFlight) itinCardData.getTripComponent();
		List<Traveler> travelers = flight.getTravelers();
		return travelers.get(0).getFullName();
	}

	@Override
	public int getSharedItinIconBackground() {
		return 0xFF1F6699;
	}

	@Override
	public View getTitleView(View convertView, ViewGroup container) {
		TextView view = (TextView) convertView;
		if (view == null) {
			view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_title_generic, container, false);
		}
		ItinCardDataFlight itinCardData = getItinCardData();
		String headerText = itinCardData.getFlightLeg().getLastWaypoint().getAirport().mCity;
		view.setText(headerText);
		return view;
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
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
			DateTime departureTimeCal = leg.getFirstWaypoint().getBestSearchDateTime();
			DateTime arrivalTimeCal = leg.getLastWaypoint().getBestSearchDateTime();

			String departureTime = formatTime(departureTimeCal);
			String departureTz = res.getString(R.string.depart_tz_TEMPLATE,
					FormatUtils.formatTimeZone(leg.getFirstWaypoint().getAirport(), departureTimeCal,
							MAX_TIMEZONE_LENGTH));
			String arrivalTime = formatTime(arrivalTimeCal);
			String arrivalTz = res.getString(R.string.arrive_tz_TEMPLATE,
					FormatUtils.formatTimeZone(leg.getLastWaypoint().getAirport(), arrivalTimeCal,
							MAX_TIMEZONE_LENGTH));

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
					flightLegContainer.addView(getWayPointView(segment.getOriginWaypoint(), null, WaypointType.DEPARTURE, null));
					flightLegContainer.addView(getHorizontalDividerView(divPadding));
				}
				else {
					flightLegContainer.addView(getWayPointView(prevSegment.getDestinationWaypoint(), segment.getOriginWaypoint(),
							WaypointType.LAYOVER, null));
					flightLegContainer.addView(getHorizontalDividerView(divPadding));
				}

				flightLegContainer.addView(getFlightView(segment, departureTimeCal, arrivalTimeCal));
				flightLegContainer.addView(getHorizontalDividerView(divPadding));

				if (isLastSegment) {
					flightLegContainer.addView(getWayPointView(segment.getDestinationWaypoint(), null, WaypointType.ARRIVAL,
							segment.mBaggageClaim));
				}

				prevSegment = segment;
			}

			//Add shared data
			addSharedGuiElements(commonItinDataContainer);
		}

		return view;
	}

	private static class SummaryViewHolder {
		private TextView mTopLine;
		private TextView mBottomLine;
		private ImageView mBulb;
		private ImageView mGlowBulb;
	}

	@Override
	public View getSummaryView(View convertView, ViewGroup container) {
		final ItinCardDataFlight itinCardData = getItinCardData();

		if (itinCardData == null || itinCardData.getStartDate() == null || itinCardData.getEndDate() == null) {
			//Bad data (we don't show any summary view in this case)
			return null;
		}

		SummaryViewHolder vh;
		if (convertView == null) {
			convertView = getLayoutInflater().inflate(R.layout.include_itin_card_summary_flight, container, false);

			vh = new SummaryViewHolder();
			vh.mTopLine = Ui.findView(convertView, R.id.flight_status_top_line);
			vh.mBottomLine = Ui.findView(convertView, R.id.flight_status_bottom_line);
			vh.mBulb = Ui.findView(convertView, R.id.flight_status_bulb);
			vh.mGlowBulb = Ui.findView(convertView, R.id.flight_status_bulb_glow);

			// One-time setup
			FontCache.setTypeface(vh.mTopLine, FontCache.Font.ROBOTO_REGULAR);

			convertView.setTag(vh);
		}
		else {
			vh = (SummaryViewHolder) convertView.getTag();
		}

		Resources res = getResources();
		Flight flight = itinCardData.getMostRelevantFlightSegment();
		DateTime departure = new DateTime(flight.getOriginWaypoint().getMostRelevantDateTime());
		DateTime now = DateTime.now();

		if (flight.isRedAlert()) {
			boolean shouldPulseBulb = false;
			if (Flight.STATUS_CANCELLED.equals(flight.mStatusCode)) {
				vh.mTopLine.setText(res.getString(R.string.flight_to_city_cancelled_TEMPLATE,
						FormatUtils.getCityName(flight.getArrivalWaypoint(), getContext())));
				if (departure.plusHours(12).isAfter(now)) {
					shouldPulseBulb = true;
				}
			}
			else if (Flight.STATUS_DIVERTED.equals(flight.mStatusCode)) {
				vh.mTopLine.setText(R.string.flight_diverted);
			}
			else if (Flight.STATUS_REDIRECTED.equals(flight.mStatusCode)) {
				vh.mTopLine.setText(R.string.flight_redirected);
				shouldPulseBulb = true;
			}
			vh.mBottomLine.setText(FormatUtils.formatFlightNumber(flight, getContext()));
			vh.mBulb.setImageResource(R.drawable.ic_flight_status_cancelled);

			if (shouldPulseBulb) {
				vh.mGlowBulb.setImageResource(R.drawable.ic_flight_status_cancelled_glow);
				vh.mGlowBulb.setVisibility(View.VISIBLE);
				vh.mGlowBulb.startAnimation(getGlowAnimation());
			}
		}
		else {
			DateTime arrival = new DateTime(flight.getArrivalWaypoint().getMostRelevantDateTime());
			Waypoint summaryWaypoint = null;
			int bottomLineTextId = 0;
			int bottomLineFallbackId = 0;

			if (arrival.isBefore(now)) {
				//flight complete
				if (flight.mFlightHistoryId == -1) {
					// no FS data
					vh.mTopLine.setText(R.string.flight_arrived);
					vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
				}
				else {
					String timeString = JodaUtils.formatDateTime(getContext(), arrival, DateFormatUtils.FLAGS_TIME_FORMAT);
					int delay = getDelayForWaypoint(flight.getArrivalWaypoint());
					if (delay > 0) {
						vh.mTopLine.setText(res.getString(R.string.flight_arrived_late_at_TEMPLATE, timeString));
						vh.mBulb.setImageResource(R.drawable.ic_flight_status_delayed);
					}
					else if (delay < 0) {
						vh.mTopLine.setText(res.getString(R.string.flight_arrived_early_at_TEMPLATE, timeString));
						vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
					}
					else {
						vh.mTopLine.setText(res.getString(R.string.flight_arrived_on_time_at_TEMPLATE, timeString));
						vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
					}
				}

				summaryWaypoint = flight.getArrivalWaypoint();
				bottomLineTextId = R.string.at_airport_terminal_gate_TEMPLATE;
				bottomLineFallbackId = R.string.at_airport_TEMPLATE;
			}
			else if (departure.isBefore(now) && (flight.mFlightHistoryId != -1)) {
				//flight in progress AND we have FS data, show arrival info
				int delay = getDelayForWaypoint(flight.getArrivalWaypoint());
				CharSequence timeSpanString = getItinRelativeTimeSpan(getContext(), arrival, now);

				if (delay > 0) {
					vh.mTopLine.setText(res.getString(R.string.flight_arrives_late_TEMPLATE, timeSpanString));
					vh.mBulb.setImageResource(R.drawable.ic_flight_status_delayed);
					vh.mGlowBulb.setImageResource(R.drawable.ic_flight_status_delayed_glow);
				}
				else if (delay < 0) {
					vh.mTopLine.setText(res.getString(R.string.flight_arrives_early_TEMPLATE, timeSpanString));
					vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
					vh.mGlowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}
				else {
					vh.mTopLine.setText(res.getString(R.string.flight_arrives_on_time_TEMPLATE, timeSpanString));
					vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
					vh.mGlowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}

				vh.mGlowBulb.setVisibility(View.VISIBLE);
				vh.mGlowBulb.startAnimation(getGlowAnimation());

				summaryWaypoint = flight.getArrivalWaypoint();
				bottomLineTextId = R.string.at_airport_terminal_gate_TEMPLATE;
				bottomLineFallbackId = R.string.at_airport_TEMPLATE;
			}
			else if (JodaUtils.daysBetween(now, departure) > 3 || flight.mFlightHistoryId == -1) {
				//More than 72 hours away or no FS data yet
				String dateStr = JodaUtils.formatDateTime(getContext(), departure, DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_YEAR);
				vh.mTopLine.setText(res.getString(R.string.flight_departs_on_TEMPLATE, dateStr));
				vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
				vh.mBottomLine.setText(Html.fromHtml(res.getString(R.string.from_airport_time_TEMPLATE,
						flight.getOriginWaypoint().mAirportCode,
						formatTime(flight.getOriginWaypoint().getMostRelevantDateTime()))));
			}
			else {
				//Less than 72 hours in the future and has FS data
				int delay = getDelayForWaypoint(flight.getOriginWaypoint());
				CharSequence timeSpanString = getItinRelativeTimeSpan(getContext(), departure, now);

				if (delay > 0) {
					vh.mTopLine.setText(res.getString(R.string.flight_departs_late_TEMPLATE, timeSpanString));
					vh.mBulb.setImageResource(R.drawable.ic_flight_status_delayed);
					vh.mGlowBulb.setImageResource(R.drawable.ic_flight_status_delayed_glow);
				}
				else if (delay < 0) {
					vh.mTopLine.setText(res.getString(R.string.flight_departs_early_TEMPLATE, timeSpanString));
					vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
					vh.mGlowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}
				else {
					vh.mTopLine.setText(res.getString(R.string.flight_departs_on_time_TEMPLATE, timeSpanString));
					vh.mBulb.setImageResource(R.drawable.ic_flight_status_on_time);
					vh.mGlowBulb.setImageResource(R.drawable.ic_flight_status_on_time_glow);
				}

				vh.mGlowBulb.setVisibility(View.VISIBLE);
				vh.mGlowBulb.startAnimation(getGlowAnimation());

				summaryWaypoint = flight.getOriginWaypoint();
				bottomLineTextId = R.string.from_airport_terminal_gate_TEMPLATE;
				bottomLineFallbackId = R.string.from_airport_TEMPLATE;
			}

			if (summaryWaypoint != null) {
				String bottomLineHtml;
				if (!summaryWaypoint.hasGate() && !summaryWaypoint.hasTerminal()) {
					bottomLineHtml = res.getString(bottomLineFallbackId, summaryWaypoint.mAirportCode);
				}
				else {
					String terminalGate = FlightUtils.getTerminalGateString(getContext(), summaryWaypoint);
					bottomLineHtml = res.getString(bottomLineTextId, summaryWaypoint.mAirportCode, terminalGate);
				}
				vh.mBottomLine.setText(Html.fromHtml(bottomLineHtml));
			}
		}

		return convertView;
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
						NavUtils.startActivitySafe(getContext(), intent);
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
			return getSupportSummaryButton();
		}
	}

	// Add to calendar

	@Override
	public List<Intent> getAddToCalendarIntents() {
		Context context = getContext();
		PointOfSale pointOfSale = PointOfSale.getPointOfSale();
		ItinCardDataFlight itinCardData = getItinCardData();
		String itinNumber = itinCardData.getTripComponent().getParentTrip().getTripNumber();
		if (itinCardData.getTripComponent().getParentTrip().isShared()) {
			//We dont want to show the itin number of shared itins.
			itinNumber = null;
		}

		FlightLeg leg = itinCardData.getFlightLeg();

		List<Intent> intents = new ArrayList<Intent>();
		intents.add(AddToCalendarUtils.generateFlightAddToCalendarIntent(context, pointOfSale, itinNumber, leg));

		return intents;
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

	// Facebook

	@Override
	public String getFacebookShareName() {
		return getContext().getString(R.string.share_facebook_template_title_flight,
				getItinCardData().getFlightLeg().getLastWaypoint().getAirport().mCity);
	}

	//////////////////////////////////////////////////////////////////////////
	// Notifications
	//////////////////////////////////////////////////////////////////////////

	@Override
	public List<Notification> generateNotifications() {
		FlightLeg leg = getItinCardData().getFlightLeg();
		if (leg == null) {
			return null;
		}
		ArrayList<Notification> notifications = null;
		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled() && LeanPlumFlags.mShowShareFlightNotification) {
			notifications = new ArrayList<Notification>(2);
			notifications.add(generateCheckinNotification(leg));
			notifications.add(generateShareNotification(leg));
		}
		else {
			notifications = new ArrayList<Notification>(1);
			notifications.add(generateCheckinNotification(leg));
		}
		return notifications;
	}

	// https://mingle.karmalab.net/projects/eb_ad_app/cards/878
	// Given I have a flight, when it is 24 hours prior to the scheduled departure of that flight,
	// then I want to receive a notification that reads "You can now check in for your Virgin America
	// flight to Chicago."
	// If there is more than one segment for a given leg, the notification should just be sent
	// 24 hours prior to the scheduled departure of the first flight.
	//
	// https://mingle.karmalab.net/projects/eb_ad_app/cards/981
	// The amount of time shouldn't be exactly 24 hours, it should rely on AirlineCheckInIntervals.json
	private Notification generateCheckinNotification(FlightLeg leg) {
		Context context = getContext();
		ItinCardDataFlight data = getItinCardData();

		String itinId = data.getId();

		int checkInIntervalSeconds = AirlineCheckInIntervals.get(context, leg.getFirstAirlineCode());
		long expirationTimeMillis = data.getStartDate().getMillis();
		long triggerTimeMillis = expirationTimeMillis - checkInIntervalSeconds * DateUtils.SECOND_IN_MILLIS;

		Notification notification = new Notification(itinId + "_checkin", itinId, triggerTimeMillis);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setNotificationType(NotificationType.FLIGHT_CHECK_IN);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_SHARE);
		notification.setIconResId(R.drawable.ic_stat_flight);

		notification.setTicker(getContext().getString(R.string.Check_in_available));
		notification.setTitle(getContext().getString(R.string.Check_in_available));

		String airline = leg.getAirlinesFormatted();
		Waypoint lastWaypoint = leg.getLastWaypoint();
		String destination = StrUtils.getWaypointCityOrCode(lastWaypoint);
		String destinationCode = lastWaypoint.mAirportCode;

		String body;
		if (!TextUtils.isEmpty(airline)) {
			body = context.getString(R.string.x_flight_to_x_TEMPLATE, airline, destination);
		}
		else {
			body = context.getString(R.string.your_flight_to_x_TEMPLATE, destination);
		}

		notification.setBody(body);

		notification.setImageDestination(R.drawable.bg_itin_placeholder_flight, destinationCode);

		return notification;
	}

	// Given I have a flight, when it is 48 hours prior to the scheduled departure of that flight,
	// then I want to receive a notification that reads "Share your flight and allow others to get
	// live updates while you travel."
	private Notification generateShareNotification(FlightLeg leg) {
		Context context = getContext();
		ItinCardDataFlight data = getItinCardData();
		String itinId = data.getId();

		int checkInIntervalSeconds = AirlineCheckInIntervals.get(context, leg.getFirstAirlineCode());
		long triggerTimeMillis = data.getStartDate().getMillis() - DateUtils.HOUR_IN_MILLIS * 48;
		long expirationTimeMillis =
			data.getStartDate().getMillis() - checkInIntervalSeconds * DateUtils.SECOND_IN_MILLIS;

		Notification notification = new Notification(itinId + "_flightshare", itinId, triggerTimeMillis);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setNotificationType(NotificationType.FLIGHT_SHARE);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_VIEW | Notification.FLAG_SHARE);
		notification.setIconResId(R.drawable.ic_stat_flight);
		notification.setTicker(getContext().getString(R.string.Share_flight_itinerary_title));
		notification.setTitle(getContext().getString(R.string.Share_flight_itinerary_title));
		Waypoint lastWaypoint = leg.getLastWaypoint();
		String destinationCode = lastWaypoint.mAirportCode;
		String body = context.getString(R.string.Share_flight_itinerary_content);
		notification.setBody(body);
		notification.setImageDestination(R.drawable.bg_itin_placeholder_flight, destinationCode);

		return notification;
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

		return intent;
	}

	public Intent getShareWithFlightTrackIntent() {
		ItinCardDataFlight cardData = getItinCardData();
		FlightTrip flightTrip = ((TripFlight) cardData.getTripComponent()).getFlightTrip();
		List<Flight> flights = flightTrip.getLeg(cardData.getLegNumber()).getSegments();
		return AddFlightsIntentUtils.getIntent(flights);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private enum WaypointType {
		DEPARTURE, ARRIVAL, LAYOVER
	}

	private View getWayPointView(final Waypoint primaryWaypoint, Waypoint secondaryWaypoint, WaypointType type,
			String baggageClaim) {
		View v = getLayoutInflater().inflate(R.layout.snippet_itin_waypoint_row, null);
		TextView firstRowText = Ui.findView(v, R.id.layover_terminal_gate_one);
		TextView secondRowText = Ui.findView(v, R.id.layover_terminal_gate_two);
		TextView baggageClaimTextView = Ui.findView(v, R.id.baggage_claim_text_view);
		View terminalMapDirectionsBtn = Ui.findView(v, R.id.terminal_map_or_directions_btn);

		Resources res = getResources();

		ImageView waypointTypeIcon = Ui.findView(v, R.id.waypoint_type_image);
		switch (type) {
		case DEPARTURE:
			waypointTypeIcon.setImageResource(R.drawable.ic_departure_details);
			break;
		case LAYOVER:
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
				primaryText = primaryWaypoint.isInternationalTerminal()
						? res.getString(R.string.Arrive_International_Terminal_Gate_X_TEMPLATE,
								primaryWaypoint.getGate())
						: res.getString(R.string.Arrive_Terminal_X_Gate_Y_TEMPLATE,
								primaryWaypoint.getTerminal(),
								primaryWaypoint.getGate());
			}
			else if (primaryWaypointHasTerm) {
				primaryText = primaryWaypoint.isInternationalTerminal()
						? res.getString(R.string.Arrive_International_Terminal)
						: res.getString(R.string.Arrive_Terminal_X_TEMPLATE, primaryWaypoint.getTerminal());
			}
			else if (primaryWaypointHasGate) {
				primaryText = res.getString(R.string.Arrive_Gate_X_TEMPLATE, primaryWaypoint.getGate());
			}

			String secondaryText = null;
			if (secondaryWaypointHasAll) {
				secondaryText = secondaryWaypoint.isInternationalTerminal()
						? res.getString(R.string.Depart_International_Terminal_Gate_X_TEMPLATE,
								secondaryWaypoint.getGate())
						: res.getString(R.string.Depart_Terminal_X_Gate_Y_TEMPLATE,
								secondaryWaypoint.getTerminal(),
								secondaryWaypoint.getGate());
			}
			else if (secondaryWaypointHasTerm) {
				secondaryText = secondaryWaypoint.isInternationalTerminal()
						? res.getString(R.string.Depart_International_Terminal)
						: res.getString(R.string.Depart_Terminal_X_TEMPLATE, secondaryWaypoint.getTerminal());
			}
			else if (secondaryWaypointHasGate) {
				secondaryText = res.getString(R.string.Depart_Gate_X_TEMPLATE, secondaryWaypoint.getGate());
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
			if (!primaryWaypointHasTerm && !primaryWaypointHasGate) {
				firstRowText.setVisibility(View.GONE);
			}
			else {
				firstRowText.setText(FlightUtils.getTerminalGateString(getContext(), primaryWaypoint));
			}
		}

		baggageClaimTextView.setVisibility(!TextUtils.isEmpty(baggageClaim) ? View.VISIBLE : View.GONE);
		baggageClaimTextView.setText(getContext().getString(R.string.Baggage_Claim_X_TEMPLATE, baggageClaim));

		terminalMapDirectionsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Airport airport = primaryWaypoint.getAirport();

				TerminalMapsOrDirectionsDialogFragment fragment = TerminalMapsOrDirectionsDialogFragment
						.newInstance(airport);
				FragmentManager fragManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
				fragment.show(fragManager, FRAG_TAG_AIRPORT_ACTION_CHOOSER);
			}
		});

		return v;
	}

	private View getFlightView(Flight flight, DateTime minTime, DateTime maxTime) {
		FlightLegSummarySection v = (FlightLegSummarySection) getLayoutInflater().inflate(
				R.layout.section_flight_leg_summary_itin, null);
		v.bindFlight(flight, minTime, maxTime);

		Calendar now = Calendar.getInstance();
		TextView tv = Ui.findView(v, R.id.delay_text_view);
		Resources res = getResources();

		if (flight.isRedAlert()) {
			if (Flight.STATUS_DIVERTED.equals(flight.mStatusCode)) {
				if (flight.getDivertedWaypoint() != null) {
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
			if (now.before(flight.getOriginWaypoint().getMostRelevantDateTime())) {
				int delay = getDelayForWaypoint(flight.getOriginWaypoint());
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

	private String formatTime(DateTime cal) {
		String format = DateTimeUtils.getDeviceTimeFormat(getContext());
		return JodaUtils.format(DateTimeUtils.withConfiguredTimeZone(getContext(), cal), format);
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
						NavUtils.startActivitySafe(getActivity(), intent);
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
