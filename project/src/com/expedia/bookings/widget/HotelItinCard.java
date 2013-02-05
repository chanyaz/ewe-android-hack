package com.expedia.bookings.widget;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;

public class HotelItinCard extends ItinCard {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Property mProperty;

	private TextView mCheckInDateTextView;
	private TextView mCheckOutDateTextView;
	private TextView mGuestsTextView;
	private MapImageView mStaticMapImageView;
	private TextView mAddressTextView;
	private TextView mPhoneNumberTextView;
	private TextView mRoomTypeTextView;

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
	public void bind(ItinCardData itinCardData) {
		mProperty = ((TripHotel) itinCardData.getTripComponent()).getProperty();
		super.bind(itinCardData);
	}

	@Override
	protected String getHeaderImageUrl(TripComponent tripComponent) {
		if (mProperty != null && mProperty.getMediaCount() > 0) {
			return mProperty.getMedia(0).getUrl(Media.IMAGE_BIG_SUFFIX);
		}
		else if (mProperty != null) {
			return mProperty.getThumbnail().getUrl();
		}

		return "";
	}

	@Override
	protected String getHeaderText(TripComponent tripComponent) {
		return mProperty.getName();
	}

	public View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripHotel) {
		View view = inflater.inflate(R.layout.include_itin_card_hotel, container, false);

		mCheckInDateTextView = Ui.findView(view, R.id.check_in_date_text_view);
		mCheckOutDateTextView = Ui.findView(view, R.id.check_out_date_text_view);
		mGuestsTextView = Ui.findView(view, R.id.guests_text_view);
		mStaticMapImageView = Ui.findView(view, R.id.mini_map);
		mAddressTextView = Ui.findView(view, R.id.address_text_view);
		mPhoneNumberTextView = Ui.findView(view, R.id.phone_number_text_view);
		mRoomTypeTextView = Ui.findView(view, R.id.room_type_text_view);

		bind((TripHotel) tripHotel);

		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton() {
		return new SummaryButton(R.drawable.ic_urgency_clock, "DIRECTIONS", new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="
						+ mProperty.getLocation().getStreetAddressString()));

				intent.setComponent(new ComponentName("com.google.android.apps.maps",
						"com.google.android.maps.MapsActivity"));

				getContext().startActivity(intent);
			}
		});
	}

	@Override
	protected SummaryButton getSummaryRightButton() {
		return new SummaryButton(R.drawable.ic_urgency_clock, "CALL HOTEL", new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.call(getContext(), mProperty.getTollFreePhone());
			}
		});
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void bind(TripHotel tripHotel) {
		mCheckInDateTextView.setText(DATE_FORMAT.format(tripHotel.getStartDate().getCalendar().getTime()));
		mCheckOutDateTextView.setText(DATE_FORMAT.format(tripHotel.getEndDate().getCalendar().getTime()));
		mGuestsTextView.setText(String.valueOf(tripHotel.getGuests()));

		mStaticMapImageView.setCenterPoint(mProperty.getLocation());
		mStaticMapImageView.setPoiPoint(mProperty.getLocation());

		mAddressTextView.setText(mProperty.getLocation().getStreetAddressString());
		mRoomTypeTextView.setText(mProperty.getDescriptionText());

		if (!TextUtils.isEmpty(mProperty.getTollFreePhone())) {
			mPhoneNumberTextView.setText(mProperty.getTollFreePhone());
		}
		else {
			mPhoneNumberTextView.setText(mProperty.getLocalPhone());
		}
	}
}