package com.expedia.bookings.widget.itin;

import java.util.Calendar;

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
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.graphics.DestinationBitmapDrawable;
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
		Calendar start = data.getStartDate().getCalendar();
		Calendar end = data.getEndDate().getCalendar();
		Calendar preDrop1 = (Calendar) end.clone();
		Calendar now = Calendar.getInstance(start.getTimeZone());

		preDrop1.add(Calendar.DAY_OF_YEAR, -1);

		// Pick up Jun 11
		if (now.before(start) && now.get(Calendar.DAY_OF_YEAR) != start.get(Calendar.DAY_OF_YEAR)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_pick_up_day_TEMPLATE,
					getItinCardData().getPickUpDate().formatTime(getContext(),
							DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR)));
		}
		else if (now.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR)) {
			// Pick up after 3PM
			if (now.before(end) && start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)) {
				view.setText(getContext().getString(
						R.string.itin_card_details_pick_up_TEMPLATE,
						getItinCardData().getPickUpDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
			}
			// Drop off before 5PM
			else {
				view.setText(getContext().getString(
						R.string.itin_card_details_drop_off_TEMPLATE,
						getItinCardData().getDropOffDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
			}
		}
		// Drop off Jun 15
		else if (now.before(preDrop1) && now.get(Calendar.DAY_OF_YEAR) != preDrop1.get(Calendar.DAY_OF_YEAR)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_drop_off_day_TEMPLATE,
					getItinCardData().getDropOffDate().formatTime(getContext(),
							DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR)));
		}
		// Drop off tomorrow before 5PM
		else if (now.get(Calendar.DAY_OF_YEAR) == preDrop1.get(Calendar.DAY_OF_YEAR)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_drop_off_tomorrow_TEMPLATE,
					getItinCardData().getDropOffDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
		}
		// Drop off before 5PM
		else if (now.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR)) {
			view.setText(getContext().getString(
					R.string.itin_card_details_drop_off_TEMPLATE,
					getItinCardData().getDropOffDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
		}
		// Dropped off Jun 15
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
				res.getQuantityText(R.plurals.number_of_days_label, itinCardData.getDays()));

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

}
