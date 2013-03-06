package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.MapImageView;
import com.mobiata.android.SocialUtils;

public class HotelItinContentGenerator extends ItinContentGenerator<ItinCardDataHotel> {

	////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public HotelItinContentGenerator(Context context, ItinCardDataHotel data) {
		super(context, data);
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
	public String getShareSubject() {
		final ItinCardDataHotel itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_subject_hotel);
		String checkIn = itinCardData.getFormattedShortShareCheckInDate();
		String checkOut = itinCardData.getFormattedShortShareCheckOutDate();

		return String.format(template, "", checkIn, checkOut);
	}

	@Override
	public String getShareTextShort() {
		final ItinCardDataHotel itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_short_hotel);
		String hotelName = itinCardData.getPropertyName();
		String checkIn = itinCardData.getFormattedShortShareCheckInDate();
		String checkOut = itinCardData.getFormattedShortShareCheckOutDate();
		String detailsUrl = itinCardData.getPropertyInfoSiteUrl();

		return String.format(template, hotelName, checkIn, checkOut, detailsUrl);
	}

	@Override
	public String getShareTextLong() {
		final ItinCardDataHotel itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_long_hotel);
		String hotelName = itinCardData.getPropertyName();
		String lengthOfStay = itinCardData.getFormattedLengthOfStay(getContext());
		String checkIn = itinCardData.getFormattedLongShareCheckInDate();
		String checkOut = itinCardData.getFormattedLongShareCheckOutDate();
		String phone = itinCardData.getRelevantPhone();
		String detailsUrl = itinCardData.getPropertyInfoSiteUrl();

		return String.format(template, hotelName, lengthOfStay, checkIn, checkOut, "", "", "", "", phone, detailsUrl,
				"");
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.default_flights_background;
	}

	@Override
	public String getHeaderImageUrl() {
		return getItinCardData().getHeaderImageUrl();
	}

	@Override
	public String getHeaderText() {
		return getItinCardData().getPropertyName();
	}

	@Override
	public View getTitleView(ViewGroup container) {
		final ItinCardDataHotel itinCardData = getItinCardData();

		ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.include_itin_card_title_hotel, container,
				false);

		TextView hotelNameTextView = Ui.findView(view, R.id.hotel_name_text_view);
		RatingBar hotelRatingBar = Ui.findView(view, R.id.hotel_rating_bar);

		hotelNameTextView.setText(itinCardData.getPropertyName());
		hotelRatingBar.setRating(itinCardData.getPropertyRating());

		return view;
	}

	@Override
	public View getSummaryView(ViewGroup container) {
		TextView view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_summary_hotel, container,
				false);
		view.setText("Check-in after " + getItinCardData().getCheckInTime());

		return view;
	}

	public View getDetailsView(ViewGroup container) {
		final ItinCardDataHotel itinCardData = getItinCardData();

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_hotel, container, false);

		// Find
		TextView checkInDateTextView = Ui.findView(view, R.id.check_in_date_text_view);
		TextView checkOutDateTextView = Ui.findView(view, R.id.check_out_date_text_view);
		TextView guestsTextView = Ui.findView(view, R.id.guests_text_view);
		MapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);
		TextView addressTextView = Ui.findView(view, R.id.address_text_view);
		TextView phoneNumberTextView = Ui.findView(view, R.id.phone_number_text_view);
		TextView roomTypeTextView = Ui.findView(view, R.id.room_type_text_view);
		ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);

		// Bind
		checkInDateTextView.setText(itinCardData.getFormattedDetailsCheckInDate());
		checkOutDateTextView.setText(itinCardData.getFormattedDetailsCheckOutDate());
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
						final Intent intent = getItinCardData().getDirectionsIntent();
						if (intent != null) {
							getContext().startActivity(intent);
							OmnitureTracking.trackItinHotelDirections(getContext());
						}
					}
				});
	}

	@Override
	public SummaryButton getSummaryRightButton() {
		return new SummaryButton(R.drawable.ic_phone, getContext().getString(R.string.itin_action_call_hotel),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						String phone = getItinCardData().getRelevantPhone();
						if (phone != null) {
							SocialUtils.call(getContext(), phone);
							OmnitureTracking.trackItinHotelCall(getContext());
						}
					}
				});
	}

}
