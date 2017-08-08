package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.DefaultMedia;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.cars.LatLong;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.EventSummaryView;
import com.expedia.bookings.widget.InfoTripletView;
import com.expedia.bookings.widget.LocationMapImageView;
import com.mobiata.android.SocialUtils;
import com.squareup.phrase.Phrase;

import java.util.ArrayList;
import java.util.List;

public class CarItinContentGenerator extends ItinContentGenerator<ItinCardDataCar> {
	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public CarItinContentGenerator(Context context, ItinCardDataCar data) {
		super(context, data);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// ABSTRACT IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getTypeIconResId() {
		return Ui.obtainThemeResID(getContext(), R.attr.itin_card_list_icon_car_drawable);
	}

	@Override
	public Type getType() {
		return Type.CAR;
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
		return Ui.obtainThemeResID(getContext(), R.attr.skin_itinCarPlaceholderDrawable);
	}

	@Override
	public List<? extends IMedia> getHeaderBitmapDrawable() {
		List<String> urls = new ArrayList<>();
		List<IMedia> mediaList =  new ArrayList<>();
		urls.add(Images.getCarRental(getItinCardData().getCar(), getResources().getDimension(R.dimen.car_image_width)));
		mediaList.add(new DefaultMedia(urls, "", getHeaderImagePlaceholderResId()));
		return mediaList;
	}

	@Override
	public String getHeaderText() {
		return getItinCardData().getVendorName();
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_car);
	}

	@Override
	public View getTitleView(View convertView, ViewGroup container) {
		TextView view = (TextView) convertView;
		if (view == null) {
			view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_title_generic, container, false);
		}

		view.setText(getItinCardData().getCarCategoryDescription(getContext()));
		return view;
	}

	@Override
	public View getSummaryView(View convertView, ViewGroup container) {
		TextView view = (TextView) convertView;
		if (view == null) {
			view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_summary_car, container, false);
		}

		ItinCardDataCar data = getItinCardData();
		DateTime pickUpDate = data.getPickUpDate();
		DateTime dropOffDate = data.getDropOffDate();
		DateTime now = DateTime.now(pickUpDate.getZone());

		final boolean beforeStart = now.isBefore(pickUpDate);
		final long daysBetweenStart = JodaUtils.daysBetween(now, pickUpDate);
		final long daysBetweenEnd = JodaUtils.daysBetween(now, dropOffDate);

		// Pick up in 3 days
		if (beforeStart && daysBetweenStart == 3) {
			view.setText(R.string.itin_card_details_pick_up_three_days);
		}
		// Pick up in 2 days
		else if (beforeStart && daysBetweenStart == 2) {
			view.setText(R.string.itin_card_details_pick_up_two_days);
		}
		// Pick up tomorrow
		else if (beforeStart && daysBetweenStart == 1) {
			view.setText(R.string.itin_card_details_pick_up_tomorrow);
		}
		// Pick up after 3PM
		else if (beforeStart && daysBetweenStart == 0) {
			view.setText(getContext().getString(
					R.string.itin_card_details_pick_up_TEMPLATE,
					JodaUtils.formatDateTime(getContext(), pickUpDate, DateUtils.FORMAT_SHOW_TIME)));
		}
		// Pick up May 14
		else if (beforeStart) {
			view.setText(getContext().getString(
					R.string.itin_card_details_pick_up_day_TEMPLATE,
					JodaUtils.formatDateTime(getContext(), pickUpDate, DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_NO_YEAR)));
		}
		// Drop off in 3 days
		else if (!beforeStart && daysBetweenEnd == 3) {
			view.setText(R.string.itin_card_details_drop_off_three_days);
		}
		// Drop off in 2 days
		else if (!beforeStart && daysBetweenEnd == 2) {
			view.setText(R.string.itin_card_details_drop_off_two_days);
		}
		// Drop off tomorrow
		else if (!beforeStart && daysBetweenEnd == 1) {
			view.setText(R.string.itin_card_details_drop_off_tomorrow);
		}
		// Drop off before 5PM
		else if (!beforeStart && daysBetweenEnd == 0) {
			view.setText(getContext().getString(
					R.string.itin_card_details_drop_off_TEMPLATE,
					JodaUtils.formatDateTime(getContext(), dropOffDate, DateUtils.FORMAT_SHOW_TIME)));
		}
		// Drop off May 18
		else if (now.isBefore(dropOffDate)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_drop_off_day_TEMPLATE,
					JodaUtils.formatDateTime(getContext(), dropOffDate, DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_NO_YEAR)));
		}
		// Dropped off May 18
		else {
			view.setText(getContext().getString(
					R.string.itin_card_details_dropped_off_TEMPLATE,
					JodaUtils.formatDateTime(getContext(), dropOffDate, DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_NO_YEAR)));
		}

		return view;
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
		final ItinCardDataCar itinCardData = getItinCardData();

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_car, container, false);

		// Find
		InfoTripletView infoTriplet = Ui.findView(view, R.id.info_triplet);
		View miniMapContainer = Ui.findView(view, R.id.mini_map_container);
		EventSummaryView pickUpEventSummaryView = Ui.findView(view, R.id.pick_up_event_summary_view);
		EventSummaryView dropOffEventSummaryView = Ui.findView(view, R.id.drop_off_event_summary_view);
		TextView localPhoneLabelTextView = Ui.findView(view, R.id.local_phone_label_text_view);
		TextView localPhoneTextView = Ui.findView(view, R.id.local_phone_text_view);
		TextView tollFreePhoneLabelTextView = Ui.findView(view, R.id.toll_free_phone_label_text_view);
		TextView tollFreePhoneTextView = Ui.findView(view, R.id.toll_free_phone_text_view);
		ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);

		// Bind
		Resources res = getResources();
		infoTriplet.setValues(
				itinCardData.getFormattedShortPickUpDate(getContext()),
				itinCardData.getFormattedShortDropOffDate(getContext()),
				itinCardData.getFormattedDays());
		infoTriplet.setLabels(
				res.getString(R.string.itin_card_details_pick_up),
				res.getString(R.string.itin_card_details_drop_off),
				res.getQuantityText(R.plurals.number_of_days_label, itinCardData.getInclusiveDays()));

		Location relevantLocation = itinCardData.getRelevantVendorLocation();
		if (relevantLocation != null && (relevantLocation.getLatitude() != 0 || relevantLocation.getLongitude() != 0)) {
			miniMapContainer.setVisibility(View.VISIBLE);

			LocationMapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);
			staticMapImageView.setLocation(new LatLong(relevantLocation.getLatitude(), relevantLocation.getLongitude()));
		}
		else {
			miniMapContainer.setVisibility(View.GONE);
		}

		pickUpEventSummaryView.bind(itinCardData.getPickUpDate(), itinCardData.getPickUpLocation(), true,
				itinCardData.getVendorName());
		dropOffEventSummaryView.bind(itinCardData.getDropOffDate(), itinCardData.getDropOffLocation(), true,
				itinCardData.getVendorName());

		showPhoneNumber(localPhoneLabelTextView, localPhoneTextView, itinCardData.getLocalPhoneNumber());
		showPhoneNumber(tollFreePhoneLabelTextView, tollFreePhoneTextView, itinCardData.getTollFreePhoneNumber());

		//Add shared data
		addSharedGuiElements(commonItinDataContainer);

		return view;
	}

	private void showPhoneNumber(TextView label, TextView display, final String phoneNumber) {
		boolean isEmpty = TextUtils.isEmpty(phoneNumber);
		label.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
		display.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

		if (!isEmpty) {
			display.setText(phoneNumber);
			display.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getContext(), phoneNumber);
				}
			});
		}
	}

	@Override
	public SummaryButton getSummaryLeftButton() {
		return new SummaryButton(Ui.obtainThemeResID(getContext(), R.attr.itin_card_summary_left_action_button_car_drawable), getContext().getString(R.string.itin_action_directions),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Intent intent = getItinCardData().getRelevantDirectionsIntent();
						if (intent != null) {
							NavUtils.startActivitySafe(getContext(), intent);

							OmnitureTracking.trackItinCarDirections();
						}
					}
				});
	}

	@Override
	public SummaryButton getSummaryRightButton() {
		final String vendorPhone = getItinCardData().getRelevantVendorPhone();
		if (!TextUtils.isEmpty(vendorPhone)) {
			String callAgencyContDesc = Phrase.from(getContext(), R.string.itin_action_call_agency_cont_desc_TEMPLATE)
				.put("agency", getItinCardData().getVendorName())
				.format().toString();
			return new SummaryButton(Ui.obtainThemeResID(getContext(), R.attr.itin_card_summary_right_action_button_car_drawable), getItinCardData().getVendorName(), callAgencyContDesc, new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getContext(), vendorPhone);

					OmnitureTracking.trackItinCarCall();
				}
			});
		}
		else {
			return getSupportSummaryButton();
		}
	}

	@Override
	public List<Intent> getAddToCalendarIntents() {
		return new ArrayList<Intent>();
	}

	// Facebook

	@Override
	public String getFacebookShareName() {
		return getContext().getString(R.string.share_facebook_template_title_car,
				getItinCardData().getVendorName());
	}

	//////////////////////////////////////////////////////////////////////////
	// Notifications
	//////////////////////////////////////////////////////////////////////////

	@Override
	public List<Notification> generateNotifications() {
		ArrayList<Notification> notifications = new ArrayList<Notification>(2);
		notifications.add(generatePickUpNotification());
		notifications.add(generateDropOffNotification());
		return notifications;
	}

	// https://mingle.karmalab.net/projects/eb_ad_app/cards/941
	// Given I have a car rental, when the pickup time starts, then I want to
	// receive a notification that reads (contentTitle) "Car Pick Up - Alamo"
	// (contentText) "You can now pick up your car."
	// Car Pick-up: Valid starting 2 hours prior to pick-up time, ending at 11:59PM
	// the day of pick-up or 2 hours prior to the drop-off time (if on the same day).
	private Notification generatePickUpNotification() {
		ItinCardDataCar data = getItinCardData();
		Car car = data.getCar();

		String itinId = data.getId();

		long triggerTimeMillis = data.getPickUpDate().getMillis();

		MutableDateTime expiration = data.getPickUpDate().toMutableDateTime();
		expiration.setZoneRetainFields(DateTimeZone.getDefault());
		expiration.setRounding(expiration.getChronology().minuteOfHour());
		expiration.setHourOfDay(23);
		expiration.setMinuteOfHour(59);
		long expirationTimeMillis = Math.min(expiration.getMillis(), calculateDropOffNotificationMillis());

		Notification notification = new Notification(itinId + "_pickup", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.CAR_PICK_UP);
		notification.setExpirationTimeMillis(expirationTimeMillis);

		final String carUrl = Images.getCarRental(car, getResources().getDimension(R.dimen.car_image_width));
		notification.setImageCar(carUrl);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_CALL);
		notification.setIconResId(R.drawable.ic_stat_car);

		String title = getContext().getString(R.string.Car_Pick_Up_X_TEMPLATE, data.getVendorName());
		notification.setTicker(title);
		notification.setTitle(title);

		String body = getContext().getString(R.string.You_can_now_pick_up_your_car);
		notification.setBody(body);

		return notification;
	}

	// https://mingle.karmalab.net/projects/eb_ad_app/cards/879
	// Given I have a car rental, when it is 2 hours prior to the return time,
	// then I want to receive a notification that reads "Your Enterprise rental
	// car is due to be returned in two hours."
	private Notification generateDropOffNotification() {
		ItinCardDataCar data = getItinCardData();
		Car car = data.getCar();

		String itinId = data.getId();

		long triggerTimeMillis = calculateDropOffNotificationMillis();

		MutableDateTime expiration = data.getDropOffDate().toMutableDateTime();
		expiration.setZoneRetainFields(DateTimeZone.getDefault());
		expiration.setRounding(expiration.getChronology().minuteOfHour());
		expiration.setHourOfDay(23);
		expiration.setMinuteOfHour(59);
		long expirationTimeMillis = expiration.getMillis();

		Notification notification = new Notification(itinId + "_dropoff", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.CAR_DROP_OFF);
		notification.setExpirationTimeMillis(expirationTimeMillis);

		final String carUrl = Images.getCarRental(car, getResources().getDimension(R.dimen.car_image_width));
		notification.setImageCar(carUrl);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_CALL);
		notification.setIconResId(R.drawable.ic_stat_car);

		String title = getContext().getString(R.string.Car_Drop_Off_X_TEMPLATE, data.getVendorName());
		notification.setTicker(title);
		notification.setTitle(title);

		String body = getContext().getString(R.string.Your_rental_is_due_returned);
		notification.setBody(body);

		return notification;
	}

	private long calculateDropOffNotificationMillis() {
		return getItinCardData().getDropOffDate().minusHours(2).getMillis();
	}

}
