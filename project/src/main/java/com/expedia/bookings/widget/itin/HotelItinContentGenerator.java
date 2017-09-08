package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.MutableDateTime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.bitmaps.FailedUrlCache;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.LatLong;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.itin.data.ItinCardDataHotel;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.GoogleMapsUtil;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.widget.InfoTripletView;
import com.expedia.bookings.widget.LocationMapImageView;
import com.mobiata.android.SocialUtils;
import com.squareup.phrase.Phrase;

import io.reactivex.Observer;

public class HotelItinContentGenerator extends ItinContentGenerator<ItinCardDataHotel> {

	@Inject
	public HotelServices hotelServices;
	private ItinCardDataHotel data;

	public HotelItinContentGenerator(Context context, ItinCardDataHotel data, MediaCallback callback) {
		super(context, data);
		this.setCallback(callback);
		this.data = data;

		if (data.getProperty().getMediaList().isEmpty()) {
			Ui.getApplication(getContext()).defaultHotelComponents();
			Ui.getApplication(getContext()).hotelComponent().inject(this);
			SuggestionV4 destination = new SuggestionV4();
			destination.gaiaId = data.getProperty().getPropertyId();
			HotelSearchParams params = (HotelSearchParams) new HotelSearchParams.Builder(28, 300, true)
				.startDate(data.getStartDate().toLocalDate())
				.endDate(data.getEndDate() != null ? data.getEndDate().toLocalDate()
					: data.getStartDate().plusDays(1).toLocalDate())
				.destination(destination)
				.build();
			hotelServices.info(params, data.getProperty().getPropertyId(), observer);
		}
	}

	private void setPlaceholderImage() {
		data.getProperty().setMediaList(new ArrayList<HotelMedia>());
		HotelMedia placeholder = new HotelMedia();
		placeholder.setIsPlaceholder(true);
		data.getProperty().addMedia(placeholder);
	}

	private Observer<HotelOffersResponse> observer = new Observer<HotelOffersResponse>() {

		@Override
		public void onComplete() {
		}

		@Override
		public void onError(Throwable e) {
			setPlaceholderImage();
			if (callback != null) {
				callback.onMediaReady(data.getProperty().getMediaList());
			}
		}

		@Override
		public void onNext(HotelOffersResponse hotelOffersResponse) {
			data.getProperty().setMediaList(Images.getHotelImages(hotelOffersResponse, getHeaderImagePlaceholderResId()));
			if (data.getProperty().getMediaList().isEmpty()) {
				setPlaceholderImage();
			}
			if (callback != null) {
				callback.onMediaReady(data.getProperty().getMediaList());
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////
	// ABSTRACT IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getTypeIconResId() {
		if (isSharedItin()) {
			return R.drawable.ic_itin_shared_placeholder_hotel;
		}
		else {
			return Ui.obtainThemeResID(getContext(), R.attr.itin_card_list_icon_hotel_drawable);
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
		return R.drawable.room_fallback;
	}

	@Override
	public List<? extends IMedia> getHeaderBitmapDrawable() {
		return data.getProperty().getMediaList();
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
			convertView = getLayoutInflater().inflate(R.layout.include_itin_card_title_hotel, container,
				false);

			vh = new TitleViewHolder();
			vh.mHotelNameTextView = Ui.findView(convertView, R.id.hotel_name_text_view);
			convertView.setTag(vh);
		}
		else {
			vh = (TitleViewHolder) convertView.getTag();
		}

		final ItinCardDataHotel itinCardData = getItinCardData();
		vh.mHotelNameTextView.setText(itinCardData.getPropertyName());
		return convertView;
	}

	private static class TitleViewHolder {
		private TextView mHotelNameTextView;
	}

	@Override
	public View getSummaryView(View convertView, ViewGroup container) {
		TextView view = (TextView) convertView;
		if (view == null) {
			view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_summary_hotel, container, false);
		}

		view.setText(getSummaryText());
		return view;
	}

	public String getSummaryText() {
		DateTime startDate = data.getStartDate();
		DateTime endDate = data.getEndDate();
		DateTime now = DateTime.now(startDate.getZone());

		final boolean beforeStart = now.isBefore(startDate);
		final long daysBetweenStart = JodaUtils.daysBetween(now, startDate);
		final long daysBetweenEnd = JodaUtils.daysBetween(now, endDate);

		String summaryText = "";
		// Check in - 3 days
		if (beforeStart && daysBetweenStart == 3) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_in_three_days));
		}
		// Check in - 2 days
		else if (beforeStart && daysBetweenStart == 2) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_in_two_days));
		}
		// Check in tomorrow
		else if (beforeStart && daysBetweenStart == 1) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_in_tomorrow));
		}
		// Check in after 3 PM
		else if (daysBetweenStart == 0) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_in_TEMPLATE,
				data.getFallbackCheckInTime(getContext())));
		}
		// Check in May 14
		else if (beforeStart) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_in_day_TEMPLATE,
				data.getFormattedDetailsCheckInDate(getContext())));
		}
		// Check out in 3 days
		else if (!beforeStart && daysBetweenEnd == 3) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_out_three_days));
		}
		// Check out in 2 days
		else if (!beforeStart && daysBetweenEnd == 2) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_out_two_days));
		}
		// Check out tomorrow
		else if (!beforeStart && daysBetweenEnd == 1) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_out_tomorrow));
		}
		// Check out before noon
		else if (daysBetweenEnd == 0) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_out_TEMPLATE,
				data.getFallbackCheckOutTime(getContext())));
		}
		// Check out May 18
		else if (now.isBefore(endDate)) {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_check_out_day_TEMPLATE,
				data.getFormattedDetailsCheckOutDate(getContext())));
		}
		// Checked out at May 18
		else {
			summaryText = (getContext().getString(R.string.itin_card_hotel_summary_checked_out_day_TEMPLATE,
				data.getFormattedDetailsCheckOutDate(getContext())));
		}

		return summaryText;
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
		TextView nonPricePromotionsHeaderTextView = Ui.findView(view, R.id.non_price_promotion_header_text_view);
		TextView nonPricePromotionsTextView = Ui.findView(view, R.id.non_price_promotion_text_view);
		TextView bedTypeHeaderTextView = Ui.findView(view, R.id.bed_type_header_text_view);
		TextView bedTypeTextView = Ui.findView(view, R.id.bed_type_text_view);
		ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);
		TextView hotelUpgradeText = Ui.findView(view, R.id.room_upgrade_button);

		// Bind
		Resources res = getResources();
		infoTriplet.setValues(
			itinCardData.getFormattedDetailsCheckInDate(getContext()),
			itinCardData.getFormattedDetailsCheckOutDate(getContext()),
			itinCardData.getFormattedGuests());
		infoTriplet.setLabels(
			res.getString(R.string.itin_card_details_check_in),
			res.getString(R.string.itin_card_details_check_out),
			res.getQuantityString(R.plurals.number_of_guests_label, itinCardData.getGuestCount()));

		if (itinCardData.getPropertyLocation() != null) {
			staticMapImageView.setLocation(new LatLong(itinCardData.getPropertyLocation().getLatitude(),
				itinCardData.getPropertyLocation().getLongitude()));

			staticMapImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = GoogleMapsUtil
						.getGoogleMapsIntent(getItinCardData().getProperty().getLocation(),
							getItinCardData().getProperty().getName());
					if (intent != null) {
						NavUtils.startActivitySafe(getContext(), intent);
					}
				}
			});
		}

		//Upgrade hotel booking
		boolean showRoomUpgradeButton = !isSharedItin();
		if (showRoomUpgradeButton) {
			//hasUpgradeAvailable set to true until API is ready
			boolean hasUpgradeAvailable = itinCardData.hasRoomUpgradeOffers();
			hotelUpgradeText.setVisibility(hasUpgradeAvailable ? View.VISIBLE : View.GONE);
			AccessibilityUtil.appendRoleContDesc(hotelUpgradeText, hotelUpgradeText.getText().toString(), R.string.accessibility_cont_desc_role_button);

			if (hasUpgradeAvailable) {
				//add null check if necessary!
				//roomUpgradeLink stubbed until API is ready
				final String roomUpgradeLink = itinCardData.getProperty().getRoomUpgradeWebViewUrl();
				hotelUpgradeText.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						WebViewActivity.IntentBuilder intentBuilder =
							buildWebViewIntent(R.string.trips_upgrade_hotel_button_label_cont_desc, roomUpgradeLink).setRoomUpgradeType();

						Intent intent = intentBuilder.getIntent();
						//Stubbed using cancel room data until API is ready
						intent.putExtra(Constants.ITIN_ROOM_UPGRADE_TRIP_ID, getItinCardData().getTripNumber());
						((Activity) getContext()).startActivityForResult(intent, Constants.ITIN_ROOM_UPGRADE_WEBPAGE_CODE);
						OmnitureTracking.trackHotelItinRoomUpgradeClick();
					}
				});
			}
		}

		addressTextView.setText(itinCardData.getAddressString());

		if (!TextUtils.isEmpty(itinCardData.getRoomType()) && !isSharedItin()) {
			roomTypeTextView.setText(itinCardData.getRoomType());
		}
		else {
			roomTypeHeaderTextView.setVisibility(View.GONE);
			roomTypeTextView.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(itinCardData.getNonPricePromotionText()) && !isSharedItin()) {
			nonPricePromotionsTextView.setText(itinCardData.getNonPricePromotionText());
		}
		else {
			nonPricePromotionsHeaderTextView.setVisibility(View.GONE);
			nonPricePromotionsTextView.setVisibility(View.GONE);
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
	protected boolean addBookingInfo(ViewGroup container) {
		boolean success = super.addBookingInfo(container);

		if (!success) {
			return false;
		}

		String roomCancelLink = getItinCardData().getProperty().getRoomCancelLink();
		boolean showCancelHotelRoomBtn = !getItinCardData().isPastCheckInDate() && Strings.isNotEmpty(roomCancelLink);
		if (showCancelHotelRoomBtn) {
			// Cancel booking button
			cancelHotelRoomButton(container);
		}

		if (!isSharedItin()) {
			//Setting hasEditRoomOption to true till API is ready
			boolean hasEditRoomOption = getItinCardData().getProperty().isBookingChangeAvailable();
			if (hasEditRoomOption) {
				setUpEditHotelRoomInfoButton(container);
			}
		}

		return true;
	}

	private void cancelHotelRoomButton(ViewGroup container) {
		TextView cancelHotelHotelRoomTv = Ui.findView(container, R.id.cancel_hotel_room);
		View cancelHotelLineDividerView = Ui.findView(container, R.id.cancel_hotel_divider);

		cancelHotelHotelRoomTv.setVisibility(View.VISIBLE);
		cancelHotelLineDividerView.setVisibility(View.VISIBLE);
		cancelHotelHotelRoomTv.setOnClickListener(new OnClickListener() {
			final String roomCancelLink = getItinCardData().getProperty().getRoomCancelLink();

			@Override
			public void onClick(View v) {
				startWebActivityForCancelHotelRoom(roomCancelLink);
			}
		});
	}

	private void startWebActivityForCancelHotelRoom(String roomCancelLink) {
		WebViewActivity.IntentBuilder intentBuilder =
			buildWebViewIntent(R.string.itin_card_details_cancel_hotel_room, roomCancelLink)
				.setRoomCancelType();
		Intent intent = intentBuilder.getIntent();
		intent.putExtra(Constants.ITIN_CANCEL_ROOM_BOOKING_TRIP_ID, getItinCardData().getTripNumber());
		((Activity) getContext()).startActivityForResult(intent, Constants.ITIN_CANCEL_ROOM_WEBPAGE_CODE);
		OmnitureTracking.trackHotelItinCancelRoomClick();
	}

	private void setUpEditHotelRoomInfoButton(ViewGroup container) {
		TextView editHotelRoomInfo = Ui.findView(container, R.id.edit_hotel_room_info);
		View editHotelRoomLineDividerView = Ui.findView(container, R.id.divider_edit_hotel_room_info);

		AccessibilityUtil.appendRoleContDesc(editHotelRoomInfo,
			getContext().getResources().getString(R.string.itin_card_change_hotel_options),
			R.string.accessibility_cont_desc_role_button);

		editHotelRoomInfo.setVisibility(View.VISIBLE);
		editHotelRoomLineDividerView.setVisibility(View.VISIBLE);

		editHotelRoomInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OmnitureTracking.trackItinEditRoomInfoWebViewOpen();
				String softChangeUrl = getItinCardData().getProperty().getBookingChangeWebUrl();
				Intent webViewIntent =
					buildWebViewIntent(R.string.trips_edit_room_info_web_view_title, softChangeUrl).setRoomSoftChange().getIntent();
				webViewIntent.putExtra(Constants.ITIN_SOFT_CHANGE_TRIP_ID, getItinCardData().getTripNumber());
				((Activity) getContext()).startActivityForResult(webViewIntent, Constants.ITIN_SOFT_CHANGE_WEBPAGE_CODE);
			}
		});
	}

	@Override
	public SummaryButton getSummaryLeftButton() {
		return new SummaryButton(Ui.obtainThemeResID(getContext(), R.attr.itin_card_summary_left_action_button_hotel_drawable), getContext().getString(R.string.itin_action_directions),
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = GoogleMapsUtil.getDirectionsIntent(getItinCardData().getProperty().getLocation().toLongFormattedString());
					if (intent != null) {
						NavUtils.startActivitySafe(getContext(), intent);
						OmnitureTracking.trackItinHotelDirections();
					}
				}
			});
	}

	@Override
	public SummaryButton getSummaryRightButton() {
		final int iconResId = Ui.obtainThemeResID(getContext(), R.attr.itin_card_summary_right_action_button_hotel_drawable);
		final String actionText = getContext().getString(R.string.itin_action_call_hotel);
		final String phone = getItinCardData().getRelevantPhone();

		// Action button OnClickListener
		final OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (phone != null) {
					SocialUtils.call(getContext(), phone);
					OmnitureTracking.trackItinHotelCall();
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
		intents
			.add(AddToCalendarUtils.generateHotelAddToCalendarIntent(context, property, out, false, confNum, itinId));
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

		Boolean bucketedForNewScheduledNotifications = AbacusFeatureConfigManager.isUserBucketedForTest(getContext(), AbacusUtils.TripsHotelScheduledNotificationsV2);
		if (bucketedForNewScheduledNotifications) {
			return generateNewNotifications();
		}
		else {
			return generateOldNotifications();
		}
	}

	private List<Notification> generateNewNotifications() {
		ArrayList<Notification> notifications = new ArrayList<Notification>();
		notifications.add(generateCheckinNewNotification());
		notifications.add(generateCheckoutNewNotification());
		if (getItinCardData().getGuestCount() > 2 || isDurationLongerThanDays(2)) {
			notifications.add(generateGetReadyNotification());
			notifications.add(generateActivityCrossSellNotification());
		}
		return notifications;
	}

	private List<Notification> generateOldNotifications() {
		ArrayList<Notification> notifications = new ArrayList<Notification>();
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
		notification.setNotificationType(Notification.NotificationType.HOTEL_CHECK_IN);
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

	private Notification generateCheckinNewNotification() {
		ItinCardDataHotel data = getItinCardData();

		String itinId = data.getId();

		DateTime startDate = roundTime(data.getStartDate());
		MutableDateTime trigger = startDate.toMutableDateTime();
		trigger.addDays(-1);
		long triggerTimeMillis = trigger.getMillis();

		trigger = startDate.toMutableDateTime();
		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId + "_checkin", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.HOTEL_CHECK_IN);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_CALL);
		notification.setIconResId(R.drawable.ic_stat_hotel);

		String title = getContext().getString(R.string.check_in_notification_title);

		notification.setTicker(title);
		notification.setTitle(title);

		String body = Phrase.from(getContext(), R.string.check_in_notification_body_TEMPLATE)
			.put("hotel", data.getPropertyName())
			.put("checkin", data.getFallbackCheckInTime(getContext()))
			.format().toString();
		notification.setBody(body);

		notification.setImageUrls(data.getHeaderImageUrls());

		return notification;
	}

	// https://mingle.karmalab.net/projects/eb_ad_app/cards/877
	// https://expedia.mingle.thoughtworks.com/projects/eb_ad_app/cards/3465
	// Given I have a hotel, when it is 10 AM on the checkout day, then I want to receive a notification
	// that reads "Check out at The Hyatt Regency Bellevue is at 11AM today."
	// Hotel Check-out: Valid from 10:00AM-11:59PM on the day of check-out
	private Notification generateCheckoutNotification() {
		ItinCardDataHotel data = getItinCardData();

		String itinId = data.getId();

		MutableDateTime trigger = data.getEndDate().toMutableDateTime();
		trigger.setZoneRetainFields(DateTimeZone.getDefault());
		trigger.setRounding(trigger.getChronology().minuteOfHour());
		trigger.setHourOfDay(10);
		long triggerTimeMillis = trigger.getMillis();

		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId + "_checkout", itinId, triggerTimeMillis);
		notification.setNotificationType(Notification.NotificationType.HOTEL_CHECK_OUT);
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

	@NonNull
	public DateTime roundTime(DateTime time) {
		MutableDateTime trigger = time.toMutableDateTime();
		trigger.setZoneRetainFields(DateTimeZone.getDefault());
		trigger.setRounding(trigger.getChronology().minuteOfHour());
		return trigger.toDateTime();
	}

	private Notification generateCheckoutNewNotification() {
		ItinCardDataHotel data = getItinCardData();

		String itinId = data.getId();

		DateTime endDate = roundTime(data.getEndDate());
		MutableDateTime trigger = endDate.toMutableDateTime();
		if (isDurationLongerThanDays(2)) {
			trigger.addDays(-1);
		}
		else {
			trigger.addHours(-12);
		}
		long triggerTimeMillis = trigger.getMillis();

		trigger = endDate.toMutableDateTime();
		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId + "_checkout", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.HOTEL_CHECK_OUT);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_CALL);
		notification.setIconResId(R.drawable.ic_stat_hotel);
		String title;
		String body;
		if (hasLastDayStarted()) {
			title = Phrase.from(getContext(), R.string.check_out_notification_title_day_of_TEMPLATE)
				.put("checkout", data.getFallbackCheckOutTime(getContext()))
				.format().toString();
			body = Phrase.from(getContext(), R.string.check_out_notification_body_day_of_TEMPLATE)
				.put("hotel", data.getPropertyName())
				.put("checkout", data.getFallbackCheckOutTime(getContext()))
				.format().toString();
		}
		else {
			title = Phrase.from(getContext(), R.string.check_out_notification_title_day_before_TEMPLATE)
				.put("checkout", data.getFallbackCheckOutTime(getContext()))
				.format().toString();
			body = Phrase.from(getContext(), R.string.check_out_notification_body_day_before_TEMPLATE)
				.put("hotel", data.getPropertyName())
				.put("checkout", data.getFallbackCheckOutTime(getContext()))
				.format().toString();
		}
		notification.setTicker(title);
		notification.setTitle(title);
		notification.setBody(body);

		notification.setImageUrls(data.getHeaderImageUrls());

		return notification;
	}

	@VisibleForTesting
	public Notification generateGetReadyNotification() {
		ItinCardDataHotel data = getItinCardData();

		String itinId = data.getId();

		TripHotel hotel = (TripHotel) getItinCardData().getTripComponent();


		DateTime startDate = roundTime(data.getStartDate());

		MutableDateTime trigger = startDate.toMutableDateTime();
		trigger.addDays(-3);
		long triggerTimeMillis = trigger.getMillis();

		trigger = startDate.toMutableDateTime();
		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId + "_getready", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.HOTEL_GET_READY);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_DIRECTIONS | Notification.FLAG_CALL);
		notification.setIconResId(R.drawable.ic_stat_hotel);

		String title = getContext().getString(R.string.get_ready_for_trip);
		notification.setTicker(title);
		notification.setTitle(title);

		String body = Phrase.from(getContext(), R.string.get_ready_for_trip_body_TEMPLATE)
			.put("firstname", hotel.getPrimaryTraveler().getFirstName())
			.put("hotel", hotel.getProperty().getName())
			.put("startday", data.getFormattedDetailsCheckInDate(getContext()))
			.format().toString();
		notification.setBody(body);

		notification.setImageUrls(data.getHeaderImageUrls());

		return notification;
	}

	public Notification generateActivityCrossSellNotification() {
		ItinCardDataHotel data = getItinCardData();

		String itinId = data.getId();

		DateTime startDate = roundTime(data.getStartDate());

		MutableDateTime trigger = startDate.toMutableDateTime();
		trigger.addDays(-7);
		long triggerTimeMillis = trigger.getMillis();

		trigger = startDate.toMutableDateTime();
		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId + "_activityCross", itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.HOTEL_ACTIVITY_CROSSSEll);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setFlags(Notification.FLAG_LOCAL);
		notification.setIconResId(R.drawable.ic_stat_expedia);

		String title = Phrase.from(getContext(), R.string.hotel_book_activities_cross_sell_notification_title_TEMPLATE)
			.put("destination", data.getPropertyCity())
			.format().toString();

		notification.setTicker(title);
		notification.setTitle(title);

		String body = Phrase.from(getContext(), R.string.hotel_book_activities_cross_sell_notification_body_TEMPLATE)
			.put("destination", data.getPropertyCity())
			.format().toString();
		notification.setBody(body);

		return notification;
	}


	@Override
	public String getSharableImageURL() {

		String sharableImgURL = super.getSharableImageURL();
		//If its in the cache the url failed
		if (FailedUrlCache.getInstance().contains(sharableImgURL)) {
			List<String> urls = getItinCardData().getHeaderImageUrls();
			for (String url : urls) {
				//If its not in the cache it works!
				if (!FailedUrlCache.getInstance().contains(url)) {
					sharableImgURL = url;
					break;
				}
			}
		}

		return sharableImgURL;
	}
	@VisibleForTesting
	public boolean isDurationLongerThanDays(int days) {
		DateTime endDate = getItinCardData().getEndDate();
		DateTime dateToCheck = data.getStartDate().plusDays(days);
		return endDate.isAfter(dateToCheck);

	}
	@VisibleForTesting
	public boolean hasLastDayStarted() {
		MutableDateTime endDate = data.getEndDate().toMutableDateTime();
		endDate.setZoneRetainFields(DateTimeZone.getDefault());
		endDate.setMinuteOfDay(1);
		return endDate.getMillis() < DateTime.now().getMillis();
	}

}
