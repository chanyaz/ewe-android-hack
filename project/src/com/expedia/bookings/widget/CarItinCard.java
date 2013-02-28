package com.expedia.bookings.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;

public class CarItinCard extends ItinCard<ItinCardDataCar> {
	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public CarItinCard(Context context) {
		this(context, null);
	}

	public CarItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
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
	protected String getShareSubject(ItinCardDataCar itinCardData) {
		String template = getContext().getString(R.string.share_template_subject_car);
		String pickUpDate = itinCardData.getFormattedShortPickUpDate();
		String dropOffDate = itinCardData.getFormattedShortDropOffDate();

		return String.format(template, pickUpDate, dropOffDate);
	}

	@Override
	protected String getShareTextShort(ItinCardDataCar itinCardData) {
		String template = getContext().getString(R.string.share_template_short_car);
		String carType = itinCardData.getCarTypeDescription(getContext());
		String pickUpDate = itinCardData.getFormattedShortPickUpDate();
		String dropOffDate = itinCardData.getFormattedShortDropOffDate();
		String vendorName = itinCardData.getVendorName();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toFormattedString();

		return String.format(template, carType, pickUpDate, dropOffDate, vendorName, vendorAddress);
	}

	@Override
	protected String getShareTextLong(ItinCardDataCar itinCardData) {
		String template = getContext().getString(R.string.share_template_long_car);
		String vendorName = itinCardData.getVendorName();
		String carType = itinCardData.getCarTypeDescription(getContext());
		String pickUpDate = itinCardData.getFormattedLongPickUpDate();
		String pickUpTime = itinCardData.getFormattedPickUpTime();
		String dropOffDate = itinCardData.getFormattedLongDropOffDate();
		String dropOffTime = itinCardData.getFormattedDropOffTime();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toFormattedString();
		String vendorPhone = itinCardData.getRelevantVendorPhone();

		return String.format(template, vendorName, carType, pickUpDate, pickUpTime, dropOffDate, dropOffTime,
				vendorAddress, vendorPhone, "");
	}


	@Override
	protected int getHeaderImagePlaceholderResId() {
		return R.drawable.default_flights_background;
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataCar itinCardData) {
		return itinCardData.getCarCategoryImageUrl();
	}

	@Override
	protected String getHeaderText(ItinCardDataCar itinCardData) {
		return itinCardData.getCarTypeDescription(getContext());
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataCar itinCardData) {
		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_title_generic, container, false);
		view.setText(getHeaderText(itinCardData));
		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataCar itinCardData) {
		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_summary_car, container, false);
		view.setText("Pick up after " + itinCardData.getFormattedPickUpTime());

		return view;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, final ItinCardDataCar itinCardData) {
		View view = inflater.inflate(R.layout.include_itin_card_details_car, container, false);

		// Find
		TextView pickUpDateTextView = Ui.findView(view, R.id.pick_up_date_text_view);
		TextView dropOffDateTextView = Ui.findView(view, R.id.drop_off_date_text_view);
		TextView daysTextView = Ui.findView(view, R.id.days_text_view);
		MapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);
		EventSummaryView pickUpEventSummaryView = Ui.findView(view, R.id.pick_up_event_summary_view);
		EventSummaryView dropOffEventSummaryView = Ui.findView(view, R.id.drop_off_event_summary_view);
		TextView vendorPhoneTextView = Ui.findView(view, R.id.vendor_phone_text_view);
		TextView confirmationNumberTextView = Ui.findView(view, R.id.confirmation_number_text_view);
		TextView detailsTextView = Ui.findView(view, R.id.details_text_view);
		TextView insuranceLabel = Ui.findView(view, R.id.insurance_label);
		ViewGroup insuranceContainer = Ui.findView(view, R.id.insurance_container);

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

		final String confirmationNumber = itinCardData.getConfirmationNumber();
		confirmationNumberTextView.setText(confirmationNumber);
		confirmationNumberTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardUtils.setText(getContext(), confirmationNumber);
				Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
			}
		});

		vendorPhoneTextView.setText(itinCardData.getRelevantVendorPhone());
		vendorPhoneTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getContext(), itinCardData.getRelevantVendorPhone());
			}
		});

		detailsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
				builder.setUrl(itinCardData.getDetailsUrl());
				builder.setTitle(R.string.booking_info);
				builder.setTheme(R.style.FlightTheme);
				getContext().startActivity(builder.getIntent());

				OmnitureTracking.trackItinCarInfo(getContext());
			}
		});

		boolean hasInsurance = hasInsurance();
		int insuranceVisibility = hasInsurance ? View.VISIBLE : View.GONE;
		insuranceLabel.setVisibility(insuranceVisibility);
		insuranceContainer.setVisibility(insuranceVisibility);
		if (hasInsurance) {
			addInsuranceRows(inflater, insuranceContainer);
		}

		return view;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(final ItinCardDataCar itinCardData) {
		return new SummaryButton(R.drawable.ic_direction, R.string.itin_action_directions, new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = itinCardData.getRelevantDirectionsIntent();
				if (intent != null) {
					getContext().startActivity(intent);

					OmnitureTracking.trackItinCarDirections(getContext());
				}
			}
		});
	}

	@Override
	protected SummaryButton getSummaryRightButton(final ItinCardDataCar itinCardData) {
		return new SummaryButton(R.drawable.ic_phone, itinCardData.getVendorName(), new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getContext(), itinCardData.getRelevantVendorPhone());

				OmnitureTracking.trackItinCarCall(getContext());
			}
		});
	}
}
