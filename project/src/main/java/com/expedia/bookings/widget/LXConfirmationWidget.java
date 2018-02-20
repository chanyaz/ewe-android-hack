package com.expedia.bookings.widget;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.ApiDateUtils;
import com.expedia.bookings.utils.CarnivalUtils;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXConfirmationWidget extends android.widget.LinearLayout {

	LXCheckoutParams lxCheckoutParams;
	private boolean isGroundTransport;

	public LXConfirmationWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.widget_lx_confirmation, this);
	}

	@InjectView(R.id.confirmation_image_view)
	ImageView confirmationImageView;

	@InjectView(R.id.confirmation_text)
	TextView confirmationText;

	@InjectView(R.id.email_text)
	TextView emailText;

	@InjectView(R.id.title)
	android.widget.TextView title;

	@InjectView(R.id.location)
	android.widget.TextView location;

	@InjectView(R.id.tickets)
	android.widget.TextView tickets;

	@InjectView(R.id.date)
	android.widget.TextView date;

	@InjectView(R.id.toolbar)
	android.support.v7.widget.Toolbar toolbar;

	@InjectView(R.id.text_container)
	ViewGroup textContainer;

	@InjectView(R.id.itin_number)
	TextView itineraryNumber;

	@InjectView(R.id.reservation_confirmation_text)
	TextView reservationConfirmation;

	@Inject
	LXState lxState;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp).mutate();
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NavUtils.goToItin(getContext());
				Events.post(new Events.FinishActivity());
			}
		});
		toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_close_cont_desc);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onDoCheckoutCall(Events.LXKickOffCheckoutCall event) {
		lxCheckoutParams = event.checkoutParams;
	}

	@Subscribe
	public void onCheckoutSuccess(Events.LXCheckoutSucceeded event) {
		OmnitureTracking.trackAppLXCheckoutConfirmation(event.checkoutResponse, lxState.activity.id,
			ApiDateUtils.yyyyMMddHHmmssToLocalDate(lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate), lxState.searchParams.getActivityEndDate(),
			lxState.selectedTicketsCount(), isGroundTransport);
		CarnivalUtils.getInstance().trackLxConfirmation(lxState.activity.title, lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate);

		AdTracker.trackLXBooked(lxState.activity.location, lxState.latestTotalPrice(), lxState.selectedTickets().get(0).money,
			lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate, lxState.activity.categories,
			event.checkoutResponse, lxState.activity.title, lxState.activity.id, lxState.searchParams.getActivityStartDate(),
			lxState.activity.regionId, lxState.selectedTicketsCount(), lxState.selectedChildTicketsCount());

		final Resources res = getResources();
		List<String> imageURLs = Images
			.getLXImageURLBasedOnWidth(lxState.activity.getImages(), AndroidUtils.getDisplaySize(getContext()).x);
		new PicassoHelper.Builder(confirmationImageView)
			.fade()
			.fit()
			.centerCrop()
			.build()
			.load(imageURLs);
		title.setText(lxState.activity.title);
		tickets.setText(lxState.selectedTicketsCountSummary(getContext()));
		location.setText(lxState.activity.location);
		LocalDate offerSelectedDate = ApiDateUtils.yyyyMMddHHmmssToLocalDate(
			lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
		date.setText(LocaleBasedDateFormatUtils.localDateToEEEMMMd(offerSelectedDate));
		emailText.setText(lxCheckoutParams.email);
		confirmationText.setText(res.getString(R.string.lx_successful_checkout_email_label));
		reservationConfirmation.setText(res.getString(R.string.lx_successful_checkout_reservation_label));
		itineraryNumber.setText(res.getString(R.string.successful_checkout_TEMPLATE, event.checkoutResponse.newTrip.itineraryNumber));

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbar.setPadding(0, statusBarHeight, 0, 0);
		textContainer.setPadding(0, statusBarHeight, 0, 0);

		FontCache.setTypeface(confirmationText, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(emailText, FontCache.Font.ROBOTO_LIGHT);
		AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
	}

	public void setIsFromGroundTransport(boolean isGroundTransport) {
		this.isGroundTransport = isGroundTransport;
	}
}
