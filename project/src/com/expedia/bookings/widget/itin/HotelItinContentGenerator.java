package com.expedia.bookings.widget.itin;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LocationMapImageView;
import com.expedia.bookings.widget.InfoTripletView;
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
		String checkIn = itinCardData.getFormattedShortShareCheckInDate(getContext());
		String checkOut = itinCardData.getFormattedShortShareCheckOutDate(getContext());

		return String.format(template, "", checkIn, checkOut);
	}

	@Override
	public String getShareTextShort() {
		final ItinCardDataHotel itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_short_hotel);
		String hotelName = itinCardData.getPropertyName();
		String checkIn = itinCardData.getFormattedShortShareCheckInDate(getContext());
		String checkOut = itinCardData.getFormattedShortShareCheckOutDate(getContext());
		String detailsUrl = itinCardData.getPropertyInfoSiteUrl();

		return String.format(template, hotelName, checkIn, checkOut, detailsUrl);
	}

	@Override
	public String getShareTextLong() {
		final ItinCardDataHotel itinCardData = getItinCardData();

		Context ctx = getContext();

		String hotelName = itinCardData.getPropertyName();
		String lengthOfStay = itinCardData.getFormattedLengthOfStay(getContext());
		String checkIn = itinCardData.getFormattedLongShareCheckInDate(getContext());
		String checkOut = itinCardData.getFormattedLongShareCheckOutDate(getContext());
		String address = itinCardData.getAddressString();
		String phone = itinCardData.getRelevantPhone();
		String detailsUrl = itinCardData.getPropertyInfoSiteUrl();
		String downloadUrl = PointOfSale.getPointOfSale().getAppInfoUrl();

		StringBuilder builder = new StringBuilder();
		builder.append(ctx.getString(R.string.share_template_long_hotel_1_greeting, hotelName, lengthOfStay));
		builder.append("\n\n");

		if (checkIn != null || checkOut != null) {
			builder.append(ctx.getString(R.string.share_template_long_hotel_2_checkin_checkout, checkIn, checkOut));
			builder.append("\n\n");
		}

		if (address != null) {
			builder.append(ctx.getString(R.string.share_template_long_hotel_3_address, hotelName, address));
			builder.append("\n\n");
		}

		if (phone != null) {
			builder.append(ctx.getString(R.string.share_template_long_hotel_4_phone, phone));
			builder.append("\n\n");
		}

		if (detailsUrl != null) {
			builder.append(ctx.getString(R.string.share_template_long_hotel_5_more_info, detailsUrl));
			builder.append("\n\n");
		}

		builder.append(ctx.getString(R.string.share_template_long_ad, downloadUrl));

		return builder.toString();
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.bg_itin_placeholder;
	}

	@Override
	public List<String> getHeaderImageUrls() {
		return getItinCardData().getHeaderImageUrls();
	}

	@Override
	public String getHeaderText() {
		return getItinCardData().getPropertyName();
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_hotel);
	}

	@Override
	public View getTitleView(View convertView, ViewGroup container) {
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
	public View getSummaryView(View convertView, ViewGroup container) {
		TextView view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_summary_hotel, container,
				false);

		ItinCardDataHotel data = getItinCardData();
		Calendar startCal = data.getStartDate().getCalendar();
		Calendar now = Calendar.getInstance(startCal.getTimeZone());

		if (now.before(startCal) || startCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
			if (!TextUtils.isEmpty(data.getCheckInTime())) {
				view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_in_TEMPLATE,
						data.getCheckInTime()));
			}
			else {
				view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_in_TEMPLATE,
						data.getStartDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
			}
		}
		else {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_out_TEMPLATE,
					data.getEndDate().formatTime(getContext(), DateUtils.FORMAT_SHOW_TIME)));
		}

		return view;
	}

	public View getDetailsView(ViewGroup container) {
		final ItinCardDataHotel itinCardData = getItinCardData();

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_hotel, container, false);

		// Find
		InfoTripletView infoTriplet = Ui.findView(view, R.id.info_triplet);
		LocationMapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);
		TextView addressTextView = Ui.findView(view, R.id.address_text_view);
		TextView phoneNumberTextView = Ui.findView(view, R.id.phone_number_text_view);
		TextView roomTypeTextView = Ui.findView(view, R.id.room_type_text_view);
		ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);

		// Bind
		Resources res = getResources();
		infoTriplet.setValues(
				itinCardData.getFormattedDetailsCheckInDate(getContext()),
				itinCardData.getFormattedDetailsCheckOutDate(getContext()),
				itinCardData.getFormattedGuests());
		infoTriplet.setLabels(
				res.getString(R.string.itin_card_details_check_in),
				res.getString(R.string.itin_card_details_check_out),
				res.getQuantityText(R.plurals.number_of_guests_label, itinCardData.getGuestCount()));

		if (itinCardData.getPropertyLocation() != null) {
			staticMapImageView.setLocation(itinCardData.getPropertyLocation());
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
		final int iconResId = R.drawable.ic_phone;
		final String actionText = getContext().getString(R.string.itin_action_call_hotel);
		final String phone = getItinCardData().getRelevantPhone();

		// Action button OnClickListener
		final OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (phone != null) {
					SocialUtils.call(getContext(), phone);
					OmnitureTracking.trackItinHotelCall(getContext());
				}
			}
		};

		// Popup view and click listener
		final View popupContentView;
		final OnClickListener popupOnClickListener;

		if (!SocialUtils.canHandleIntentOfTypeXandUriY(getContext(), Intent.ACTION_VIEW, "tel:")) {
			popupContentView = getLayoutInflater().inflate(R.layout.popup_copy, null);
			Ui.setText(popupContentView, R.id.content_text_view, getContext().getString(R.string.copy_TEMPLATE, phone));

			popupOnClickListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardUtils.setText(getContext(), phone);
					Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
				}
			};
		}
		else {
			popupContentView = null;
			popupOnClickListener = null;
		}

		return new SummaryButton(iconResId, actionText, onClickListener, popupContentView, popupOnClickListener);
	}
}
