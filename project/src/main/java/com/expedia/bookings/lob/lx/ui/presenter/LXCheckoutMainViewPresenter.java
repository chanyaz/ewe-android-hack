package com.expedia.bookings.lob.lx.ui.presenter;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXBookableItem;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.trips.TripBucketItemLX;
import com.expedia.bookings.data.trips.TripBucketItemTransport;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.packages.LXTravelersPresenter;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingSuppressionUtils;
import com.expedia.bookings.utils.ApiDateUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVEntryWidget;
import com.expedia.bookings.widget.CheckoutBasePresenter;
import com.expedia.bookings.widget.LXCheckoutSummaryWidget;
import com.expedia.bookings.widget.PaymentWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class LXCheckoutMainViewPresenter extends CheckoutBasePresenter
	implements CVVEntryWidget.CVVEntryFragmentListener {

	private boolean isGroundTransport;

	public LXCheckoutMainViewPresenter(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Inject
	LXState lxState;

	LXCheckoutSummaryWidget summaryWidget;

	String tripId;

	@Inject
	LxServices lxServices;

	protected LineOfBusiness getLineOfBusiness() {
		return isGroundTransport ? LineOfBusiness.TRANSPORT : LineOfBusiness.LX;
	}

	@Override
	protected String getAccessibilityTextForPurchaseButton() {
		return getResources().getString(R.string.accessibility_reserve_button);
	}


	@Override
	protected Class getTravelersPresenter() {
		return LXTravelersPresenter.class;
	}


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		summaryWidget = Ui.inflate(R.layout.lx_checkout_summary_widget, summaryContainer, false);
		summaryContainer.addView(summaryWidget);
		mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.lx_enter_contact_details));
	}

	@Subscribe
	public void onUpdateCheckoutAfterPriceChange(Events.LXUpdateCheckoutSummaryAfterPriceChange event) {
		// We don't support multiple ticket booking as of now, passing only the first bookable item.
		bind(event.lxCheckoutResponse.tripId, event.lxCheckoutResponse.originalPrice,
			event.lxCheckoutResponse.newTotalPrice, event.lxCheckoutResponse.lxProduct.lxBookableItems.get(0));
		slideWidget.resetSlider();
	}

	/**
	 *
	 * @param originalPrice - In case there was a Price Change [during CreateTrip/Checkout], this is non-null
	 *                        and contains the original price. Otherwise it is null.
	 * @param newPrice - Always non-null. Contains the up-to-date price of the selected offer(s) to be displayed to the user
	 *                 and deducted during Payment.
	 */
	private void bind(String tripId, Money originalPrice, Money newPrice, LXBookableItem lxBookableItem) {
		this.tripId = tripId;
		summaryWidget.bind(originalPrice, newPrice, lxBookableItem);
		paymentInfoCardView.getViewmodel().isCreditCardRequired().onNext(true);
		clearCCNumber();
		scrollCheckoutToTop();
		slideWidget.resetSlider();

		sliderTotalText.setText(Phrase.from(getContext(), R.string.your_card_will_be_charged_template)
			.put("dueamount", Money.getFormattedMoneyFromAmountAndCurrencyCode(
				newPrice.getAmount(), newPrice.getCurrency())).format().toString());

		mainContactInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(INVISIBLE);
		paymentInfoCardView.show(new PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK);
		String e3EndpointUrl = Ui.getApplication(getContext()).appComponent().endpointProvider().getE3EndpointUrl();
		String rulesAndRestrictionsURL = LXDataUtils.getRulesRestrictionsUrl(e3EndpointUrl, tripId);
		legalInformationText.setText(StrUtils.generateLegalClickableLink(getContext(), rulesAndRestrictionsURL));
		checkoutFormWasUpdated();
		updateLoginWidget();
		selectFirstAvailableCardIfOnlyOneAvailable();
	}

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideProgress(float pixels, float total) {
	}

	@Override
	public void onSlideAllTheWay() {
		BillingInfo billingInfo = Db.getBillingInfo();
		Events.post(new Events.ShowCVV(billingInfo));
		slideWidget.resetSlider();
	}

	@Override
	public void onSlideAbort() {
	}

	@Override
	public void onBook(String cvv) {
		final boolean suppressFinalBooking = BookingSuppressionUtils
			.shouldSuppressFinalBooking(getContext(), R.string.preference_suppress_lx_bookings);
		String email = null;

		if (userStateManager.getUserSource().getUser() != null) {
			email = userStateManager.getUserSource().getUser().getPrimaryTraveler().getEmail();
		}

		LXCheckoutParams checkoutParams = new LXCheckoutParams()
			.firstName(mainContactInfoCardView.firstName.getText().toString())
			.lastName(mainContactInfoCardView.lastName.getText().toString())
			.email(userStateManager.isUserAuthenticated() ? email : mainContactInfoCardView.emailAddress.getText().toString())
			.expectedTotalFare(lxState.latestTotalPrice().getAmount().setScale(2).toString())
			.phoneCountryCode(
				Integer.toString(mainContactInfoCardView.phoneSpinner.getSelectedTelephoneCountryCode()))
			.phone(mainContactInfoCardView.phoneNumber.getText().toString())
			.expectedFareCurrencyCode(lxState.activity.price.currencyCode)
			.tripId(tripId)
			.suppressFinalBooking(suppressFinalBooking);

		if (Db.getBillingInfo().hasStoredCard()) {
			checkoutParams.storedCreditCardId(Db.getBillingInfo().getStoredCard().getId()).cvv(cvv);
		}
		else {
			BillingInfo info = Db.getBillingInfo();
			String expirationYear = JodaUtils.format(info.getExpirationDate(), "yyyy");
			String expirationMonth = JodaUtils.format(info.getExpirationDate(), "MM");

			checkoutParams.creditCardNumber(info.getNumber())
				.expirationDateYear(expirationYear)
				.expirationDateMonth(expirationMonth)
				.postalCode(info.getLocation().getPostalCode())
				.nameOnCard(info.getNameOnCard()).cvv(cvv)
				.storeCreditCardInUserProfile(info.getSaveCardToExpediaAccount());
		}
		Events.post(new Events.LXKickOffCheckoutCall(checkoutParams));
	}

	private Observer<LXCreateTripResponse> createTripObserver = new Observer<LXCreateTripResponse>() {
		@Override
		public void onComplete() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("LXCreateTrip - onError", e);
			showProgress(false);
			if (RetrofitUtils.isNetworkError(e)) {
				showOnCreateNoInternetErrorDialog(R.string.error_no_internet);
			}
			else if (e instanceof ApiError) {
				Events.post(new Events.LXError((ApiError) e));
			}
			else {
				Events.post(new Events.LXError(null));
			}
		}

		@Override
		public void onSubscribe(@NonNull Disposable d) {
			// ignore
		}

		@Override
		public void onNext(LXCreateTripResponse response) {
			if (getLineOfBusiness() == LineOfBusiness.TRANSPORT) {
				Db.getTripBucket().clearTransport();
				Db.getTripBucket().add(new TripBucketItemTransport(response));
			}
			else if (getLineOfBusiness() == LineOfBusiness.LX) {
				Db.getTripBucket().clearLX();
				Db.getTripBucket().add(new TripBucketItemLX(response));
			}

			showProgress(false);
			OmnitureTracking.trackAppLXCheckoutPayment(lxState.activity.id,
				ApiDateUtils
					.yyyyMMddHHmmssToLocalDate(lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate),
				lxState.selectedTicketsCount(), lxState.latestTotalPrice().getAmount().setScale(2).toString(),
				isGroundTransport);
			Money tripTotalPrice = response.hasPriceChange() ? response.newTotalPrice : lxState.latestTotalPrice();
			// We don't support multiple ticket booking as of now, passing only the first bookable item.
			bind(response.tripId, response.originalPrice, tripTotalPrice, response.lxProduct.lxBookableItems.get(0));
			AdTracker.trackLXCheckoutStarted(lxState.activity.destination, tripTotalPrice,
				lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate, lxState.activity.categories,
				lxState.selectedTicketsCount(), lxState.activity.title, lxState.activity.regionId, lxState.activity.id,
				lxState.searchParams.getActivityStartDate(), lxState.selectedChildTicketsCount());
			show(new Ready(), FLAG_CLEAR_BACKSTACK);
			Events.post(new Events.LXCreateTripSucceeded(response, lxState.activity));
		}
	};

	private Disposable createTripSubscription;

	private void cleanup() {
		if (createTripSubscription != null) {
			createTripSubscription.dispose();
			createTripSubscription = null;
		}
	}

	@Override
	public void doCreateTrip() {
		cleanup();
		createTripSubscription = lxServices.createTrip(lxState.createTripParams(getContext()), lxState.originalTotalPrice(), createTripObserver);
	}

	@Override
	public void showProgress(boolean show) {
		summaryWidget.setVisibility(show ? INVISIBLE : VISIBLE);
		mSummaryProgressLayout.setVisibility(show ? VISIBLE : GONE);
	}

	private void showOnCreateNoInternetErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					doCreateTrip();
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXActivitySelectedRetry());
				}
			})
			.show();
	}

	@Subscribe
	public void onLogin(Events.LoggedInSuccessful event) {
		onLoginSuccessful();
	}

	public void setIsFromGroundTransport(boolean isGroundTransport) {
		this.isGroundTransport = isGroundTransport;
	}
}
