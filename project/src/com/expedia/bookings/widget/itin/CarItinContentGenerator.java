package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.EventSummaryView;
import com.expedia.bookings.widget.MapImageView;
import com.mobiata.android.SocialUtils;

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
		String pickUpDate = itinCardData.getFormattedShortPickUpDate();
		String dropOffDate = itinCardData.getFormattedShortDropOffDate();

		return String.format(template, pickUpDate, dropOffDate);
	}

	@Override
	public String getShareTextShort() {
		ItinCardDataCar itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_short_car);
		String carType = itinCardData.getCarTypeDescription(getContext());
		String pickUpDate = itinCardData.getFormattedShortPickUpDate();
		String dropOffDate = itinCardData.getFormattedShortDropOffDate();
		String vendorName = itinCardData.getVendorName();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toFormattedString();

		return String.format(template, carType, pickUpDate, dropOffDate, vendorName, vendorAddress);
	}

	@Override
	public String getShareTextLong() {
		ItinCardDataCar itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_long_car);
		String vendorName = itinCardData.getVendorName();
		String carType = itinCardData.getCarTypeDescription(getContext());
		String pickUpDate = itinCardData.getFormattedLongPickUpDate();
		String pickUpTime = itinCardData.getFormattedPickUpTime();
		String dropOffDate = itinCardData.getFormattedLongDropOffDate();
		String dropOffTime = itinCardData.getFormattedDropOffTime();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toLongFormattedString();
		String vendorPhone = itinCardData.getRelevantVendorPhone();
		String downloadUrl = PointOfSale.getPointOfSale().getAppInfoUrl();

		StringBuilder builder = new StringBuilder();

		builder.append(String.format(template, vendorName, carType, pickUpDate, pickUpTime, dropOffDate, dropOffTime,
				vendorAddress, vendorPhone));

		builder.append("\n\n");
		builder.append(getContext().getString(R.string.share_template_long_ad, downloadUrl));

		return builder.toString();
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.default_flights_background;
	}

	@Override
	public String getHeaderImageUrl() {
		return getItinCardData().getCarCategoryImageUrl();
	}

	@Override
	public String getHeaderText() {
		return getItinCardData().getCarTypeDescription(getContext());
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_car);
	}

	@Override
	public View getTitleView(ViewGroup container) {
		TextView view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_title_generic, container,
				false);
		view.setText(getHeaderText());
		return view;
	}

	@Override
	public View getSummaryView(ViewGroup container) {
		TextView view = (TextView) getLayoutInflater()
				.inflate(R.layout.include_itin_card_summary_car, container, false);
		view.setText("Pick up after " + getItinCardData().getFormattedPickUpTime());

		return view;
	}

	@Override
	public View getDetailsView(ViewGroup container) {
		final ItinCardDataCar itinCardData = getItinCardData();

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_car, container, false);

		// Find
		TextView pickUpDateTextView = Ui.findView(view, R.id.pick_up_date_text_view);
		TextView dropOffDateTextView = Ui.findView(view, R.id.drop_off_date_text_view);
		TextView daysTextView = Ui.findView(view, R.id.days_text_view);
		MapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);
		EventSummaryView pickUpEventSummaryView = Ui.findView(view, R.id.pick_up_event_summary_view);
		EventSummaryView dropOffEventSummaryView = Ui.findView(view, R.id.drop_off_event_summary_view);
		TextView vendorPhoneTextView = Ui.findView(view, R.id.vendor_phone_text_view);
		ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);

		// Bind
		pickUpDateTextView.setText(itinCardData.getFormattedShortPickUpDate());
		dropOffDateTextView.setText(itinCardData.getFormattedShortDropOffDate());
		daysTextView.setText(itinCardData.getFormattedDays());

		Location relevantLocation = itinCardData.getRelevantVendorLocation();
		if (relevantLocation != null) {
			staticMapImageView.setCenterPoint(relevantLocation);
			staticMapImageView.setPoiPoint(relevantLocation);
		}

		pickUpEventSummaryView.bind(itinCardData.getPickUpDate().getCalendar().getTime(),
				itinCardData.getPickUpLocation(), true);

		dropOffEventSummaryView.bind(itinCardData.getDropOffDate().getCalendar().getTime(),
				itinCardData.getDropOffLocation(), true);

		vendorPhoneTextView.setText(itinCardData.getRelevantVendorPhone());
		vendorPhoneTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getContext(), itinCardData.getRelevantVendorPhone());
			}
		});

		//Add shared data
		addSharedGuiElements(commonItinDataContainer);

		return view;
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
		return new SummaryButton(R.drawable.ic_phone, getItinCardData().getVendorName(), new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getContext(), getItinCardData().getRelevantVendorPhone());

				OmnitureTracking.trackItinCarCall(getContext());
			}
		});
	}

}
