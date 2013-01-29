package com.expedia.bookings.widget;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.utils.Ui;

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
	// OVERRIDES
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
	public void bind(TripComponent tripComponent) {
		mProperty = ((TripHotel) tripComponent).getProperty();
		super.bind(tripComponent);
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

		bind((TripHotel) tripHotel);

		return view;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void bind(TripHotel tripHotel) {
		mCheckInDateTextView.setText(DATE_FORMAT.format(tripHotel.getStartDate().getCalendar().getTime()));
		mCheckOutDateTextView.setText(DATE_FORMAT.format(tripHotel.getEndDate().getCalendar().getTime()));
		mGuestsTextView.setText("2");

		mStaticMapImageView.setCenterPoint(mProperty.getLocation());
		mStaticMapImageView.setPoiPoint(mProperty.getLocation());
	}
}