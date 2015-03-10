package com.expedia.bookings.presenter;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVEntryWidget;
import com.expedia.bookings.widget.CarCheckoutWidget;
import com.expedia.bookings.widget.CarConfirmationWidget;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarCheckoutPresenter extends Presenter {
	public CarCheckoutPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
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
		}

		@Override
		public void onError(Throwable e) {
			checkoutDialog.dismiss();
		}

		@Override
		public void onNext(CarCheckoutResponse carCheckoutResponse) {
			checkoutDialog.dismiss();
			Events.post(new Events.CarsShowConfirmation(carCheckoutResponse));
			show(confirmation, FLAG_CLEAR_BACKSTACK);
		}
	};

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
	public void onShowCVV(Events.CarsShowCVV event) {
		show(cvv);
		BillingInfo billingInfo = event.billingInfo;
		cvv.bind(billingInfo);
	}

	@Subscribe
	public void onDoCheckoutCall(Events.CarsKickOffCheckoutCall event) {
		CarCheckoutParamsBuilder builder = event.checkoutParamsBuilder;
		if (builder.areRequiredParamsFilled()) {
			checkoutSubscription = carServices.checkout(builder.build(), checkoutObserver);
			checkoutDialog.show();
		}
		else {
			String msg = getResources().getString(R.string.error_missing_checkout_params);
			String btn = getResources().getString(R.string.ok);
			showAlertMessage(msg, btn);
		}
	}
}
