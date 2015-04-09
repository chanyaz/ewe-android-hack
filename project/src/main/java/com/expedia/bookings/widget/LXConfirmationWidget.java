package com.expedia.bookings.widget;

import javax.inject.Inject;


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
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXFormatter;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXConfirmationWidget extends android.widget.LinearLayout {

	LXCheckoutParams lxCheckoutParams;

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

	@InjectView(R.id.support_action_textView)
	TextView supportTextView;

	@InjectView(R.id.text_container)
	ViewGroup textContainer;

	@Inject
	LXState lxState;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
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
		lxCheckoutParams = event.checkoutParamsBuilder.build();
	}

	@Subscribe
	public void onCheckoutSuccess(Events.LXCheckoutSucceeded event) {
		OmnitureTracking.trackAppLXCheckoutConfirmation(getContext(), event.checkoutResponse, lxState);

		final Resources res = getResources();
		String url = Images.getLXImageURL(lxState.activity.imageUrl);
		new PicassoHelper.Builder(confirmationImageView)
			.fade()
			.fit()
			.centerCrop()
			.build()
			.load(url);
		title.setText(lxState.offer.title);
		tickets.setText(LXFormatter.selectedTicketsSummaryText(getContext(), lxState.selectedTickets));
		location.setText(lxState.activity.location);
		date.setText(lxState.offer.availabilityInfoOfSelectedDate.availabilities.displayDate);
		emailText.setText(lxCheckoutParams.email);
		confirmationText.setText(res.getString(R.string.itinerary_confirmation_TEMPLATE,
			event.checkoutResponse.newTrip.itineraryNumber));

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbar.setPadding(0, statusBarHeight, 0, 0);
		textContainer.setPadding(0, statusBarHeight, 0, 0);

		FontCache.setTypeface(confirmationText, FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface(emailText, FontCache.Font.ROBOTO_LIGHT);
	}

	@OnClick(R.id.support_action_textView)
	public void callCustomerSupport() {
		String phoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser());
		SocialUtils.call(getContext(), phoneNumber);
	}
}
