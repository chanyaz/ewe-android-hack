package com.expedia.bookings.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ClipboardUtils;
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
	protected String getShareSubject(ItinCardDataHotel itinCardData) {
		String template = getContext().getString(R.string.share_template_subject_hotel);
		String checkIn = itinCardData.getFormattedShortShareCheckInDate();
		String checkOut = itinCardData.getFormattedShortShareCheckOutDate();

		return String.format(template, "", checkIn, checkOut);
	}

	@Override
	protected String getShareTextShort(ItinCardDataHotel itinCardData) {
		String template = getContext().getString(R.string.share_template_short_hotel);
		String hotelName = itinCardData.getPropertyName();
		String checkIn = itinCardData.getFormattedShortShareCheckInDate();
		String checkOut = itinCardData.getFormattedShortShareCheckOutDate();
		String detailsUrl = itinCardData.getPropertyInfoSiteUrl();

		return String.format(template, hotelName, checkIn, checkOut, detailsUrl);
	}

	@Override
	protected String getShareTextLong(ItinCardDataHotel itinCardData) {
		String template = getContext().getString(R.string.share_template_long_hotel);
		String hotelName = itinCardData.getPropertyName();
		String lengthOfStay = itinCardData.getFormattedLengthOfStay(getContext());
		String checkIn = itinCardData.getFormattedLongShareCheckInDate();
		String checkOut = itinCardData.getFormattedLongShareCheckOutDate();
		String streetAddress = itinCardData.getAddressString();
		String phone = itinCardData.getRelevantPhone();
		String detailsUrl = itinCardData.getPropertyInfoSiteUrl();

		return String.format(template, hotelName, lengthOfStay, checkIn, checkOut, "", "", "", "", phone, detailsUrl,
				"");
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataHotel itinCardData) {
		return itinCardData.getHeaderImageUrl();
	}

	@Override
	protected String getHeaderText(ItinCardDataHotel itinCardData) {
		return itinCardData.getPropertyName();
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataHotel itinCardData) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.include_itin_card_title_hotel, container, false);

		TextView hotelNameTextView = Ui.findView(view, R.id.hotel_name_text_view);
		RatingBar hotelRatingBar = Ui.findView(view, R.id.hotel_rating_bar);

		hotelNameTextView.setText(itinCardData.getPropertyName());
		hotelRatingBar.setRating(itinCardData.getPropertyRating());

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
		TextView confirmationNumberTextView = Ui.findView(view, R.id.confirmation_number_text_view);
		TextView detailsTextView = Ui.findView(view, R.id.details_text_view);
		TextView insuranceLabel = Ui.findView(view, R.id.insurance_label);
		ViewGroup insuranceContainer = Ui.findView(view, R.id.insurance_container);

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

		final String confirmationNumbers = itinCardData.getFormattedConfirmationNumbers();
		confirmationNumberTextView.setText(confirmationNumbers);
		confirmationNumberTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardUtils.setText(getContext(), confirmationNumbers);
				Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
			}
		});

		detailsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
				builder.setUrl(itinCardData.getDetailsUrl());
				builder.setTitle(R.string.booking_info);
				builder.setTheme(R.style.FlightTheme);
				builder.setDisableSignIn(true);
				getContext().startActivity(builder.getIntent());
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
	protected SummaryButton getSummaryLeftButton(final ItinCardDataHotel itinCardData) {
		return new SummaryButton(R.drawable.ic_direction, R.string.itin_action_directions, new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = itinCardData.getDirectionsIntent();
				if (intent != null) {
					getContext().startActivity(intent);
					OmnitureTracking.trackItinHotelDirections(getContext());
				}
			}
		});
	}

	@Override
	protected SummaryButton getSummaryRightButton(final ItinCardDataHotel itinCardData) {
		return new SummaryButton(R.drawable.ic_phone, R.string.itin_action_call_hotel, new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone = itinCardData.getRelevantPhone();
				if (phone != null) {
					SocialUtils.call(getContext(), phone);
					OmnitureTracking.trackItinHotelCall(getContext());
				}
			}
		});
	}
}
