package com.expedia.bookings.widget;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.util.AttributeSet;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.trips.TripBucketItemCar;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingSuppressionUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscription;

public class CarCheckoutMainViewPresenter extends CheckoutBasePresenter implements CVVEntryWidget.CVVEntryFragmentListener {

	public CarCheckoutMainViewPresenter(Context context, AttributeSet attr) {
		super(context, attr);
	}

	CreateTripCarOffer carProduct;
	String tripId;

	CarCheckoutSummaryWidget summaryWidget;

	private Subscription createTripSubscription;
	private Events.CarsShowCheckout createTripParams;

	@Inject
	CarServices carServices;

	protected LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.CARS;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).carComponent().inject(this);
		summaryWidget = Ui.inflate(R.layout.car_checkout_summary_widget, summaryContainer, false);
		summaryContainer.addView(summaryWidget);
		mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.enter_driver_details));
		mainContactInfoCardView.setEnterDetailsContentDescription(getResources().getString(R.string.enter_driver_details_cont_desc));
	}

	// Create Trip network handling

	private Observer<CarCreateTripResponse> createTripObserver = new Observer<CarCreateTripResponse>() {
		@Override
		public void onCompleted() {

		}

		@Override
		public void onError(Throwable e) {
			Log.e("CarCreateTrip - onError", e);
			showProgress(false);

			if (RetrofitUtils.isNetworkError(e)) {
				showOnCreateNoInternetErrorDialog(R.string.error_no_internet);
			}
			else {
				handleCreateTripError((ApiError) e);
			}
		}

		@Override
		public void onNext(CarCreateTripResponse createTripResponse) {
			Events.post(new Events.CarsCheckoutCreateTripSuccess(createTripResponse));
			Db.getTripBucket().add(new TripBucketItemCar(createTripResponse));
			showProgress(false);
			String ogPriceForPriceChange = createTripResponse.originalPrice == null ?
				"" : createTripResponse.originalPrice;
			bind(createTripResponse.carProduct, ogPriceForPriceChange, createTripResponse.tripId);
			OmnitureTracking.trackAppCarCheckoutPage(createTripResponse.carProduct);
			AdTracker.trackCarCheckoutStarted(createTripResponse.carProduct);
			show(new Ready(), FLAG_CLEAR_BACKSTACK);
		}
	};

	private void handleCreateTripError(final ApiError error) {
		switch (error.errorCode) {
		case INVALID_CAR_PRODUCT_KEY:
			showInvalidProductErrorDialog();
			break;
		default:
			showGenericCreateTripErrorDialog();
			break;
		}
	}

	private void showGenericCreateTripErrorDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(
				Phrase.from(getContext(), R.string.error_server_TEMPLATE).put("brand", BuildConfig.brand).format()
					.toString())
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	private void showInvalidProductErrorDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(R.string.error_cars_product_expired))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
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
					show(new CheckoutFailed(), FLAG_CLEAR_BACKSTACK);
				}
			})
			.show();
	}


	@Subscribe
	public void onShowCheckout(Events.CarsShowCheckout event) {
		createTripParams = event;
		cleanup();
		showCheckout();
	}

	@Subscribe
	public void onSignOut(Events.SignOut event) {
		accountLogoutClicked();
	}

	@Subscribe
	public void onShowCheckoutAfterPriceChange(Events.CarsUpdateCheckoutSummaryAfterPriceChange event) {
		bind(event.newCreateTripOffer, /* createTripOffer */
			event.originalCreateTripOffer.detailedFare.grandTotal.formattedPrice, /* originalPriceString */
			event.tripId /* tripId */);
		slideWidget.resetSlider();
	}

	private void bind(CreateTripCarOffer createTripOffer, String originalOfferFormattedPrice, String tripId) {
		this.tripId = tripId;
		this.carProduct = createTripOffer;
		summaryWidget.bind(carProduct, originalOfferFormattedPrice);
		paymentInfoCardView.getViewmodel().isCreditCardRequired().onNext(carProduct.checkoutRequiresCard);
		clearCCNumber();
		scrollCheckoutToTop();
		slideWidget.resetSlider();

		acceptTermsWidget.getVm().resetAcceptedTerms();

		int sliderMessage = carProduct.checkoutRequiresCard ? R.string.your_card_will_be_charged_template
			: R.string.amount_due_today_template;
		sliderTotalText.setText(Phrase.from(getContext(), sliderMessage)
			.put("dueamount",
				Money.getFormattedMoneyFromAmountAndCurrencyCode(carProduct.detailedFare.totalDueToday.getAmount(),
					carProduct.detailedFare.totalDueToday.getCurrency())).format().toString());
		mainContactInfoCardView.setExpanded(false);
		slideToContainer.setVisibility(INVISIBLE);
		paymentInfoCardView.show(new PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK);
		legalInformationText.setText(
			StrUtils.generateLegalClickableLink(getContext(), carProduct.rulesAndRestrictionsURL));
		updateLoginWidget();
		selectFirstAvailableCardIfOnlyOneAvailable();
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
		if (carProduct.checkoutRequiresCard) {
			BillingInfo billingInfo = Db.getBillingInfo();
			Events.post(new Events.ShowCVV(billingInfo));
			slideWidget.resetSlider();
		}
		else {
			onBook(null);
		}

	}

	@Override
	public void onSlideAbort() {
	}

	@Override
	public void onBook(String cvv) {
		final boolean suppressFinalBooking = BookingSuppressionUtils
			.shouldSuppressFinalBooking(getContext(), R.string.preference_suppress_car_bookings);
		CarCheckoutParamsBuilder builder =
			new CarCheckoutParamsBuilder()
				.firstName(mainContactInfoCardView.firstName.getText().toString())
				.lastName(mainContactInfoCardView.lastName.getText().toString())
				.emailAddress(User.isLoggedIn(getContext()) ? Db.getUser().getPrimaryTraveler().getEmail()
					: mainContactInfoCardView.emailAddress.getText().toString())
				.grandTotal(carProduct.detailedFare.grandTotal)
				.phoneCountryCode(
					Integer.toString(mainContactInfoCardView.phoneSpinner.getSelectedTelephoneCountryCode()))
				.phoneNumber(mainContactInfoCardView.phoneNumber.getText().toString())
				.tripId(tripId)
				.guid(Db.getAbacusGuid())
				.suppressFinalBooking(suppressFinalBooking);

		if (carProduct.checkoutRequiresCard && Db.getBillingInfo().hasStoredCard()) {
			builder.storedCCID(Db.getBillingInfo().getStoredCard().getId()).cvv(cvv);
		}
		else if (carProduct.checkoutRequiresCard) {
			BillingInfo info = Db.getBillingInfo();
			String expirationYear = JodaUtils.format(info.getExpirationDate(), "yyyy");
			String expirationMonth = JodaUtils.format(info.getExpirationDate(), "MM");

			builder.ccNumber(info.getNumber()).expirationYear(expirationYear)
				.expirationMonth(expirationMonth).ccPostalCode(info.getLocation().getPostalCode())
				.storeCreditCardInUserProfile(info.getSaveCardToExpediaAccount())
				.ccName(info.getNameOnCard()).cvv(cvv);
		}
		Events.post(new Events.CarsKickOffCheckoutCall(builder));
	}

	private void cleanup() {
		if (createTripSubscription != null) {
			createTripSubscription.unsubscribe();
			createTripSubscription = null;
		}
	}

	@Override
	public void doCreateTrip() {
		cleanup();
		if (createTripParams != null && createTripParams.productKey != null) {
			createTripSubscription = carServices
				.createTrip(createTripParams.productKey, createTripParams.fare, createTripParams.isInsuranceIncluded,
					createTripObserver);
		}
		else {
			handleCreateTripError(new ApiError(ApiError.Code.INVALID_CAR_PRODUCT_KEY));
			OmnitureTracking.trackCarCheckoutError();
		}
	}

	@Override
	public void showProgress(boolean show) {
		summaryWidget.setVisibility(show ? INVISIBLE : VISIBLE);
		mSummaryProgressLayout.setVisibility(show ? VISIBLE : GONE);
	}

	@Subscribe
	public void onLogin(Events.LoggedInSuccessful event) {
		onLoginSuccessful();
	}
}

