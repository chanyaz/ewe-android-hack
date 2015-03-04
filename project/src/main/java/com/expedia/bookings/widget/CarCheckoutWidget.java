package com.expedia.bookings.widget;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.RateBreakdownItem;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.interfaces.ToolbarListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarCheckoutWidget extends FrameLayout implements SlideToWidgetJB.ISlideToListener {

	public CarCheckoutWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	CarCreateTripResponse createTripResponse;

	@InjectView(R.id.checkout_scroll)
	ScrollView scrollView;

	@InjectView(R.id.hint_container)
	ViewGroup hintContainer;

	@InjectView(R.id.car_vendor_text)
	TextView carCompanyText;

	@InjectView(R.id.legal_information_text_view)
	TextView legalInformationText;

	@InjectView(R.id.category_title_text)
	TextView categoryTitleText;

	@InjectView(R.id.car_model_text)
	TextView carModelText;

	@InjectView(R.id.location_description_text)
	TextView locationDescriptionText;

	@InjectView(R.id.airport_text)
	TextView airportText;

	@InjectView(R.id.date_time_text)
	TextView dateTimeText;

	@InjectView(R.id.free_cancellation_text)
	TextView freeCancellationText;

	@InjectView(R.id.unlimited_mileage_text)
	TextView unlimitedMileageText;

	@InjectView(R.id.login_widget)
	AccountLoginWidget loginWidget;

	@InjectView(R.id.price_text)
	TextView tripTotalText;

	@InjectView(R.id.purchase_total_text_view)
	TextView sliderTotalText;

	@InjectView(R.id.slide_to_purchase_layout)
	ViewGroup slideToContainer;

	@InjectView(R.id.slide_to_purchase_widget)
	SlideToWidgetJB slideWidget;

	@InjectView(R.id.summery_container)
	CardView summeryContainer;

	@InjectView(R.id.driver_info_card_view)
	CarDriverWidget driverInfoCardView;

	@InjectView(R.id.payment_info_card_view)
	PaymentWidget paymentInfoCardView;

	@InjectView(R.id.checkout_toolbar)
	Toolbar toolbar;

	MenuItem menuNext;
	MenuItem menuDone;

	ExpandableCardView mLastExpandedCard;
	ExpandableCardView mCurrentExpandedCard;
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		slideWidget.addSlideToListener(this);

		loginWidget.setToolbarListener(toolbarListener);
		loginWidget.setLoginStatusListener(mLoginStatusListener);
		driverInfoCardView.setToolbarListener(toolbarListener);
		paymentInfoCardView.setToolbarListener(toolbarListener);

		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		toolbar.setTitle(getContext().getString(R.string.cars_checkout_text));
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setBackgroundColor(getResources().getColor(R.color.cars_primary_color));
		toolbar.inflateMenu(R.menu.cars_checkout_menu);

		menuNext = toolbar.getMenu().findItem(R.id.menu_next);
		menuNext.setVisible(false);

		menuDone = toolbar.getMenu().findItem(R.id.menu_done);
		menuDone.setVisible(false);

		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_checkout:
					Ui.hideKeyboard(CarCheckoutWidget.this);
					menuItem.setVisible(false);
					slideToContainer.setVisibility(View.VISIBLE);
					break;
				case R.id.menu_next:
					mCurrentExpandedCard.setNextFocus();
					break;
				case R.id.menu_done:
					mCurrentExpandedCard.onDonePressed();
					Ui.hideKeyboard(CarCheckoutWidget.this);
					break;
				}

				return false;
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = getContext().getResources().getColor(R.color.cars_status_bar_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, scrollView, color));
		}

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
	public void onShowCheckout(Events.CarsShowCheckout event) {
		bind(event.createTripResponse);
	}

	private void bind(CarCreateTripResponse createTrip) {
		createTripResponse = createTrip;
		CreateTripCarOffer offer = createTripResponse.carProduct;

		paymentInfoCardView.setCreditCardRequired(offer.checkoutRequiresCard);

		locationDescriptionText.setText(offer.pickUpLocation.airportInstructions);

		slideWidget.resetSlider();

		carCompanyText.setText(offer.vendor.name);
		categoryTitleText.setText(offer.vehicleInfo.category + " " + offer.vehicleInfo.type);
		carModelText.setText(getContext().getString(R.string.car_model_name_template, offer.vehicleInfo.makes.get(0)));
		airportText.setText(offer.pickUpLocation.locationDescription);
		tripTotalText.setText(offer.detailedFare.grandTotal.formattedPrice);
		sliderTotalText.setText(getResources()
			.getString(R.string.your_card_will_be_charged_TEMPLATE, offer.detailedFare.grandTotal.formattedPrice));

		dateTimeText.setText(DateFormatUtils
			.formatDateTimeRange(getContext(), offer.pickupTime, offer.dropOffTime,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT));

		Drawable drawableEnabled = getResources().getDrawable(R.drawable.ic_action_bar_checkmark_white);
		drawableEnabled.setColorFilter(getResources().getColor(R.color.cars_checkmark_color), PorterDuff.Mode.SRC_IN);
		freeCancellationText.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);
		unlimitedMileageText.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);
		driverInfoCardView.setExpanded(false);
		paymentInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(View.GONE);
		legalInformationText.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());
		isCheckoutComplete();
	}

	@Subscribe
	public void onShowConfirmation(Events.CarsShowConfirmation event) {
		slideWidget.resetSlider();
	}

	//  SlideToWidget.ISlideToListener

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideProgress(float pixels, float total) {
	}

	@Override
	public void onSlideAllTheWay() {
		CarCheckoutParamsBuilder builder =
			new CarCheckoutParamsBuilder()
				.firstName(driverInfoCardView.firstName.getText().toString())
				.lastName(driverInfoCardView.lastName.getText().toString())
				.emailAddress(driverInfoCardView.emailAddress.getText().toString())
				.grandTotal(createTripResponse.carProduct.detailedFare.grandTotal)
				.phoneCountryCode(Integer.toString(driverInfoCardView.phoneSpinner.getSelectedTelephoneCountryCode()))
				.phoneNumber(driverInfoCardView.phoneNumber.getText().toString())
				.tripId(createTripResponse.tripId);
		Events.post(new Events.CarsKickOffCheckoutCall(builder));
	}

	@Override
	public void onSlideAbort() {
	}

	// Listener to update the toolbar status when a widget(Login, Driver Info, Payment) is being interacted with
	ToolbarListener toolbarListener = new ToolbarListener() {
		@Override
		public void setActionBarTitle(String title) {
			toolbar.setTitle(title);
		}

		@Override
		public void onWidgetExpanded(ExpandableCardView cardView) {
			mLastExpandedCard = mCurrentExpandedCard;
			mCurrentExpandedCard = cardView;
			expandWidget(mCurrentExpandedCard);
		}

		@Override
		public void onWidgetClosed() {
			closeWidget();
		}

		@Override
		public void onEditingComplete() {
			menuNext.setVisible(false);
			menuDone.setVisible(true);
		}
	};

	private void expandWidget(final View v) {
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.smoothScrollTo(0, 0);
				summeryContainer.setVisibility(GONE);
				if (loginWidget != mCurrentExpandedCard) {
					loginWidget.setVisibility(GONE);
				}
				hintContainer.setVisibility(GONE);
				if (driverInfoCardView != mCurrentExpandedCard) {
					driverInfoCardView.setVisibility(GONE);
				}
				if (paymentInfoCardView != mCurrentExpandedCard) {
					paymentInfoCardView.setVisibility(GONE);
				}
				legalInformationText.setVisibility(GONE);
				if (mLastExpandedCard != null && mLastExpandedCard != mCurrentExpandedCard) {
					mLastExpandedCard.setExpanded(false, false);
				}
			}
		});
		toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentExpandedCard.setExpanded(false);
			}
		});
		menuNext.setVisible(true);
		menuDone.setVisible(false);
		slideToContainer.setVisibility(GONE);
	}

	private void closeWidget() {
		Ui.hideKeyboard(CarCheckoutWidget.this);
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.smoothScrollTo(0, 0);
				summeryContainer.setVisibility(VISIBLE);
				loginWidget.setVisibility(VISIBLE);
				hintContainer.setVisibility(VISIBLE);
				driverInfoCardView.setVisibility(VISIBLE);
				if (paymentInfoCardView.isCreditCardRequired()) {
					paymentInfoCardView.setVisibility(VISIBLE);
				}
				legalInformationText.setVisibility(VISIBLE);
				// After user enters either the payment/driver widget (i.e. data changed) let's check the checkout status. If complete, show slider widget.
				isCheckoutComplete();
			}
		});
		toolbar.setTitle(getContext().getString(R.string.cars_checkout_text));
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		menuNext.setVisible(false);
		menuDone.setVisible(false);
	}

	private void isCheckoutComplete() {
		if (driverInfoCardView.isComplete() && paymentInfoCardView.isComplete()) {
			slideToContainer.setVisibility(VISIBLE);
		}
		else {
			slideToContainer.setVisibility(GONE);
		}
	}

	@OnClick(R.id.price_text)
	public void showCarCostBreakdown() {
		buildCarBreakdownDialog(getContext(), createTripResponse.carProduct);
	}

	public static void buildCarBreakdownDialog(Context context, CreateTripCarOffer offer) {
		List<RateBreakdownItem> rateBreakdownDueAtPickup = offer.detailedFare.priceBreakdownOfTotalDueAtPickup;
		List<RateBreakdownItem> rateBreakdownDueToday = offer.detailedFare.priceBreakdownOfTotalDueToday;

		View view = LayoutInflater.from(context).inflate(R.layout.car_cost_summary_alert, null);
		LinearLayout ll = Ui.findView(view, R.id.parent);

		for (RateBreakdownItem item : rateBreakdownDueAtPickup) {
			ll.addView(
				addRow(context, CarDataUtils.getFareBreakdownType(context, item.type), item.price.formattedPrice));
		}

		for (RateBreakdownItem item : rateBreakdownDueToday) {
			ll.addView(
				addRow(context, CarDataUtils.getFareBreakdownType(context, item.type), item.price.formattedPrice));
		}

		ll.addView(addRow(context, context.getString(R.string.car_cost_breakdown_due_today),
			offer.detailedFare.totalDueToday.formattedPrice));
		ll.addView(addRow(context, context.getString(R.string.car_cost_breakdown_total_due),
			offer.detailedFare.totalDueAtPickup.formattedPrice));

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(view);
		builder.setPositiveButton(context.getString(R.string.car_cost_breakdown_button_text),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		builder.create().show();
	}

	public static View addRow(Context context, String leftSideText, String rightSideText) {
		View row = LayoutInflater.from(context).inflate(R.layout.car_cost_summary_row, null);
		TextView priceDescription = Ui.findView(row, R.id.price_type_text_view);
		TextView priceValue = Ui.findView(row, R.id.price_text_view);
		priceDescription.setText(leftSideText);
		priceValue.setText(rightSideText);
		return row;
	}

	AccountLoginWidget.LogInStatusListener mLoginStatusListener = new AccountLoginWidget.LogInStatusListener() {
		@Override
		public void onLoginStarted() {

		}

		@Override
		public void onLoginCompleted() {
			driverInfoCardView.onLogin();
			paymentInfoCardView.onLogin();
		}

		@Override
		public void onLoginFailed() {

		}

		@Override
		public void onLogout() {
			driverInfoCardView.onLogout();
			paymentInfoCardView.onLogout();
		}
	};

}
