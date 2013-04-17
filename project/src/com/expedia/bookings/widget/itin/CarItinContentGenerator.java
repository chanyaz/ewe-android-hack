package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.graphics.DestinationBitmapDrawable;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.ImageType;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.EventSummaryView;
import com.expedia.bookings.widget.InfoTripletView;
import com.expedia.bookings.widget.LocationMapImageView;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

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
		return R.drawable.ic_type_circle_car;
	}

	@Override
	public Type getType() {
		return Type.CAR;
	}

	@Override
	public String getShareSubject() {
		ItinCardDataCar itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_subject_car);
		String pickUpDate = itinCardData.getFormattedShortPickUpDate(getContext());
		String dropOffDate = itinCardData.getFormattedShortDropOffDate(getContext());

		return String.format(template, pickUpDate, dropOffDate);
	}

	@Override
	public String getShareTextShort() {
		ItinCardDataCar itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_short_car);
		String carCategory = itinCardData.getCarCategoryDescription(getContext());
		String pickUpDate = itinCardData.getFormattedShortPickUpDate(getContext());
		String dropOffDate = itinCardData.getFormattedShortDropOffDate(getContext());
		String vendorName = itinCardData.getVendorName();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toLongFormattedString();

		return String.format(template, carCategory, pickUpDate, dropOffDate, vendorName, vendorAddress);
	}

	@Override
	public String getShareTextLong() {
		ItinCardDataCar itinCardData = getItinCardData();

		Context context = getContext();
		StringBuilder sb = new StringBuilder();

		sb.append(context.getString(R.string.share_hi));
		sb.append("\n\n");

		sb.append(context.getString(R.string.share_car_start_TEMPLATE, itinCardData.getVendorName()));
		sb.append("\n\n");

		sb.append(context.getString(R.string.share_car_vehicle_TEMPLATE,
				itinCardData.getCarCategoryDescription(context)));
		sb.append("\n");

		String pickUpDate = itinCardData.getFormattedLongPickUpDate(getContext());
		String pickUpTime = itinCardData.getFormattedPickUpTime(getContext());
		sb.append(context.getString(R.string.share_car_pickup_TEMPLATE, pickUpDate, pickUpTime));
		sb.append("\n");

		String dropOffDate = itinCardData.getFormattedLongDropOffDate(getContext());
		String dropOffTime = itinCardData.getFormattedDropOffTime(getContext());
		sb.append(context.getString(R.string.share_car_dropoff_TEMPLATE, dropOffDate, dropOffTime));
		sb.append("\n\n");

		String localPhone = itinCardData.getLocalPhoneNumber();
		String vendorPhone = itinCardData.getTollFreePhoneNumber();

		Location pickupLoc = itinCardData.getPickUpLocation();
		Location dropoffLoc = itinCardData.getDropOffLocation();
		boolean hasDiffLocations = pickupLoc != null && !pickupLoc.equals(dropoffLoc);

		if (pickupLoc != null) {
			if (!hasDiffLocations) {
				sb.append(context.getString(R.string.share_car_location_section));
			}
			else {
				sb.append(context.getString(R.string.share_car_pickup_location_section));
			}

			sb.append("\n");
			sb.append(pickupLoc.toLongFormattedString());
			sb.append("\n");

			if (!TextUtils.isEmpty(localPhone)) {
				sb.append(localPhone);
				sb.append("\n");
			}

			if (!TextUtils.isEmpty(vendorPhone)) {
				sb.append(vendorPhone);
				sb.append("\n");
			}

			sb.append("\n");
		}

		if (hasDiffLocations && dropoffLoc != null) {
			sb.append(context.getString(R.string.share_car_dropoff_location_section));
			sb.append("\n");
			sb.append(dropoffLoc.toLongFormattedString());
			sb.append("\n");

			if (!TextUtils.isEmpty(vendorPhone)) {
				sb.append(vendorPhone);
				sb.append("\n");
			}

			sb.append("\n");
		}

		sb.append(getContext().getString(R.string.share_template_long_ad, PointOfSale.getPointOfSale().getAppInfoUrl()));

		return sb.toString();
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.default_flights_background;
	}

	@Override
	public UrlBitmapDrawable getHeaderBitmapDrawable(int width, int height) {
		Car car = getItinCardData().getCar();
		return new DestinationBitmapDrawable(getResources(), getHeaderImagePlaceholderResId(),
				car.getCategory(), car.getType(), width, height);
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
		Calendar start = data.getPickUpDate().getCalendar();
		Calendar end = data.getDropOffDate().getCalendar();
		Calendar preStart1 = (Calendar) start.clone();
		Calendar preStart2 = (Calendar) start.clone();
		Calendar preStart3 = (Calendar) start.clone();
		Calendar preEnd1 = (Calendar) end.clone();
		Calendar preEnd2 = (Calendar) end.clone();
		Calendar preEnd3 = (Calendar) end.clone();
		Calendar now = Calendar.getInstance(start.getTimeZone());

		preStart1.add(Calendar.DAY_OF_YEAR, -1);
		preStart2.add(Calendar.DAY_OF_YEAR, -2);
		preStart3.add(Calendar.DAY_OF_YEAR, -3);
		preEnd1.add(Calendar.DAY_OF_YEAR, -1);
		preEnd2.add(Calendar.DAY_OF_YEAR, -2);
		preEnd3.add(Calendar.DAY_OF_YEAR, -3);

		// Pick up in 3 days
		if (now.get(Calendar.DAY_OF_YEAR) == preStart3.get(Calendar.DAY_OF_YEAR)) {
			view.setText(R.string.itin_card_details_pick_up_three_days);
		}
		// Pick up in 2 days
		else if (now.get(Calendar.DAY_OF_YEAR) == preStart2.get(Calendar.DAY_OF_YEAR)) {
			view.setText(R.string.itin_card_details_pick_up_two_days);
		}
		// Pick up tomorrow
		else if (now.get(Calendar.DAY_OF_YEAR) == preStart1.get(Calendar.DAY_OF_YEAR)) {
			view.setText(R.string.itin_card_details_pick_up_tomorrow);
		}
		else if (now.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR)) {
			// Drop off before 5PM
			if (start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR) && now.after(start)) {
				view.setText(getContext().getString(
						R.string.itin_card_details_drop_off_TEMPLATE,
						getItinCardData().getDropOffDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
			}
			// Pick up after 3PM
			else {
				view.setText(getContext().getString(
						R.string.itin_card_details_pick_up_TEMPLATE,
						getItinCardData().getPickUpDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
			}
		}
		// Pick up May 14
		else if (now.before(start)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_pick_up_day_TEMPLATE,
					getItinCardData().getPickUpDate().formatTime(getContext(),
							DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR)));
		}
		// Drop off in 3 days
		else if (now.get(Calendar.DAY_OF_YEAR) == preEnd3.get(Calendar.DAY_OF_YEAR)) {
			view.setText(R.string.itin_card_details_drop_off_three_days);
		}
		// Drop off in 2 days
		else if (now.get(Calendar.DAY_OF_YEAR) == preEnd2.get(Calendar.DAY_OF_YEAR)) {
			view.setText(R.string.itin_card_details_drop_off_two_days);
		}
		// Drop off tomorrow
		else if (now.get(Calendar.DAY_OF_YEAR) == preEnd1.get(Calendar.DAY_OF_YEAR)) {
			view.setText(R.string.itin_card_details_drop_off_tomorrow);
		}
		// Drop off before 5PM
		else if (now.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_drop_off_TEMPLATE,
					getItinCardData().getDropOffDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
		}
		// Drop off May 18
		else if (now.before(end)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_drop_off_day_TEMPLATE,
					getItinCardData().getDropOffDate().formatTime(getContext(),
							DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR)));
		}
		// Dropped off May 18
		else {
			view.setText(getContext().getString(
					R.string.itin_card_details_dropped_off_TEMPLATE,
					getItinCardData().getDropOffDate().formatTime(getContext(),
							DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR)));
		}

		return view;
	}

	@Override
	public View getDetailsView(ViewGroup container) {
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
			staticMapImageView.setLocation(relevantLocation);
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
		return new SummaryButton(R.drawable.ic_direction, getContext().getString(R.string.itin_action_directions),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Intent intent = getItinCardData().getRelevantDirectionsIntent();
						if (intent != null) {
							getContext().startActivity(intent);

							OmnitureTracking.trackItinCarDirections(getContext());
						}
					}
				});
	}

	@Override
	public SummaryButton getSummaryRightButton() {
		final String vendorPhone = getItinCardData().getRelevantVendorPhone();
		if (!TextUtils.isEmpty(vendorPhone)) {
			return new SummaryButton(R.drawable.ic_phone, getItinCardData().getVendorName(), new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getContext(), vendorPhone);

					OmnitureTracking.trackItinCarCall(getContext());
				}
			});
		}
		else {
			return getSupportSummaryButton();
		}
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
	private Notification generatePickUpNotification() {
		ItinCardDataCar data = getItinCardData();

		String uniqueId = data.getId();

		long triggerTimeMillis = data.getPickUpDate().getMillisFromEpoch();

		Notification notification = new Notification(uniqueId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.CAR_PICK_UP);
		String carImageValue = ExpediaImageManager.getImageCode(data.getCar().getCategory(), data.getCar().getType());
		notification.setImage(ImageType.CAR, 0, carImageValue);
		notification.setFlags(Notification.FLAG_LOCAL);
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

		String uniqueId = data.getId();

		long triggerTimeMillis = data.getDropOffDate().getMillisFromEpoch();
		triggerTimeMillis -= 2 * DateUtils.HOUR_IN_MILLIS;

		Notification notification = new Notification(uniqueId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.CAR_DROP_OFF);
		String carImageValue = ExpediaImageManager.getImageCode(data.getCar().getCategory(), data.getCar().getType());
		notification.setImage(ImageType.CAR, 0, carImageValue);
		notification.setFlags(Notification.FLAG_LOCAL);
		notification.setIconResId(R.drawable.ic_stat_car);

		String title = getContext().getString(R.string.Car_Drop_Off_X_TEMPLATE, data.getVendorName());
		notification.setTicker(title);
		notification.setTitle(title);

		String body = getContext().getString(R.string.Your_rental_is_due_returned);
		notification.setBody(body);

		return notification;
	}

}
