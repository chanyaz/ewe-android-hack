package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.data.DefaultMedia;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.ImageType;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.InfoTripletView;
import com.mobiata.android.SocialUtils;

public class ActivityItinContentGenerator extends ItinContentGenerator<ItinCardDataActivity> {

	@Override
	public List<? extends IMedia> getHeaderBitmapDrawable() {
		ArrayList<DefaultMedia> mediaList = new ArrayList<>();
		ArrayList<String> imageUrlList = new ArrayList<>();
		ItinCardDataActivity data = getItinCardData();
		if (data.getImageUrl() !=  null && !data.getImageUrl().isEmpty()) {
			imageUrlList.add(data.getImageUrl());
			mediaList.add(new DefaultMedia(imageUrlList, "Image Gallery", getHeaderImagePlaceholderResId()));
		}
		else {
			DefaultMedia placeholder = new DefaultMedia(Collections.<String>emptyList(), "", getHeaderImagePlaceholderResId());
			placeholder.setIsPlaceholder(true);
			mediaList.add(placeholder);
			setHideDetailsTypeIcon(false);
			setHideDetailsTitle(false);
		}
		return mediaList;
	}

	private static final int[] GUEST_ICONS = new int[] {
		R.drawable.bg_activities_guest_cirlce_blue,
		R.drawable.bg_activities_guest_cirlce_orange,
		R.drawable.bg_activities_guest_cirlce_green,
		R.drawable.bg_activities_guest_cirlce_turquoise,
		R.drawable.bg_activities_guest_cirlce_red,
		R.drawable.bg_activities_guest_cirlce_purple,
		R.drawable.bg_activities_guest_cirlce_yellow,
	};

	public ActivityItinContentGenerator(Context context, ItinCardDataActivity data) {
		super(context, data);
	}

	@Override
	public int getTypeIconResId() {
		return Ui.obtainThemeResID(getContext(), R.attr.itin_card_list_icon_activity_drawable);
	}

	@Override
	public Type getType() {
		return Type.ACTIVITY;
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
	public int getHeaderImagePlaceholderResId() {
		return Ui.obtainThemeResID(getContext(), R.attr.skin_itinActivityPlaceholderDrawable);
	}

	@Override
	public String getHeaderText() {
		return getItinCardData().getTitle();
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_activity);
	}

	@Override
	public View getTitleView(View convertView, ViewGroup container) {
		// We don't need to do anything after the initial setup (if we're reusing)
		if (convertView != null) {
			return convertView;
		}

		TextView view = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_title_generic, container,
			false);
		view.setText(R.string.activity_information);
		return view;
	}

	@Override
	public View getSummaryView(View convertView, ViewGroup container) {
		TextView view = (TextView) convertView;
		if (view == null) {
			view = (TextView) getLayoutInflater()
				.inflate(R.layout.include_itin_card_summary_activity, container, false);
		}

		view.setText(HtmlCompat.fromHtml(getContext().getString(R.string.itin_card_activity_summary_TEMPLATE,
			getItinCardData().getLongFormattedValidDate(getContext()))));

		return view;
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
		ItinCardDataActivity itinCardData = getItinCardData();

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_activity, container, false);

		// Find
		InfoTripletView infoTriplet = Ui.findView(view, R.id.info_triplet);
		ViewGroup guestsLayout = Ui.findView(view, R.id.guests_layout);
		ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);

		// Bind
		Resources res = getResources();
		CharSequence[] infoTripletValues;
		CharSequence[] infoTripletLabels;
		/**
		 * Activity read trip returns guest count as 0 when only Summary details are present for the trip.
		 * Displaying only Start and Expire in such cases.
		 */
		if (itinCardData.getGuestCount() != 0) {
			infoTripletValues = new CharSequence[] {
				itinCardData.getFormattedValidDate(getContext()), itinCardData.getFormattedExpirationDate(getContext()),
				itinCardData.getFormattedGuestCount()
			};
			infoTripletLabels = new CharSequence[] {
				res.getString(R.string.itin_card_details_active),
				res.getString(R.string.itin_card_details_expires),
				res.getQuantityText(R.plurals.number_of_guests_label, itinCardData.getGuestCount())
			};
		}
		else {
			infoTripletValues = new CharSequence[] {
				itinCardData.getFormattedValidDate(getContext()), itinCardData.getFormattedExpirationDate(getContext())
			};
			infoTripletLabels = new CharSequence[] {
				res.getString(R.string.itin_card_details_active),
				res.getString(R.string.itin_card_details_expires)
			};
		}

		infoTriplet.setValues(infoTripletValues);
		infoTriplet.setLabels(infoTripletLabels);

		final List<Traveler> travelers = itinCardData.getTravelers();
		final int size = travelers == null ? 0 : travelers.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				final TextView guestView = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_guest,
					container, false);
				final Traveler traveler = travelers.get(i);
				final int resId = GUEST_ICONS[i % GUEST_ICONS.length];

				guestView.setText(traveler.getFullName());

				guestsLayout.addView(guestView);
			}
		}
		else {
			Ui.findView(view, R.id.guests_header).setVisibility(View.GONE);
		}

		//Add shared data
		addSharedGuiElements(commonItinDataContainer);

		return view;
	}

	@Override
	public SummaryButton getSummaryLeftButton() {
		return new SummaryButton(Ui.obtainThemeResID(getContext(), R.attr.itin_card_summary_left_action_button_activity_drawable), getContext().getString(R.string.itin_action_redeem),
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					Context context = getContext();
					Intent intent = getItinCardData().buildRedeemIntent(context);
					context.startActivity(intent);
					OmnitureTracking.trackItinActivityRedeem();
				}
			});
	}

	@Override
	public SummaryButton getSummaryRightButton() {
		ItinCardDataActivity itinCardData = getItinCardData();
		final String supportPhoneNumber = itinCardData.getBestSupportPhoneNumber(getContext());
		final String actionText = getContext().getString(R.string.itin_action_call_support_cont_desc);

		if (TextUtils.isEmpty(supportPhoneNumber)) {
			return getSupportSummaryButton();
		}

		return new SummaryButton(Ui.obtainThemeResID(getContext(), R.attr.itin_card_summary_right_action_button_activity_drawable), getContext().getString(
			R.string.itin_action_support), actionText,
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getContext(), supportPhoneNumber);
					OmnitureTracking.trackItinActivitySupport();
				}
			});
	}

	@Override
	public List<Intent> getAddToCalendarIntents() {
		return new ArrayList<>();
	}

	// Facebook

	@Override
	public String getFacebookShareName() {
		return getItinCardData().getTitle();
	}

	//////////////////////////////////////////////////////////////////////////
	// Notifications
	//////////////////////////////////////////////////////////////////////////

	@Override
	public List<Notification> generateNotifications() {
		ArrayList<Notification> notifications = new ArrayList<>(2);
		notifications.add(generateActivityStartNotification());
		return notifications;
	}

	// https://mingle.karmalab.net/projects/eb_ad_app/cards/880
	// Given I have an activity, when it is 12 hours prior to the validity start
	// date, then I want to receive a notification that reads "Your Universal
	// Studios ticket can be redeemed starting tomorrow."
	// Update: use Noon on the day before, instead of "12 hours prior"
	private Notification generateActivityStartNotification() {
		ItinCardDataActivity data = getItinCardData();

		String itinId = data.getId();

		MutableDateTime trigger = data.getValidDate().toMutableDateTime();
		trigger.setZoneRetainFields(DateTimeZone.getDefault());
		trigger.setRounding(trigger.getChronology().minuteOfHour());
		trigger.addDays(-1);
		trigger.setHourOfDay(12);
		long triggerTimeMillis = trigger.getMillis();

		trigger.setHourOfDay(23);
		trigger.setMinuteOfHour(59);
		long expirationTimeMillis = trigger.getMillis();

		Notification notification = new Notification(itinId, itinId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.ACTIVITY_START);
		notification.setExpirationTimeMillis(expirationTimeMillis);
		notification.setImageType(ImageType.ACTIVITY);
		notification.setFlags(Notification.FLAG_LOCAL | Notification.FLAG_REDEEM | Notification.FLAG_CALL);

		String title = getContext().getString(R.string.Activity_starting_soon);
		notification.setTicker(title);
		notification.setTitle(title);

		String body = getContext().getString(R.string.Your_X_ticket_can_be_redeemed_TEMPLATE, data.getTitle());
		notification.setBody(body);

		return notification;
	}
}
