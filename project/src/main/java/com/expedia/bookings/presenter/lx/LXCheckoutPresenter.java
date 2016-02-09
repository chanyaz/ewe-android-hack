package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVEntryWidget;
import com.expedia.bookings.widget.LXCheckoutWidget;
import com.expedia.bookings.widget.LXErrorWidget;
import com.expedia.bookings.widget.LxRulesWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class LXCheckoutPresenter extends Presenter {
	public LXCheckoutPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.lx_checkout_presenter, this);
	}

	@Inject
	LXServices lxServices;

	@InjectView(R.id.checkout)
	LXCheckoutWidget checkout;

	@InjectView(R.id.rules)
	LxRulesWidget rules;

	@InjectView(R.id.cvv)
	CVVEntryWidget cvv;

	@InjectView(R.id.lx_checkout_error_widget)
	LXErrorWidget errorScreen;

	private LXCheckoutParams checkoutParams;
	private ProgressDialog checkoutDialog;
	private Subscription checkoutSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		addDefaultTransition(defaultCheckoutTransition);
		addTransition(checkoutToRules);
		addTransition(checkoutToCvv);
		addTransition(cvvToError);
		addTransition(checkoutToError);

		cvv.setCVVEntryListener(checkout);

		checkoutDialog = new ProgressDialog(getContext());
		checkoutDialog.setMessage(getResources().getString(R.string.booking_loading));
		checkoutDialog.setIndeterminate(true);
	}

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	private void cleanup() {
		if (checkoutSubscription != null) {
			checkoutSubscription.unsubscribe();
			checkoutSubscription = null;
		}

	}

	private void showAlertMessage(String message, String confirmButton) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(message)
			.setNeutralButton(confirmButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}

	private Observer<LXCheckoutResponse> checkoutObserver = new Observer<LXCheckoutResponse>() {
		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
			Log.e("LXCheckout - onError", e);
			checkoutDialog.dismiss();

			if (RetrofitUtils.isNetworkError(e)) {
				showCheckoutErrorDialog(R.string.error_no_internet);
			}
			else if (e instanceof ApiError) {
				showErrorScreen((ApiError) e);
			}
			else {
				showErrorScreen(null);
			}
		}

		@Override
		public void onNext(LXCheckoutResponse lxCheckoutResponse) {
			checkoutDialog.dismiss();
			if (lxCheckoutResponse == null) {
				showErrorScreen(null);
			}
			else if (lxCheckoutResponse.hasPriceChange()) {
				Events.post(new Events.LXUpdateCheckoutSummaryAfterPriceChange(lxCheckoutResponse));
				showErrorScreen(lxCheckoutResponse.getFirstError());
			}
			else {
				Events.post(new Events.LXCheckoutSucceeded(lxCheckoutResponse));
				// Add guest itin to itin manager
				refreshGuestTrip(lxCheckoutResponse);
			}
		}
	};

	private DefaultTransition defaultCheckoutTransition = new DefaultTransition(LXCheckoutWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			checkout.setVisibility(View.VISIBLE);
			cvv.setVisibility(View.GONE);
			errorScreen.setVisibility(View.GONE);
		}
	};

	private Transition checkoutToRules = new VisibilityTransition(this, LXCheckoutWidget.class, LxRulesWidget.class);

	private Transition checkoutToCvv = new VisibilityTransition(this, CVVEntryWidget.class, LXCheckoutWidget.class);

	private Transition cvvToError = new VisibilityTransition(this, CVVEntryWidget.class, LXErrorWidget.class);

	private Transition checkoutToError = new VisibilityTransition(this, LXCheckoutWidget.class, LXErrorWidget.class) {
		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			if (!forward) {
				checkout.slideWidget.resetSlider();
				checkout.checkoutFormWasUpdated();
			}
		}
	};
	/**
	 * Events
	 */

	@Subscribe
	public void showPriceChange(Events.LXShowCheckoutAfterPriceChange event) {
		show(checkout, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void onShowCVV(Events.ShowCVV event) {
		show(cvv);
		BillingInfo billingInfo = event.billingInfo;
		cvv.bind(billingInfo);
		OmnitureTracking.trackAppLXCheckoutCvvScreen();
	}

	@Subscribe
	public void showLxRulesOnCheckout(Events.LXShowRulesOnCheckout event) {
		show(rules);
	}

	@Subscribe
	public void onDoCheckoutCall(Events.LXKickOffCheckoutCall event) {
		checkoutParams = event.checkoutParams;
		if (checkoutParams.areRequiredParamsFilled()) {
			checkoutSubscription = lxServices.lxCheckout(checkoutParams, checkoutObserver);
			checkoutDialog.show();
		}
		else {
			String msg = getResources().getString(R.string.error_missing_checkout_params);
			String btn = getResources().getString(R.string.ok);
			showAlertMessage(msg, btn);
		}
	}

	@Subscribe
	public void showInvalidInput(Events.LXInvalidInput event) {
		show(checkout, FLAG_CLEAR_TOP);
		checkout.slideWidget.resetSlider();
		checkout.mainContactInfoCardView.setExpanded(true, true);
		checkout.mainContactInfoCardView.setInvalid(event.field);
	}

	@Subscribe
	public void showSessionTimeout(Events.LXSessionTimeout event) {
		clearBackStack();
		((AppCompatActivity) getContext()).onBackPressed();
	}

	@Subscribe
	public void showPaymentFailed(Events.LXPaymentFailed event) {
		show(checkout, FLAG_CLEAR_TOP);
		checkout.slideWidget.resetSlider();
		checkout.paymentInfoCardView.getCardInfoContainer().performClick();
	}

	private void showCheckoutErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXKickOffCheckoutCall(checkoutParams));
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					checkout.slideWidget.resetSlider();
				}
			})
			.show();
	}

	private void showErrorScreen(ApiError error) {
		errorScreen.bind(error);
		show(errorScreen);
	}

	private void refreshGuestTrip(LXCheckoutResponse checkoutResponse) {
		if (!User.isLoggedIn(getContext())) {
			String email = checkoutParams.getEmailAddress();
			String itineraryNumber = checkoutResponse.newTrip.itineraryNumber;
			ItineraryManager.getInstance().addGuestTrip(email, itineraryNumber);
		}
	}

	@Subscribe
	public void onOfferBooked(Events.LXOfferBooked event) {
		checkout.showCheckout();
		show(checkout);
	}

	@Subscribe
	public void onShowErrorScreen(Events.LXError event) {
		errorScreen.bind(event.apiError);
		show(errorScreen, FLAG_CLEAR_BACKSTACK);
	}
}
