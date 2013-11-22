package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.MutableDateTime;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.InfoTripletView;
import com.expedia.bookings.widget.LocationMapImageView;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.bitmaps.TwoLevelImageCache;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

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
		if (isSharedItin()) {
			return R.drawable.ic_itin_shared_placeholder_hotel;
		}
		else {
			return R.drawable.ic_type_circle_hotel;
		}
	}

	@Override
	public Type getType() {
		return Type.HOTEL;
	}

	@Override
	public String getShareSubject() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getShareSubject(getItinCardData());
	}

	@Override
	public String getShareTextShort() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getShareTextShort(getItinCardData());
	}

	@Override
	public String getShareTextLong() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		return shareUtils.getShareTextLong(getItinCardData());
	}

	@Override
	public String getHeaderTextDate() {
		return super.getHeaderTextDate();
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.bg_itin_placeholder;
	}

	@Override
	public UrlBitmapDrawable getHeaderBitmapDrawable(int width, int height) {
		List<String> urls = getItinCardData().getHeaderImageUrls();
		if (urls != null && urls.size() > 0) {
			setSharableImageURL(urls.get(0));
			return new UrlBitmapDrawable(getResources(), urls, getHeaderImagePlaceholderResId());
		}
		return null;
	}

	@Override
	public String getHeaderText() {
		if (isSharedItin()) {
			TripHotel hotel = (TripHotel) getItinCardData().getTripComponent();
			String name = hotel.getPrimaryTraveler().getFirstName();
			if (TextUtils.isEmpty(name)) {
				name = getResources().getString(R.string.sharedItin_card_fallback_name_hotel);
			}
			return getContext().getString(R.string.SharedItin_Title_Hotel_TEMPLATE,
					name, getItinCardData().getPropertyName());
		}
		else {
			return getItinCardData().getPropertyName();
		}
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_hotel);
	}

	@Override
	public String getSharedItinName() {
		TripHotel hotel = (TripHotel) getItinCardData().getTripComponent();
		return hotel.getPrimaryTraveler().getFullName();
	}

	@Override
	public int getSharedItinIconBackground() {
		return 0xFF3B5866;
	}

	@Override
	public View getTitleView(View convertView, ViewGroup container) {
		TitleViewHolder vh;
		if (convertView == null) {
			convertView = (ViewGroup) getLayoutInflater().inflate(R.layout.include_itin_card_title_hotel, container,
					false);

			vh = new TitleViewHolder();
			vh.mHotelNameTextView = Ui.findView(convertView, R.id.hotel_name_text_view);
			vh.mHotelRatingBar = Ui.findView(convertView, R.id.hotel_rating_bar);

			convertView.setTag(vh);
		}
		else {
			vh = (TitleViewHolder) convertView.getTag();
		}

		final ItinCardDataHotel itinCardData = getItinCardData();
		vh.mHotelNameTextView.setText(itinCardData.getPropertyName());
		vh.mHotelRatingBar.setRating(itinCardData.getPropertyRating());

		return convertView;
	}

	private static class TitleViewHolder {
		private TextView mHotelNameTextView;
		private RatingBar mHotelRatingBar;
	}

	@Override
	public View getSummaryView(View convertView, ViewGroup container) {
		TextView view = (TextView) convertView;
		if (view == null) {
			view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_summary_hotel, container, false);
		}

		ItinCardDataHotel data = getItinCardData();
		DateTime startDate = data.getStartDate();
		DateTime endDate = data.getEndDate();
		DateTime now = DateTime.now(startDate.getZone());

		final boolean beforeStart = now.isBefore(startDate);
		final long daysBetweenStart = JodaUtils.daysBetween(now, startDate);
		final long daysBetweenEnd = JodaUtils.daysBetween(now, endDate);

		// Check in - 3 days
		if (beforeStart && daysBetweenStart == 3) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_in_three_days));
		}
		// Check in - 2 days
		else if (beforeStart && daysBetweenStart == 2) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_in_two_days));
		}
		// Check in tomorrow
		else if (beforeStart && daysBetweenStart == 1) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_in_tomorrow));
		}
		// Check in after 3 PM
		else if (daysBetweenStart == 0) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_in_TEMPLATE,
					data.getFallbackCheckInTime(getContext())));
		}
		// Check in May 14
		else if (beforeStart) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_in_day_TEMPLATE,
					data.getFormattedDetailsCheckInDate(getContext())));
		}
		// Check out in 3 days
		else if (!beforeStart && daysBetweenEnd == 3) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_out_three_days));
		}
		// Check out in 2 days
		else if (!beforeStart && daysBetweenEnd == 2) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_out_two_days));
		}
		// Check out tomorrow
		else if (!beforeStart && daysBetweenEnd == 1) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_out_tomorrow));
		}
		// Check out before noon
		else if (daysBetweenEnd == 0) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_out_TEMPLATE,
					data.getFallbackCheckOutTime(getContext())));
		}
		// Check out May 18
		else if (now.isBefore(endDate)) {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_check_out_day_TEMPLATE,
					data.getFormattedDetailsCheckOutDate(getContext())));
		}
		// Checked out at May 18
		else {
			view.setText(getContext().getString(R.string.itin_card_hotel_summary_checked_out_day_TEMPLATE,
					data.getFormattedDetailsCheckOutDate(getContext())));
		}

		return view;
	}

	public View getDetailsView(View convertView, ViewGroup container) {
		final ItinCardDataHotel itinCardData = getItinCardData();

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_hotel, container, false);

		// Find
		InfoTripletView infoTriplet = Ui.findView(view, R.id.info_triplet);
		LocationMapImageView staticMapImageView = Ui.findView(view, R.id.mini_map);
		TextView addressTextView = Ui.findView(view, R.id.address_text_view);
		TextView localPhoneNumberHeaderTextView = Ui.findView(view, R.id.local_phone_number_header_text_view);
		TextView localPhoneNumberTextView = Ui.findView(view, R.id.local_phone_number_text_view);
		TextView tollFreePhoneNumberHeaderTextView = Ui.findView(view, R.id.toll_free_phone_number_header_text_view);
		TextView tollFreePhoneNumberTextView = Ui.findView(view, R.id.toll_free_phone_number_text_view);
		TextView roomTypeHeaderTextView = Ui.findView(view, R.id.room_type_header_text_view);
		TextView roomTypeTextView = Ui.findView(view, R.id.room_type_text_view);
		TextView bedTypeHeaderTextView = Ui.findView(view, R.id.bed_type_header_text_view);
		TextView bedTypeTextView = Ui.findView(view, R.id.bed_type_text_view);
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

		if (!TextUtils.isEmpty(itinCardData.getRoomType()) && !isSharedItin()) {
			roomTypeTextView.setText(itinCardData.getRoomType());
		}
		else {
			roomTypeHeaderTextView.setVisibility(View.GONE);
			roomTypeTextView.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(itinCardData.getBedType())) {
			bedTypeTextView.setText(itinCardData.getBedType());
		}
		else {
			bedTypeHeaderTextView.setVisibility(View.GONE);
			bedTypeTextView.setVisibility(View.GONE);
		}

		// Local phone
		boolean hasLocalPhone = !TextUtils.isEmpty(itinCardData.getLocalPhone());
		localPhoneNumberHeaderTextView.setVisibility(hasLocalPhone ? View.VISIBLE : View.GONE);
		localPhoneNumberTextView.setVisibility(hasLocalPhone ? View.VISIBLE : View.GONE);

		if (hasLocalPhone) {
			localPhoneNumberTextView.setText(itinCardData.getLocalPhone());
			localPhoneNumberTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getContext(), itinCardData.getLocalPhone());
				}
			});
		}

		// Toll free phone
		boolean hasTollFreePhone = !TextUtils.isEmpty(itinCardData.getTollFreePhone());
		tollFreePhoneNumberHeaderTextView.setVisibility(hasTollFreePhone ? View.VISIBLE : View.GONE);
		tollFreePhoneNumberTextView.setVisibility(hasTollFreePhone ? View.VISIBLE : View.GONE);

		if (hasTollFreePhone) {
			tollFreePhoneNumberTextView.setText(itinCardData.getTollFreePhone());
			tollFreePhoneNumberTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getContext(), itinCardData.getTollFreePhone());
				}
			});
		}

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
							NavUtils.startActivitySafe(getContext(), intent);
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

	// Add to calendar

	@Override
	public List<Intent> getAddToCalendarIntents() {
		Context context = getContext();

		ItinCardDataHotel itinCardDataHotel = getItinCardData();
		Property property = itinCardDataHotel.getProperty();
		LocalDate in = itinCardDataHotel.getStartDate().toLocalDate();
		LocalDate out = itinCardDataHotel.getEndDate().toLocalDate();
		String confNum = itinCardDataHotel.getFormattedConfirmationNumbers();
		String itinId = itinCardDataHotel.getTripComponent().getParentTrip().getTripNumber();
		if (itinCardDataHotel.getTripComponent().getParentTrip().isShared()) {
			//We dont want to show the itin number of shared itins.
			itinId = null;
		}

		List<Intent> intents = new ArrayList<Intent>();
		intents.add(AddToCalendarUtils.generateHotelAddToCalendarIntent(context, property, out, false, confNum, itinId));
		intents.add(AddToCalendarUtils.generateHotelAddToCalendarIntent(context, property, in, true, confNum, itinId));

		return intents;
	}

	// Facebook

	@Override
	public String getFacebookShareName() {
		return getContext().getString(R.string.share_facebook_template_title_hotel,
				getItinCardData().getPropertyName());
	}

	//////////////////////////////////////////////////////////////////////////
	// Notifications
	//////////////////////////////////////////////////////////////////////////

	@Override
	public List<Notification> generateNotifications() {
		ArrayList<Notification> notifications = new ArrayList<Notification>(2);
		notifications.add(generateCheckinNotification());
		notifications.add(generateCheckoutNotification());
		return notifications;
	}

	// https://mingle.karmalab.net/projects/eb_ad_app/cards/876
	// Given I have a hotel, when it is 10 AM on the check-in day, then I want to receive a notification
	// that reads "Check in at The Hyatt Regency Bellevue begins at 3PM today."
	// Hotel Check-in: Valid from 10:00AM-11:59PM on the day of check-in
	private Notification generateCheckinNotification() {
		ItinCardDataHotel data = getItinCardData();

		String itinId = data.getId();

		MutableDateTime trigger = data.getStartDate().toMutableDateTime();
		trigger.setZoneRetainFields(DateTimeZone.getDefault());
		trigger.setRounding(trigger.getChronology().minuteOfHour());
		trigger.setHourOfDay(10);
		long triggerTimeMillis = trigger.getMillis();

		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId + "_checkin", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.HOTEL_CHECK_IN);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_CALL);
		notification.setIconResId(R.drawable.ic_stat_hotel);

		String title = getContext().getString(R.string.itin_card_hotel_summary_check_in_TEMPLATE,
				data.getFallbackCheckInTime(getContext()));

		notification.setTicker(title);
		notification.setTitle(title);

		String body = data.getPropertyName();
		notification.setBody(body);

		notification.setImageUrls(data.getHeaderImageUrls());

		return notification;
	}

	// https://mingle.karmalab.net/projects/eb_ad_app/cards/877
	// Given I have a hotel, when it is 7 AM on the checkout day, then I want to receive a notification
	// that reads "Check out at The Hyatt Regency Bellevue is at 11AM today."
	// Hotel Check-out: Valid from 7:00AM-11:59PM on the day of check-out
	private Notification generateCheckoutNotification() {
		ItinCardDataHotel data = getItinCardData();

		String itinId = data.getId();

		MutableDateTime trigger = data.getEndDate().toMutableDateTime();
		trigger.setZoneRetainFields(DateTimeZone.getDefault());
		trigger.setRounding(trigger.getChronology().minuteOfHour());
		trigger.setHourOfDay(7);
		long triggerTimeMillis = trigger.getMillis();

		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId + "_checkout", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.HOTEL_CHECK_OUT);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_CALL);
		notification.setIconResId(R.drawable.ic_stat_hotel);

		String title = getContext().getString(R.string.itin_card_hotel_summary_check_out_TEMPLATE,
				data.getFallbackCheckOutTime(getContext()));

		notification.setTicker(title);
		notification.setTitle(title);

		String body = data.getPropertyName();
		notification.setBody(body);

		notification.setImageUrls(data.getHeaderImageUrls());

		return notification;
	}

	@Override
	public String getSharableImageURL() {
		/*
		 *  #2320. Let's get the previously set sharableImageURL and test to see if it actually exists.
		 *  Easiest way to do it is to ask TwoLevelImageCache, since it would already have been loaded if it did.
		 *  In the case it doesn't exist, then we will get the list of urls and then go thru each to find one.
		 */
		String sharableImgURL = super.getSharableImageURL();
		if (!TwoLevelImageCache.hasImageInDiskCache(sharableImgURL)) {
			List<String> urls = getItinCardData().getHeaderImageUrls();
			for (String url : urls) {
				if (TwoLevelImageCache.hasImageInDiskCache(url)) {
					sharableImgURL = url;
					break;
				}
			}
		}
		return sharableImgURL;
	}
}
