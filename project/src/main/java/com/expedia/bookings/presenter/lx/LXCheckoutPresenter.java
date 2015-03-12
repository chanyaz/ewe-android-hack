package com.expedia.bookings.presenter.lx;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.VisibilityTransition;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVEntryWidget;
import com.expedia.bookings.widget.LXCheckoutWidget;
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

	@InjectView(R.id.cvv)
	CVVEntryWidget cvv;

	private ProgressDialog checkoutDialog;
	private Subscription checkoutSubscription;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		addDefaultTransition(defaultCheckoutTransition);
		addTransition(checkoutToCvv);

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
			checkoutDialog.dismiss();
		}

		@Override
		public void onNext(LXCheckoutResponse lxCheckoutResponse) {
			checkoutDialog.dismiss();
			Events.post(new Events.LXCheckoutSucceeded(lxCheckoutResponse));
			//TODO - display confirmation
		}
	};

	private DefaultTransition defaultCheckoutTransition = new DefaultTransition(LXCheckoutWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			checkout.setVisibility(View.VISIBLE);
			cvv.setVisibility(View.GONE);
		}
	};
	private Transition checkoutToCvv = new VisibilityTransition(this, CVVEntryWidget.class.getName(), LXCheckoutWidget.class.getName());

	/**
	 * Events
	 */

	@Subscribe
	public void onShowCheckout(Events.LXCreateTripSucceeded event) {
		show(checkout);
	}

	@Subscribe
	public void onShowCVV(Events.ShowCVV event) {
		show(cvv);
		BillingInfo billingInfo = event.billingInfo;
		cvv.bind(billingInfo);
	}

	@Subscribe
	public void onDoCheckoutCall(Events.LXKickOffCheckoutCall event) {
		if (event.checkoutParams.areRequiredParamsFilled()) {
			checkoutSubscription = lxServices.lxCheckout(event.checkoutParams, checkoutObserver);
			checkoutDialog.show();
		}
		else {
			String msg = getResources().getString(R.string.error_missing_checkout_params);
			String btn = getResources().getString(R.string.ok);
			showAlertMessage(msg, btn);
		}
	}
}
