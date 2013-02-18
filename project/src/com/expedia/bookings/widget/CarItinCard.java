package com.expedia.bookings.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripComponent.Type;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getShareText(ItinCardDataCar itinCardData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataCar itinCardData) {
		return null;
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
		TextView detailsTextView = Ui.findView(view, R.id.details_text_view);
		TextView insuranceLabel = Ui.findView(view, R.id.insurance_label);
		ViewGroup insuranceContainer = Ui.findView(view, R.id.insurance_container);

		// Bind
		pickUpDateTextView.setText(itinCardData.getFormattedPickUpDate());
		dropOffDateTextView.setText(itinCardData.getFormattedDropOffDate());
		daysTextView.setText(itinCardData.getFormattedDays());

		Location relevantLocation = itinCardData.getRelevantLocation();
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

		detailsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.openSite(getContext(), itinCardData.getDetailsUrl());
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
		return new SummaryButton(R.drawable.ic_direction, R.string.directions, new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = itinCardData.getRelevantDirectionsIntent();
				if (intent != null) {
					getContext().startActivity(intent);
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
			}
		});
	}
}