package com.expedia.bookings.presenter;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.cars.CarApiError;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVEntryWidget;
import com.expedia.bookings.widget.CarCheckoutWidget;
import com.expedia.bookings.widget.CarConfirmationWidget;
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

	@InjectView(R.id.checkout)
	CarCheckoutWidget checkout;

	@InjectView(R.id.confirmation)
	CarConfirmationWidget confirmation;

	@InjectView(R.id.cvv)
	CVVEntryWidget cvv;

	private ProgressDialog checkoutDialog;
	private Subscription checkoutSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).carComponent().inject(this);

		addTransition(checkoutToCvv);
		addTransition(cvvToConfirmation);
		addTransition(checkoutToConfirmation);
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
			else {
				showDefaultErrorDialog();
			}
		}

		@Override
		public void onNext(CarCheckoutResponse response) {
			checkoutDialog.dismiss();

			if (response == null) {
				showDefaultErrorDialog();
			}
			else if (response.hasErrors()) {
				handleErrors(response);
			}
			else {
				showConfirmation(response);
			}
		}
	};

	private void handleErrors(CarCheckoutResponse response) {
		CarApiError error = response.getFirstError();
		switch (error.errorCode) {
		case PAYMENT_FAILED:
			showPaymentFailedDialog(response);
			break;
		case PRICE_CHANGE:
			showPriceChangeDialog(response);
			break;
		case SESSION_TIMEOUT:
			// TODO perform new create trip
			break;
		case TRIP_ALREADY_BOOKED:
			showTripAlreadyBooked(response);
			break;
		case UNKNOWN_ERROR:
		case OMS_ERROR:
		default:
			showDefaultErrorDialog();
			break;
		}
	}

	private void showDefaultErrorDialog() {
		showCheckoutErrorDialog(R.string.oops);
	}

	private void showPriceChangeDialog(final CarCheckoutResponse response) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(R.string.reservation_price_change))
			.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (cvv.getVisibility() == VISIBLE) {
						((ActionBarActivity) getContext()).onBackPressed();
					}
					Events.post(
						new Events.CarsShowCheckoutAfterPriceChange(response.originalCarProduct, response.newCarProduct,
							response.tripId));
				}
			})
			.show();
	}

	private void showPaymentFailedDialog(final CarCheckoutResponse response) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(R.string.payment_failed))
			.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (cvv.getVisibility() == VISIBLE) {
						((ActionBarActivity) getContext()).onBackPressed();
					}
					checkout.slideWidget.resetSlider();
					checkout.paymentInfoCardView.setExpanded(true, true);
				}
			})
			.show();
	}

	private void showTripAlreadyBooked(final CarCheckoutResponse response) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(R.string.reservation_already_exists))
			.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					showConfirmation(response);
				}
			})
			.show();
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
			.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					checkout.slideWidget.resetSlider();
				}
			})
			.show();
	}

	private void showConfirmation(CarCheckoutResponse response) {
		Events.post(new Events.CarsShowConfirmation(response));
		show(confirmation, FLAG_CLEAR_BACKSTACK);
	}

	private Transition cvvToConfirmation = new VisibilityTransition(this, CarCheckoutWidget.class.getName(), CVVEntryWidget.class.getName());
	private Transition checkoutToCvv = new VisibilityTransition(this, CVVEntryWidget.class.getName(), CarConfirmationWidget.class.getName());
	private Transition checkoutToConfirmation = new VisibilityTransition(this, CarCheckoutWidget.class.getName(), CarConfirmationWidget.class.getName());

	private DefaultTransition defaultCheckoutTransition = new DefaultTransition(CarCheckoutWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			checkout.setVisibility(View.VISIBLE);
			cvv.setVisibility(View.GONE);
			confirmation.setVisibility(View.GONE);
		}
	};

	/**
	 * Events
	 */

	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		show(checkout);
	}

	@Subscribe
	public void onShowCVV(Events.ShowCVV event) {
		show(cvv);
		BillingInfo billingInfo = event.billingInfo;
		cvv.bind(billingInfo);
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
