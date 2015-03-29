package com.expedia.bookings.widget;

import javax.inject.Inject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXFormatter;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXConfirmationWidget extends android.widget.LinearLayout {

	public LXConfirmationWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.widget_lx_confirmation, this);
	}

	@InjectView(R.id.confirmation_image_view)
	ImageView confirmationImageView;

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

	@InjectView(R.id.itinerary_text_view)
	android.widget.TextView itineraryNumber;

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

	@Subscribe
	public void onCheckoutSuccess(Events.LXCheckoutSucceeded event) {

		OmnitureTracking.trackAppLXCheckoutConfirmation(getContext(), event.checkoutResponse, lxState);

		String url = Images.getLXImageURL(lxState.activity.imageUrl);
		new PicassoHelper.Builder(confirmationImageView)
			.fade()
			.setTag("LX Confirmation")
			.fit()
			.centerCrop()
			.build()
			.load(url);
		title.setText(lxState.offer.title);
		tickets.setText(LXFormatter.selectedTicketsSummaryText(getContext(), lxState.selectedTickets));
		location.setText(lxState.activity.location);
		date.setText(lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
		itineraryNumber.setText(event.checkoutResponse.newTrip.itineraryNumber);
	}

}
