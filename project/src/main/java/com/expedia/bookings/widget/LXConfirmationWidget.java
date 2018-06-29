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
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.AbstractItinDetailsResponse;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.ApiDateUtils;
import com.expedia.bookings.marketing.carnival.CarnivalUtils;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;
import com.expedia.bookings.utils.Font;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.lx.vm.LXConfirmationWidgetViewModel;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;

public class LXConfirmationWidget extends android.widget.LinearLayout {

	LXCheckoutParams lxCheckoutParams;

	public LXConfirmationWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		viewModel = new LXConfirmationWidgetViewModel(context);
		inflate(context, R.layout.widget_lx_confirmation, this);
	}

	public LXConfirmationWidgetViewModel viewModel;

	@BindView(R2.id.confirmation_image_view)
	ImageView confirmationImageView;

	@BindView(R2.id.confirmation_text)
	TextView confirmationText;

	@BindView(R2.id.email_text)
	TextView emailText;

	@BindView(R2.id.title)
	android.widget.TextView title;

	@BindView(R2.id.location)
	android.widget.TextView location;

	@BindView(R2.id.tickets)
	android.widget.TextView tickets;

	@BindView(R2.id.date)
	android.widget.TextView date;

	@BindView(R2.id.toolbar)
	android.support.v7.widget.Toolbar toolbar;

	@BindView(R2.id.text_container)
	ViewGroup textContainer;

	@BindView(R2.id.itin_number)
	TextView itineraryNumber;

	@BindView(R2.id.reservation_confirmation_text)
	TextView reservationConfirmation;

	@Inject
	LXState lxState;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
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
		initializeSubscriptions();
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
			lxState.selectedTicketsCount());
		CarnivalUtils.getInstance().trackLxConfirmation(lxState.activity.title, lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate);

		AdTracker.trackLXBooked(event.checkoutResponse.newTrip.itineraryNumber, lxState.activity.location,
			lxState.latestTotalPrice(), lxState.selectedTickets().get(0).money,
			lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate,
			lxState.activity.title, lxState.activity.id, lxState.searchParams.getActivityStartDate(),
			lxState.activity.regionId, lxState.selectedTicketsCount(), lxState.selectedChildTicketsCount());

		final Resources res = getResources();
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

		viewModel.getConfirmationScreenUiObservable().onNext(Unit.INSTANCE);
	}

	private void initializeSubscriptions() {
		viewModel.getTitleTextObservable().subscribe(RxTextView.text(title));
		viewModel.getTicketsTextObservable().subscribe(RxTextView.text(tickets));
		viewModel.getLocationTextObservable().subscribe(RxTextView.text(location));
		viewModel.getDateTextObservable().subscribe(RxTextView.text(date));
		viewModel.getEmailTextObservable().subscribe(RxTextView.text(emailText));
		viewModel.getConfirmationTextObservable().subscribe(RxTextView.text(confirmationText));
		viewModel.getReservationConfirmationTextObservable().subscribe(RxTextView.text(reservationConfirmation));
		viewModel.getItineraryNumberTextObservable().subscribe(RxTextView.text(itineraryNumber));
		viewModel.getItinDetailsResponseObservable().subscribe(new Observer<AbstractItinDetailsResponse>() {
			@Override
			public void onSubscribe(Disposable d) {
			}

			@Override
			public void onNext(AbstractItinDetailsResponse itinDetailsResponse) {
				OmnitureTracking.trackAppLXConfirmationFromTripsResponse(itinDetailsResponse,
					lxState.activity.id, lxState.selectedTicketsCount(),
					ApiDateUtils.yyyyMMddHHmmssToLocalDate(lxState
						.offer.availabilityInfoOfSelectedDate.availabilities.valueDate),
					lxState.searchParams.getActivityEndDate());
			}

			@Override
			public void onError(Throwable e) {
			}

			@Override
			public void onComplete() {
			}
		});
		viewModel.getConfirmationScreenUiObservable().subscribe(new Observer<Unit>() {
			@Override
			public void onSubscribe(Disposable d) {
			}

			@Override
			public void onNext(Unit unit) {
				List<String> imageURLs = Images
					.getLXImageURLBasedOnWidth(lxState.activity.getImages(), AndroidUtils.getDisplaySize(getContext()).x);
				new PicassoHelper.Builder(confirmationImageView)
					.fade()
					.fit()
					.centerCrop()
					.build()
					.load(imageURLs);

				int statusBarHeight = Ui.getStatusBarHeight(getContext());
				toolbar.setPadding(0, statusBarHeight, 0, 0);
				textContainer.setPadding(0, statusBarHeight, 0, 0);

				Font.ROBOTO_LIGHT.setTypefaceOnTextView(confirmationText);
				Font.ROBOTO_LIGHT.setTypefaceOnTextView(emailText);
				AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
			}

			@Override
			public void onError(Throwable e) {
			}

			@Override
			public void onComplete() {
			}
		});
	}
}
