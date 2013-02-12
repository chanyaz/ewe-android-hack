package com.expedia.bookings.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;

public class HotelItinCard extends ItinCard<ItinCardDataHotel> {
	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public HotelItinCard(Context context) {
		this(context, null);
	}

	public HotelItinCard(Context context, AttributeSet attr) {
		super(context, attr);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// ABSTRACT IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_hotel;
	}

	@Override
	public Type getType() {
		return Type.HOTEL;
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataHotel itinCardData) {
		return itinCardData.getHeaderImageUrl();
	}

	@Override
	protected String getHeaderText(ItinCardDataHotel itinCardData) {
		return itinCardData.getHeaderText();
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataHotel itinCardData) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.include_itin_card_title_hotel, container, false);

		TextView hotelNameTextView = Ui.findView(view, R.id.hotel_name_text_view);
		RatingBar hotelRatingBar = Ui.findView(view, R.id.hotel_rating_bar);

		hotelNameTextView.setText(itinCardData.getPropertyName());
		hotelRatingBar.setRating(itinCardData.getHotelRating());

		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataHotel itinCardData) {
		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_summary_hotel, container, false);

		view.setText("Check-in after " + itinCardData.getCheckInTime());

		return view;
	}

	public View getDetailsView(LayoutInflater inflater, ViewGroup container, final ItinCardDataHotel itinCardData) {
		View view = inflater.inflate(R.layout.include_itin_card_details_hotel, container, false);

		// Find
		TextView checkInDateTextView = Ui.findView(view, R.id.check_in_date_text_view);
		TextView checkOutDateTextView = Ui.findView(view, R.id.check_out_date_text_view);
		TextView guestsTextView = Ui.findView(view, R.id.guests_text_view);
		MapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);
		TextView addressTextView = Ui.findView(view, R.id.address_text_view);
		TextView phoneNumberTextView = Ui.findView(view, R.id.phone_number_text_view);
		TextView roomTypeTextView = Ui.findView(view, R.id.room_type_text_view);
		TextView detailsTextView = Ui.findView(view, R.id.details_text_view);

		// Bind
		checkInDateTextView.setText(itinCardData.getFormattedCheckInDate());
		checkOutDateTextView.setText(itinCardData.getFormattedCheckOutDate());
		guestsTextView.setText(itinCardData.getFormattedGuests());

		if (itinCardData.getPropertyLocation() != null) {
			staticMapImageView.setCenterPoint(itinCardData.getPropertyLocation());
			staticMapImageView.setPoiPoint(itinCardData.getPropertyLocation());
		}

		addressTextView.setText(itinCardData.getAddressString());
		roomTypeTextView.setText(itinCardData.getRoomDescription());

		phoneNumberTextView.setText(itinCardData.getRelevantPhone());
		phoneNumberTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getContext(), itinCardData.getRelevantPhone());
			}
		});

		detailsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.openSite(getContext(), itinCardData.getDetailsUrl());
			}
		});

		return view;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(final ItinCardDataHotel itinCardData) {
		return new SummaryButton(R.drawable.ic_urgency_clock, "DIRECTIONS", new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = itinCardData.getDirectionsIntent();
				if (intent != null) {
					getContext().startActivity(intent);
				}
			}
		});
	}

	@Override
	protected SummaryButton getSummaryRightButton(final ItinCardDataHotel itinCardData) {
		return new SummaryButton(R.drawable.ic_urgency_clock, "CALL HOTEL", new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone = itinCardData.getRelevantPhone();
				if (phone != null) {
					SocialUtils.call(getContext(), phone);
				}
			}
		});
	}

	@Override
	protected void onShareButtonClick(ItinCardDataHotel itinCardData) {
	}
}