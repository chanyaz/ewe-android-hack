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
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private TextView mCheckInDateTextView;
	private TextView mCheckOutDateTextView;
	private TextView mGuestsTextView;
	private MapImageView mStaticMapImageView;
	private TextView mAddressTextView;
	private TextView mPhoneNumberTextView;
	private TextView mRoomTypeTextView;
	private TextView mDetailsTextView;

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

	public View getDetailsView(LayoutInflater inflater, ViewGroup container, ItinCardDataHotel itinCardData) {
		View view = inflater.inflate(R.layout.include_itin_card_details_hotel, container, false);

		mCheckInDateTextView = Ui.findView(view, R.id.check_in_date_text_view);
		mCheckOutDateTextView = Ui.findView(view, R.id.check_out_date_text_view);
		mGuestsTextView = Ui.findView(view, R.id.guests_text_view);
		mStaticMapImageView = Ui.findView(view, R.id.mini_map);
		mAddressTextView = Ui.findView(view, R.id.address_text_view);
		mPhoneNumberTextView = Ui.findView(view, R.id.phone_number_text_view);
		mRoomTypeTextView = Ui.findView(view, R.id.room_type_text_view);
		mDetailsTextView = Ui.findView(view, R.id.details_text_view);

		bind(itinCardData);

		return view;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(final ItinCardDataHotel itinCardData) {
		return new SummaryButton(R.drawable.ic_urgency_clock, "DIRECTIONS", new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = itinCardData.getDirectionsIntent();

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

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void bind(final ItinCardDataHotel itinCardData) {
		mCheckInDateTextView.setText(itinCardData.getFormattedCheckInDate());
		mCheckOutDateTextView.setText(itinCardData.getFormattedCheckOutDate());
		mGuestsTextView.setText(itinCardData.getFormattedGuests());

		if (itinCardData.getPropertyLocation() != null) {
			mStaticMapImageView.setCenterPoint(itinCardData.getPropertyLocation());
			mStaticMapImageView.setPoiPoint(itinCardData.getPropertyLocation());
		}

		mAddressTextView.setText(itinCardData.getAddressString());
		mRoomTypeTextView.setText(itinCardData.getRoomDescription());

		mPhoneNumberTextView.setText(itinCardData.getRelevantPhone());
		mPhoneNumberTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getContext(), itinCardData.getRelevantPhone());
			}
		});

		mDetailsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.openSite(getContext(), itinCardData.getDetailsUrl());
			}
		});
	}
}