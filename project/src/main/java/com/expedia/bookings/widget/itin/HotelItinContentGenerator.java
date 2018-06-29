package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.bitmaps.FailedUrlCache;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.notification.HotelNotificationGenerator;
import com.expedia.bookings.notification.INotificationManager;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.tracking.TripsTracking;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.GoogleMapsUtil;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.mobiata.android.SocialUtils;

import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;

public class HotelItinContentGenerator extends ItinContentGenerator<ItinCardDataHotel> {

	@Inject
	public HotelServices hotelServices;
	@Inject
	public INotificationManager notificationManager;
	@Inject
	public HotelNotificationGenerator notificationGenerator;

	private ItinCardDataHotel data;

	public HotelItinContentGenerator(Context context, ItinCardDataHotel data, MediaCallback callback) {
		super(context, data);
		this.setCallback(callback);
		this.data = data;

		Ui.getApplication(getContext()).defaultHotelComponents();
		Ui.getApplication(getContext()).defaultTripComponents();
		Ui.getApplication(getContext()).hotelComponent().inject(this);

		if (data.getProperty().getMediaList().isEmpty()) {
			SuggestionV4 destination = new SuggestionV4();
			destination.gaiaId = data.getProperty().getPropertyId();
			HotelSearchParams params = (HotelSearchParams) new HotelSearchParams.Builder(28, 300)
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

	private Observer<HotelOffersResponse> observer = new DisposableObserver<HotelOffersResponse>() {

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
		return null;
	}

	@Override
	protected boolean addBookingInfo(ViewGroup container) {
		return false;
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
						TripsTracking.trackItinHotelDirections();
					}
				}
			});
	}

	@Override
	@SuppressLint("InflateParams")
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
					TripsTracking.trackItinHotelCall();
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
		return new ArrayList<>();
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
		return notificationGenerator.generateNotifications(getItinCardData());
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
}
