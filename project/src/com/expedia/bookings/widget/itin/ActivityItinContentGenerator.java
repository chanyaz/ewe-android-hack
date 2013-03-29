package com.expedia.bookings.widget.itin;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.data.trips.CustomerSupport;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripActivity;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.InfoTripletView;
import com.mobiata.android.SocialUtils;

public class ActivityItinContentGenerator extends ItinContentGenerator<ItinCardDataActivity> {

	private static final int[] GUEST_ICONS = new int[] {
		R.drawable.bg_activities_guest_cirlce_blue,
		R.drawable.bg_activities_guest_cirlce_orange,
		R.drawable.bg_activities_guest_cirlce_green,
		R.drawable.bg_activities_guest_cirlce_turquoise,
		R.drawable.bg_activities_guest_cirlce_red,
		R.drawable.bg_activities_guest_cirlce_purple,
		R.drawable.bg_activities_guest_cirlce_yellow
	};

	public ActivityItinContentGenerator(Context context, ItinCardDataActivity data) {
		super(context, data);

		setHideDetailsTypeIcon(false);
		setHideDetailsTitle(false);
	}

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_activity;
	}

	@Override
	public Type getType() {
		return Type.ACTIVITY;
	}

	@Override
	public String getShareSubject() {
		String template = getContext().getString(R.string.share_template_subject_activity);
		String title = getItinCardData().getTitle();

		return String.format(template, title);
	}

	@Override
	public String getShareTextShort() {
		ItinCardDataActivity itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_short_activity);
		String title = itinCardData.getTitle();
		String validDate = itinCardData.getFormattedShareValidDate(getContext());
		String expirationDate = itinCardData.getFormattedShareExpiresDate(getContext());

		return String.format(template, title, validDate, expirationDate);
	}

	@Override
	public String getShareTextLong() {
		ItinCardDataActivity itinCardData = getItinCardData();

		String template = getContext().getString(R.string.share_template_long_activity);
		String title = itinCardData.getTitle();
		String validDate = itinCardData.getFormattedShareValidDate(getContext());
		String expirationDate = itinCardData.getFormattedShareExpiresDate(getContext());
		String downloadUrl = PointOfSale.getPointOfSale().getAppInfoUrl();

		final List<Traveler> travelers = itinCardData.getTravelers();
		final int guestCount = travelers.size();
		final String[] guests = new String[guestCount];
		for (int i = 0; i < guestCount; i++) {
			guests[i] = travelers.get(i).getFullName();
		}

		StringBuilder builder = new StringBuilder();

		builder.append(String.format(template, title, validDate, expirationDate, TextUtils.join("\n", guests)));

		builder.append("\n\n");
		builder.append(getContext().getString(R.string.share_template_long_ad, downloadUrl));

		return builder.toString();
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.itin_header_placeholder_activities;
	}

	@Override
	public List<String> getHeaderImageUrls() {
		return null;
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

		view.setText(Html.fromHtml(getContext().getString(R.string.itin_card_activity_summary_TEMPLATE,
				getItinCardData().getLongFormattedValidDate(getContext()))));

		return view;
	}

	@Override
	public View getDetailsView(ViewGroup container) {
		ItinCardDataActivity itinCardData = getItinCardData();

		View view = getLayoutInflater().inflate(R.layout.include_itin_card_details_activity, container, false);

		// Find
		InfoTripletView infoTriplet = Ui.findView(view, R.id.info_triplet);
		ViewGroup guestsLayout = Ui.findView(view, R.id.guests_layout);
		ViewGroup commonItinDataContainer = Ui.findView(view, R.id.itin_shared_info_container);

		// Bind
		Resources res = getResources();
		infoTriplet.setValues(
				itinCardData.getFormattedValidDate(getContext()),
				itinCardData.getFormattedExpirationDate(getContext()),
				itinCardData.getFormattedGuestCount());
		infoTriplet.setLabels(
				res.getString(R.string.itin_card_details_active),
				res.getString(R.string.itin_card_details_expires),
				res.getQuantityText(R.plurals.number_of_guests_label, itinCardData.getGuestCount()));

		final List<Traveler> travelers = itinCardData.getTravelers();
		final int size = travelers == null ? 0 : travelers.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				final TextView guestView = (TextView) getLayoutInflater().inflate(R.layout.include_itin_card_guest,
						null);
				final Traveler traveler = travelers.get(i);
				final int resId = GUEST_ICONS[i % GUEST_ICONS.length];

				guestView.setText(traveler.getFullName());
				guestView.setCompoundDrawables(createGuestIcon(traveler, resId), null, null, null);

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
		return new SummaryButton(R.drawable.ic_printer_redeem, getContext().getString(R.string.itin_action_redeem),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
						builder.setUrl(getItinCardData().getVoucherPrintUrl());
						builder.setTitle(R.string.webview_title_print_vouchers);
						builder.setTheme(R.style.ItineraryTheme);
						builder.setAllowMobileRedirects(false);
						getContext().startActivity(builder.getIntent());

						OmnitureTracking.trackItinActivityRedeem(getContext());
					}
				});
	}

	@Override
	public SummaryButton getSummaryRightButton() {
		String phoneNumber = "";

		ItinCardDataActivity itinCardData = getItinCardData();
		TripActivity tripActivity = itinCardData != null ? (TripActivity) itinCardData.getTripComponent() : null;
		Trip trip = tripActivity != null ? tripActivity.getParentTrip() : null;
		CustomerSupport support = trip != null ? trip.getCustomerSupport() : null;
		if (support != null) {
			if (PointOfSale.getPointOfSale(getContext()).getPointOfSaleId() == PointOfSaleId.UNITED_STATES
					&& !TextUtils.isEmpty(support.getSupportPhoneNumberDomestic())) {
				phoneNumber = support.getSupportPhoneNumberDomestic();
			}
			else if (!TextUtils.isEmpty(support.getSupportPhoneNumberInternational())) {
				phoneNumber = support.getSupportPhoneNumberInternational();
			}
		}

		if (TextUtils.isEmpty(phoneNumber)) {
			return getSupportSummaryButton();
		}
		else {
			final String finalPhoneNumber = phoneNumber;
			return new SummaryButton(R.drawable.ic_phone, getContext().getString(R.string.itin_action_support),
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							SocialUtils.call(getContext(), finalPhoneNumber);
							OmnitureTracking.trackItinActivitySupport(getContext());
						}
					});
		}
	}

	@SuppressLint("DefaultLocale")
	private Drawable createGuestIcon(Traveler travler, int iconResId) {
		final String text = travler.getFirstName().substring(0, 1).toUpperCase();
		final Resources res = getResources();

		final Bitmap bitmap = BitmapFactory.decodeResource(res, iconResId).copy(Bitmap.Config.ARGB_8888, true);
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();

		final Paint paint = new Paint();
		paint.setColor(res.getColor(R.color.itin_white_text));
		paint.setTextSize(height * 0.8f);
		paint.setTypeface(FontCache.getTypeface(Font.ROBOTO_BOLD));
		paint.setAntiAlias(true);

		final Rect bounds = new Rect();
		paint.getTextBounds(text, 0, 1, bounds);

		final int textX = (width - bounds.width()) / 2;
		final int textY = height - ((height - bounds.height()) / 2);

		final Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, textX, textY, paint);

		BitmapDrawable drawable = new BitmapDrawable(res, bitmap);
		drawable.setBounds(0, 0, width, height);

		return drawable;
	}
}
