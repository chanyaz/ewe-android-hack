package com.expedia.bookings.presenter.car;

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
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVEntryWidget;
import com.expedia.bookings.widget.CarCheckoutMainViewPresenter;
import com.expedia.bookings.widget.ErrorWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarCheckoutPresenter extends Presenter {

	public CarCheckoutPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.car_checkout_presenter, this);
	}

	@Inject
	CarServices carServices;

	//@InjectView(R.id.checkout)
	CarCheckoutMainViewPresenter checkout;

	//@InjectView(R.id.cvv)
	CVVEntryWidget cvv;

	//@InjectView(R.id.checkout_error_widget)
	ErrorWidget errorScreen;

	private ProgressDialog checkoutDialog;
	private Subscription checkoutSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).carComponent().inject(this);

		addTransition(checkoutToCvv);
		addTransition(checkoutToError);
		addTransition(cvvToError);
		addDefaultTransition(defaultCheckoutTransition);

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

	private Observer<CarCheckoutResponse> checkoutObserver = new Observer<CarCheckoutResponse>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("CarCheckout - onError", e);
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
		public void onNext(CarCheckoutResponse response) {
			checkoutDialog.dismiss();

			if (response == null) {
				showErrorScreen(null);
			}
			else if (response.hasPriceChange()) {
				Events.post(new Events.CarsUpdateCheckoutSummaryAfterPriceChange(response.originalCarProduct,
					response.newCarProduct,
					response.tripId));
				showErrorScreen(response.getFirstError());
			}
			else {
				Events.post(new Events.CarsShowConfirmation(response));
			}
		}
	};

	private void showErrorScreen(ApiError error) {
		errorScreen.bind(error);
		show(errorScreen);
	}

	private void showCheckoutErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsKickOffCheckoutCall(checkoutParamsBuilder));
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

	private Transition checkoutToCvv = new VisibilityTransition(this, CarCheckoutMainViewPresenter.class, CVVEntryWidget.class);
	private Transition checkoutToError = new VisibilityTransition(this, CarCheckoutMainViewPresenter.class, ErrorWidget.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			if (!forward) {
				checkout.slideWidget.resetSlider();
				checkout.checkoutFormWasUpdated();
			}
		}
	};
	private Transition cvvToError = new VisibilityTransition(this, CVVEntryWidget.class, ErrorWidget.class);
	private DefaultTransition defaultCheckoutTransition = new DefaultTransition(CarCheckoutMainViewPresenter.class.getName()) {
		@Override
		public void endTransition(boolean forward) {
			checkout.setVisibility(View.VISIBLE);
			cvv.setVisibility(View.GONE);
			errorScreen.setVisibility(View.GONE);
		}
	};

	/**
	 * Events
	 */

	@Subscribe
	public void showPriceChange(Events.CarsShowCheckoutAfterPriceChange event) {
		show(checkout, FLAG_CLEAR_TOP);
	}

	@Subscribe
	public void showSessionTimeout(Events.CarsSessionTimeout event) {
		clearBackStack();
		((AppCompatActivity) getContext()).onBackPressed();
	}

	@Subscribe
	public void showPaymentFailed(Events.CarsPaymentFailed event) {
		show(checkout, FLAG_CLEAR_TOP);
		checkout.slideWidget.resetSlider();
		checkout.paymentInfoCardView.getCardInfoContainer().performClick();
	}

	@Subscribe
	public void showInvalidInput(Events.CarsInvalidInput event) {
		if (event.field.equalsIgnoreCase("creditCardNumber")) {
			Events.post(new Events.CarsPaymentFailed());
		}
		else {
			show(checkout, FLAG_CLEAR_TOP);
			checkout.slideWidget.resetSlider();
			checkout.mainContactInfoCardView.setExpanded(true, true);
			checkout.mainContactInfoCardView.setInvalid(event.field);
		}
	}

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		show(checkout);
	}

	@Subscribe
	public void onShowCVV(Events.ShowCVV event) {
		show(cvv);
		BillingInfo billingInfo = event.billingInfo;
		cvv.bind(billingInfo);
		OmnitureTracking.trackAppCarCheckoutCvvScreen();
	}

	// Cached for checkout call failure retry purposes
	private CarCheckoutParamsBuilder checkoutParamsBuilder;

	@Subscribe
	public void onDoCheckoutCall(Events.CarsKickOffCheckoutCall event) {
		checkoutParamsBuilder = event.checkoutParamsBuilder;
		CarCheckoutParamsBuilder builder = event.checkoutParamsBuilder;
		if (builder.areRequiredParamsFilled()) {
			CreateTripCarOffer offer = Db.getTripBucket().getCar().mCarTripResponse.carProduct;
			checkoutSubscription = carServices.checkout(offer, builder.build(), checkoutObserver);
			checkoutDialog.show();
		}
		else {
			String msg = getResources().getString(R.string.error_missing_checkout_params);
			String btn = getResources().getString(R.string.ok);
			showAlertMessage(msg, btn);
		}
	}
}
