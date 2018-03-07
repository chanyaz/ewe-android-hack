package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.extensions.LobExtensionsKt;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.enums.TravelerCheckoutStatus;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.presenter.packages.AbstractTravelersPresenter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.FeatureUtilKt;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.traveler.TravelerSummaryCard;
import com.expedia.vm.CheckoutToolbarViewModel;
import com.expedia.vm.PaymentViewModel;
import com.expedia.vm.traveler.HotelTravelerSummaryViewModel;
import com.expedia.vm.traveler.HotelTravelersViewModel;
import com.mobiata.android.Log;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import kotlin.Unit;

public abstract class CheckoutBasePresenter extends Presenter implements SlideToWidgetLL.ISlideToListener,
	UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener,
	ExpandableCardView.IExpandedListener {

	protected abstract LineOfBusiness getLineOfBusiness();
	protected abstract String getAccessibilityTextForPurchaseButton();
	protected abstract Class getTravelersPresenter();

	public CheckoutBasePresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.widget_checkout_base, this);
	}

	@InjectView(R.id.checkout_scroll)
	public ScrollView scrollView;

	@InjectView(R.id.scroll_content)
	public LinearLayout checkoutContent;

	@InjectView(R.id.checkout_toolbar)
	public CheckoutToolbar toolbar;

	@InjectView(R.id.main_contact_info_card_view)
	public TravelerContactDetailsWidget mainContactInfoCardView;

	@InjectView(R.id.traveler_default_state_card_view)
	public CardView travelerSummaryCardView;

	@InjectView(R.id.traveler_default_state)
	public TravelerSummaryCard travelerSummaryCard;

	public AbstractTravelersPresenter travelersPresenter;

	@InjectView(R.id.traveler_presenter_stub)
	public ViewStub travelerViewStub;

	public ViewStub paymentStub;

	public PaymentWidget paymentInfoCardView;

	@InjectView(R.id.slide_to_purchase_layout)
	public ViewGroup slideToContainer;

	@InjectView(R.id.summary_container)
	public FrameLayout summaryContainer;

	@InjectView(R.id.coupon_container)
	public FrameLayout couponContainer;

	public View mSummaryProgressLayout;

	@InjectView(R.id.login_widget)
	public AccountButton loginWidget;

	@InjectView(R.id.hint_container)
	ViewGroup hintContainer;

	@InjectView(R.id.legal_information_text_view)
	public TextView legalInformationText;

	@InjectView(R.id.disclaimer_text)
	public TextView disclaimerText;

	@InjectView(R.id.deposit_policy_text)
	public TextView depositPolicyText;

	@InjectView(R.id.slide_to_purchase_widget)
	public SlideToWidgetLL slideWidget;

	@InjectView(R.id.purchase_total_text_view)
	public TextView sliderTotalText;

	@InjectView(R.id.spacer)
	public Space space;

	public MenuItem menuDone;

	ExpandableCardView lastExpandedCard;
	ExpandableCardView currentExpandedCard;

	protected UserAccountRefresher userAccountRefresher;
	protected UserStateManager userStateManager;

	private boolean listenToScroll = true;
	private boolean hotelMaterialFormTestEnabled = LobExtensionsKt.isMaterialHotelEnabled(getLineOfBusiness(), getContext());

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		userStateManager = Ui.getApplication(getContext()).appComponent().userStateManager();
		setupToolbar();

		paymentStub = (ViewStub) findViewById(R.id.payment_info_card_view_stub);
		if (hotelMaterialFormTestEnabled) {
			paymentStub.setLayoutResource(R.layout.material_payment_widget_v2);
		}
		paymentInfoCardView = (PaymentWidget) paymentStub.inflate();
		addTransition(defaultToExpanded);
		addTransition(defaultToReady);
		addTransition(defaultToCheckoutFailed);
		addTransition(defaultToPayment);
		addTransition(defaultToPaymentV2);
		addDefaultTransition(defaultTransition);
		if (hotelMaterialFormTestEnabled) {
			addTransition(defaultToTravelerPresenter);
			setupMaterialTravelerWidget();
		}
		paymentInfoCardView.setViewmodel(new PaymentViewModel(getContext()));
		paymentInfoCardView.getViewmodel().getLineOfBusiness().onNext(getLineOfBusiness());
		paymentInfoCardView.getViewmodel().getExpandObserver().subscribe(expandPaymentObserver);
		paymentInfoCardView.getFilledIn().subscribe(toolbar.getViewModel().getShowDone());
		paymentInfoCardView.getToolbarTitle().subscribe(toolbar.getViewModel().getToolbarTitle());
		paymentInfoCardView.getToolbarNavIcon().subscribe(toolbar.getViewModel().getToolbarNavIcon());
		paymentInfoCardView.getFocusedView().subscribe(toolbar.getViewModel().getCurrentFocus());
		paymentInfoCardView.getViewmodel().getMenuVisibility().subscribe(toolbar.getViewModel().getMenuVisibility());
		paymentInfoCardView.getViewmodel().getEnableMenuItem().subscribe(toolbar.getViewModel().getEnableMenuItem());
		paymentInfoCardView.getVisibleMenuWithTitleDone().subscribe(toolbar.getViewModel().getVisibleMenuWithTitleDone());
		paymentInfoCardView.getViewmodel().getDoneClickedMethod().subscribe(toolbar.getViewModel().getDoneClickedMethod());

		if (hotelMaterialFormTestEnabled) {
			paymentInfoCardView.getViewmodel().getMenuVisibility().subscribe(new DisposableObserver<Boolean>() {
				@Override
				public void onComplete() {

				}

				@Override
				public void onError(Throwable e) {

				}

				@Override
				public void onNext(Boolean isShowing) {
					updateMaterialBackgroundColor(isShowing);
				}
			});
		}

		mainContactInfoCardView.filledIn.subscribe(toolbar.getViewModel().getShowDone());
		mainContactInfoCardView.onDoneClickedMethod.subscribe(toolbar.getViewModel().getDoneClickedMethod());
		paymentInfoCardView.getViewmodel().getToolbarNavIconFocusObservable().subscribe(new DisposableObserver<Unit>() {
			@Override
			public void onComplete() {

			}
			@Override
			public void onError(Throwable e) {

			}
			@Override
			public void onNext(Unit unit) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
			}
		});
		slideWidget.addSlideToListener(this);

		loginWidget.setListener(this);
		mainContactInfoCardView.setLineOfBusiness(getLineOfBusiness());

		mainContactInfoCardView.addExpandedListener(this);
		mainContactInfoCardView.setToolbarListener(toolbar);
		hintContainer.setVisibility(userStateManager.isUserAuthenticated() ? GONE : VISIBLE);
		legalInformationText.setMovementMethod(LinkMovementMethod.getInstance());
		slideToContainer.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Consume touches so they don't pass behind
				return !AccessibilityUtil.isTalkBackEnabled(getContext());
			}
		});

		if (ExpediaBookingApp.isAutomation()) {
			//Espresso hates progress bars
			mSummaryProgressLayout = new View(getContext(), null);

		}
		else {
			mSummaryProgressLayout = new ProgressBar(getContext(), null);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.CENTER;
			mSummaryProgressLayout.setLayoutParams(lp);
			((ProgressBar) mSummaryProgressLayout).setIndeterminate(true);
		}
		summaryContainer.addView(mSummaryProgressLayout);
		userAccountRefresher = new UserAccountRefresher(getContext(), getLineOfBusiness(), this);

		if (getLineOfBusiness() == LineOfBusiness.HOTELS) {
			scrollView.addOnScrollListener(checkoutScrollListener);
		}

		slideWidget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AccessibilityUtil.isTalkBackEnabled(getContext())) {
					slideWidget.fireSlideAllTheWay();
				}
			}
		});
	}

	public void updateMaterialBackgroundColor(Boolean isMaterialFormShowing) {
		int color = isMaterialFormShowing ? R.color.material_checkout_background_color : R.color.checkout_overview_background_color;
		scrollView.setBackgroundColor(ContextCompat.getColor(getContext(), color));
	}

	protected void setupMaterialTravelerWidget() {
		travelersPresenter = (AbstractTravelersPresenter) travelerViewStub.inflate();
		travelersPresenter.setViewModel(new HotelTravelersViewModel(getContext(), getLineOfBusiness(), true));
		travelerSummaryCard.setViewModel(new HotelTravelerSummaryViewModel(getContext()));
		travelersPresenter.getViewModel().getTravelersCompletenessStatus().subscribe(travelerSummaryCard.getViewModel().getTravelerStatusObserver());
		travelersPresenter.getTravelerEntryWidget().getTravelerButton().setLOB(getLineOfBusiness());
		travelerSummaryCardView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				openTravelerPresenter();
			}
		});
		travelersPresenter.getToolbarTitleSubject().subscribe(toolbar.getViewModel().getToolbarTitle());
		travelersPresenter.getToolbarNavIconContDescSubject().subscribe(toolbar.getViewModel().getToolbarNavIconContentDesc());
		travelersPresenter.getViewModel().getDoneClickedMethod().subscribe(toolbar.getViewModel().getDoneClickedMethod());
		travelersPresenter.getCloseSubject().subscribe(new DisposableObserver<Unit>() {
			@Override
			public void onComplete() {

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onNext(Unit unit) {
				show(new Ready(), FLAG_CLEAR_BACKSTACK);
			}
		});
	}

	protected String getToolbarTitle() {
		return getContext().getString(R.string.checkout_text);
	}

	public void setupToolbar() {
		toolbar.setViewModel(new CheckoutToolbarViewModel(getContext()));
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc);
		toolbar.setTitle(getToolbarTitle());
		menuDone = toolbar.getMenu().findItem(R.id.menu_done);
		menuDone.setVisible(false);
		toolbar.getViewModel().getNextClicked().subscribe(new Observer<Unit>() {
			@Override
			public void onComplete() {

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onSubscribe(@NonNull Disposable d) {

			}

			@Override
			public void onNext(Unit unit) {
				if (getLineOfBusiness() == LineOfBusiness.HOTELS && listenToScroll) {
					scrollToEnterDetails();
				}
			}
		});

		toolbar.getViewModel().getExpanded().subscribe(new Observer<ExpandableCardView>() {
			@Override
			public void onComplete() {
			}

			@Override
			public void onError(Throwable e) {
			}

			@Override
			public void onSubscribe(@NonNull Disposable d) {

			}

			@Override
			public void onNext(ExpandableCardView cardView) {
				lastExpandedCard = currentExpandedCard;
				currentExpandedCard = cardView;
				show(new WidgetExpanded());
			}
		});


		toolbar.getViewModel().getClosed().subscribe(new Observer<Unit>() {
			@Override
			public void onComplete() {

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onSubscribe(@NonNull Disposable d) {

			}

			@Override
			public void onNext(Unit unit) {
				Ui.hideKeyboard(CheckoutBasePresenter.this);
				back();
			}
		});


		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = Ui.obtainThemeColor(getContext(), R.attr.primary_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, scrollView, color));
		}
	}

	public void resetMenuButton() {
		if (getLineOfBusiness() == LineOfBusiness.HOTELS) {
			menuDone.setVisible(true);
			toolbar.getViewModel().getMenuTitle().onNext(getContext().getString(R.string.next));
		}
		else {
			menuDone.setVisible(false);
		}
	}

	private void scrollToEnterDetails() {
		getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				scrollToLogin();
				return false;
			}
		});
	}

	private void scrollToLogin() {
		Ui.hideKeyboard(CheckoutBasePresenter.this);
		updateSpacerHeight();
		final int targetScrollY = loginWidget.getTop() - (int) getResources().getDimension(R.dimen.checkout_login_padding_top);
		final ValueAnimator scrollAnimation =
			ValueAnimator.ofInt(scrollView.getScrollY(), targetScrollY);
		scrollAnimation.setDuration(300);
		scrollAnimation.setInterpolator(new FastOutSlowInInterpolator());
		scrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int scrollTo = (Integer) animation.getAnimatedValue();
				scrollView.scrollTo(0, scrollTo);
			}
		});
		scrollAnimation.addListener(new ValueAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
				menuDone.setVisible(false);
				listenToScroll = false;
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				scrollView.scrollTo(0, targetScrollY);
				listenToScroll = true;
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});

		scrollAnimation.start();
	}

	com.expedia.bookings.widget.ScrollView.OnScrollListener checkoutScrollListener = new ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			if (listenToScroll) {
				View lastChildView = scrollView.getChildAt(scrollView.getChildCount() - 1);
				int diff = (lastChildView.getBottom()) - (scrollView.getHeight() + scrollView.getScrollY());

				// if diff is zero, then the bottom has been reached
				if (diff == 0) {
					menuDone.setVisible(false);
					return;
				}

				int top = loginWidget.getTop() - (int) getResources().getDimension(R.dimen.checkout_login_padding_top);
				if (y >= top) {
					menuDone.setVisible(false);
				}
				else {
					menuDone.setVisible(!isCheckoutFormComplete());
				}
			}
		}
	};

	public void animateInSlideToPurchase(boolean visible) {
		// If its already in position, don't do it again
		if (slideToContainer.getVisibility() == (visible ? VISIBLE : INVISIBLE)) {
			return;
		}

		if (AccessibilityUtil.isTalkBackEnabled(getContext()) && visible) {
			//hide the slider for talkback users and show a purchase button
			slideWidget.hideTouchTarget();
			AccessibilityUtil.appendRoleContDesc(slideWidget, getAccessibilityTextForPurchaseButton(), R.string.accessibility_cont_desc_role_button);
		}

		// animate the container in
		slideToContainer.setTranslationY(visible ? slideToContainer.getHeight() : 0);
		slideToContainer.setVisibility(VISIBLE);
		ObjectAnimator animator = ObjectAnimator
			.ofFloat(slideToContainer, "translationY", visible ? 0 : slideToContainer.getHeight());
		animator.setDuration(300);
		animator.start();

		if (visible) {
			scrollToEnterDetails();
			String cardType = paymentInfoCardView.getCardType().getOmnitureTrackingCode();
			switch (getLineOfBusiness()) {
			//Hotel Tracking is inside HotelCheckoutMainViewPresenter as we have to handle ETP,pwp etc.
			case LX:
			case TRANSPORT:
				OmnitureTracking.trackAppLXCheckoutSlideToPurchase(getLineOfBusiness(), cardType);
				break;
			}
		}
	}

	public void checkoutFormWasUpdated() {
		if (isCheckoutFormComplete()) {
			animateInSlideToPurchase(true);
		}
		else {
			animateInSlideToPurchase(false);
		}
	}

	public static class CheckoutDefault {
	}

	public static class Ready {
	}

	public static class WidgetExpanded {
	}

	public static class CheckoutFailed {
	}

	private DefaultTransition defaultTransition = new DefaultTransition(CheckoutDefault.class.getName()) {
		@Override
		public void endTransition(boolean forward) {
			showProgress(true);
			menuDone.setVisible(false);
			for (int i = 0; i < checkoutContent.getChildCount(); i++) {
				View v = checkoutContent.getChildAt(i);
				if (v instanceof ExpandableCardView) {
					((ExpandableCardView) v).setExpanded(false, false);
				}
				v.setVisibility(GONE);
			}
			summaryContainer.setVisibility(VISIBLE);
			slideToContainer.setVisibility(INVISIBLE);

			updateSpacerHeight();
		}
	};

	private Transition defaultToReady = new Transition(CheckoutDefault.class, Ready.class) {
		@Override
		public void startTransition(boolean forward) {
			super.startTransition(forward);
			for (int i = 0; i < checkoutContent.getChildCount(); i++) {
				View v = checkoutContent.getChildAt(i);
				if (v == hintContainer) {
					hintContainer.setVisibility(forward ? userStateManager.isUserAuthenticated() ? GONE : VISIBLE : INVISIBLE);
				}
				else if (v == paymentInfoCardView) {
					if (paymentInfoCardView.isCreditCardRequired()) {
						paymentInfoCardView
							.setVisibility(forward ? VISIBLE : INVISIBLE);
					}
				}
				else if (v == travelerViewStub) {
					if (hotelMaterialFormTestEnabled) {
						travelerViewStub.setVisibility(forward ? VISIBLE : GONE);
					}
				}
				else {
					v.setVisibility(forward ? VISIBLE : INVISIBLE);
				}
			}
			summaryContainer.setVisibility(VISIBLE);
			if (hotelMaterialFormTestEnabled) {
				mainContactInfoCardView.setVisibility(GONE);
				travelerSummaryCard.setVisibility(forward ? VISIBLE : GONE);
				travelersPresenter.setVisibility(GONE);
				travelersPresenter.getViewModel().refresh();
			}
			else {
				travelerSummaryCardView.setVisibility(GONE);
			}
		}

		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			showProgress(!forward);
			updateSpacerHeight();
			if (forward) {
				resetMenuButton();
				checkoutFormWasUpdated();
			}
			else {
				animateInSlideToPurchase(false);
			}
			listenToScroll = true;
			AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
		}
	};

	public Transition defaultToTravelerPresenter = new Presenter.Transition(Ready.class, getTravelersPresenter()) {
		@Override
		public void startTransition(boolean forward) {
			super.startTransition(forward);
			summaryContainer.setVisibility(forward ? View.GONE : View.VISIBLE);
			loginWidget.setVisibility(forward ? View.GONE : View.VISIBLE);
			hintContainer.setVisibility(forward ? View.GONE : userStateManager.isUserAuthenticated() ? GONE : VISIBLE);
			travelerSummaryCardView.setVisibility(forward ? GONE : VISIBLE);
			mainContactInfoCardView.setVisibility(GONE);
			couponContainer.setVisibility(forward ? View.GONE : View.VISIBLE);
			legalInformationText.setVisibility(forward ? View.GONE : View.VISIBLE);
			disclaimerText.setVisibility(forward ? View.GONE : View.VISIBLE);
			depositPolicyText.setVisibility(forward ? View.GONE : View.VISIBLE);
			space.setVisibility(forward ? View.GONE : View.VISIBLE);
			paymentInfoCardView.setVisibility(forward ? View.GONE : View.VISIBLE);
			travelersPresenter.setVisibility(forward ? VISIBLE : GONE);
			listenToScroll = !forward;
			updateMaterialBackgroundColor(forward);
			if (forward) {
				toolbar.getViewModel().getVisibleMenuWithTitleDone().onNext(Unit.INSTANCE);
			}
			else {
				Ui.hideKeyboard(travelersPresenter);
				AccessibilityUtil.setFocusForView(travelersPresenter);
				AccessibilityUtil.setFocusForView(travelerSummaryCard);
				travelersPresenter.getToolbarNavIconContDescSubject().onNext(getResources().getString(R.string.toolbar_nav_icon_cont_desc));
				travelersPresenter.getViewModel().updateCompletionStatus();
				resetMenuButton();
				toolbar.setTitle(getToolbarTitle());
			}
		}

		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			updateCheckoutOverviewUiTransition(forward);
		}
	};

	private void updateSpacerHeight() {
		if (getLineOfBusiness() != LineOfBusiness.HOTELS || isCheckoutFormComplete()) {
			float scrollViewActualHeight = scrollView.getHeight() - scrollView.getPaddingTop();
			int bottom = (disclaimerText.getVisibility() == View.VISIBLE) ? disclaimerText.getBottom()
				: legalInformationText.getBottom();
			if (scrollViewActualHeight - bottom < slideToContainer.getHeight()) {
				ViewGroup.LayoutParams params = space.getLayoutParams();
				params.height = slideToContainer.getVisibility() == VISIBLE ? slideToContainer.getHeight() : 0;

				if (slideToContainer.getVisibility() == VISIBLE) {
					params.height = slideToContainer.getHeight();
				}
				else {
					params.height = 0;
				}
				space.setLayoutParams(params);
			}
		}
		else {
			// if not complete, provide enough space for sign in button to be anchored at top of viewable area
			int remainingHeight =
				scrollView.getChildAt(0).getHeight() - space.getHeight() - summaryContainer.getHeight();
			ViewGroup.LayoutParams params = space.getLayoutParams();
			params.height = scrollView.getHeight() - remainingHeight - Ui.getToolbarSize(getContext());
			space.setLayoutParams(params);
		}
	}

	private Transition defaultToCheckoutFailed = new Transition(CheckoutDefault.class, CheckoutFailed.class) {
		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			showProgress(false);
		}
	};

	private Presenter.Transition defaultToExpanded = new Presenter.Transition(Ready.class,
		WidgetExpanded.class) {

		@Override
		public void startTransition(boolean forward) {
			summaryContainer.setVisibility(forward ? View.GONE : View.VISIBLE);
			loginWidget.setVisibility(forward ? View.GONE : View.VISIBLE);
			hintContainer.setVisibility(forward ? View.GONE : userStateManager.isUserAuthenticated() ? GONE : VISIBLE);
			paymentInfoCardView
				.setVisibility(forward ? GONE : paymentInfoCardView.isCreditCardRequired() ? VISIBLE : GONE);
			if (hotelMaterialFormTestEnabled) {
				travelerSummaryCardView.setVisibility(forward ? GONE : VISIBLE);
			}
			else {
 				mainContactInfoCardView.setVisibility(!forward ? View.VISIBLE : currentExpandedCard instanceof TravelerContactDetailsWidget ? VISIBLE : GONE);
			}
			couponContainer
				.setVisibility(!forward ? View.VISIBLE : currentExpandedCard instanceof AbstractCouponWidget ? VISIBLE : GONE);
			legalInformationText.setVisibility(forward ? View.GONE : View.VISIBLE);
			disclaimerText.setVisibility(forward ? View.GONE : View.VISIBLE);
			depositPolicyText.setVisibility(forward ? View.GONE : View.VISIBLE);
			space.setVisibility(forward ? View.GONE : View.VISIBLE);
			if (forward) {
				if (lastExpandedCard != null && lastExpandedCard != currentExpandedCard) {
					lastExpandedCard.setExpanded(false, false);
				}
			}
			else {
				if (currentExpandedCard != null) {
					currentExpandedCard.setExpanded(false, false);
				}
				Ui.hideKeyboard(CheckoutBasePresenter.this);
				resetMenuButton();
			}
			listenToScroll = !forward;
			toolbar.setTitle(forward ? currentExpandedCard.getActionBarTitle() : getToolbarTitle());
			if (forward) {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
			}
		}

		@Override
		public void endTransition(boolean forward) {
			updateCheckoutOverviewUiTransition(forward);
		}
	};

	private Presenter.Transition defaultToPayment = new Presenter.Transition(Ready.class,
		PaymentWidget.class) {

		@Override
		public void startTransition(boolean forward) {
			summaryContainer.setVisibility(forward ? View.GONE : View.VISIBLE);
			loginWidget.setVisibility(forward ? View.GONE : View.VISIBLE);
			hintContainer.setVisibility(forward ? View.GONE : userStateManager.isUserAuthenticated() ? GONE : VISIBLE);
			couponContainer.setVisibility(forward ? View.GONE : View.VISIBLE);
			legalInformationText.setVisibility(forward ? View.GONE : View.VISIBLE);
			disclaimerText.setVisibility(forward ? View.GONE : View.VISIBLE);
			depositPolicyText.setVisibility(forward ? View.GONE : View.VISIBLE);
			space.setVisibility(forward ? View.GONE : View.VISIBLE);
			listenToScroll = !forward;
			if (hotelMaterialFormTestEnabled) {
				travelerSummaryCardView.setVisibility(forward ? GONE : VISIBLE);
			}
			else {
				mainContactInfoCardView.setVisibility(forward ? GONE : VISIBLE);
			}
			if (!forward) {
				paymentInfoCardView.show(new PaymentWidget.PaymentDefault(), FLAG_CLEAR_BACKSTACK);
				paymentInfoCardView.setVisibility(paymentInfoCardView.isCreditCardRequired() ? VISIBLE : GONE);
				Ui.hideKeyboard(CheckoutBasePresenter.this);
				resetMenuButton();
			}
			else {
				AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar);
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			super.updateTransition(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
			updateCheckoutOverviewUiTransition(forward);
		}
	};

	private Presenter.Transition defaultToPaymentV2 = new Presenter.Transition(Ready.class,
		PaymentWidgetV2.class) {

		@Override
		public void startTransition(boolean forward) {
			summaryContainer.setVisibility(forward ? View.GONE : View.VISIBLE);
			loginWidget.setVisibility(forward ? View.GONE : View.VISIBLE);
			hintContainer.setVisibility(forward ? View.GONE : userStateManager.isUserAuthenticated() ? GONE : VISIBLE);
			couponContainer.setVisibility(forward ? View.GONE : View.VISIBLE);
			legalInformationText.setVisibility(forward ? View.GONE : View.VISIBLE);
			disclaimerText.setVisibility(forward ? View.GONE : View.VISIBLE);
			depositPolicyText.setVisibility(forward ? View.GONE : View.VISIBLE);
			space.setVisibility(forward ? View.GONE : View.VISIBLE);
			listenToScroll = !forward;
			if (hotelMaterialFormTestEnabled) {
				travelerSummaryCardView.setVisibility(forward ? GONE : VISIBLE);
			}
			else {
				mainContactInfoCardView.setVisibility(forward ? GONE : VISIBLE);
			}
			if (!forward) {
				paymentInfoCardView.show(new PaymentWidget.PaymentDefault(), FLAG_CLEAR_BACKSTACK);
				paymentInfoCardView.setVisibility(paymentInfoCardView.isCreditCardRequired() ? VISIBLE : GONE);
				Ui.hideKeyboard(CheckoutBasePresenter.this);
				resetMenuButton();
			}
			else {
				if (FeatureUtilKt.isPopulateCardholderNameEnabled(getContext())) {
//					TODO update using travelerpresenter or travelerSummaryCard instead
					paymentInfoCardView.getViewmodel().getTravelerFirstName().onNext(mainContactInfoCardView.firstName);
					paymentInfoCardView.getViewmodel().getTravelerLastName().onNext(mainContactInfoCardView.lastName);
				}
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			super.updateTransition(f, forward);
		}

		@Override
		public void endTransition(boolean forward) {
			updateCheckoutOverviewUiTransition(forward);
		}
	};

	public void clearCCNumber() {
		try {
			paymentInfoCardView.getCreditCardNumber().setText("");
			Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setNumber(null);
			Db.getBillingInfo().setNumber(null);
			paymentInfoCardView.validateAndBind();
		}
		catch (Exception ex) {
			Log.e("Error clearing billingInfo card number", ex);
		}
	}

	public void scrollCheckoutToTop() {
		scrollView.scrollTo(0, 0);
	}

	@Override
	public void onUserAccountRefreshed() {
		doCreateTripAndScrollToCheckout();
	}

	private void doCreateTripAndScrollToCheckout() {
		if (userStateManager.isUserAuthenticated()) {
			listenToScroll = true;
			if (getLineOfBusiness() == LineOfBusiness.HOTELS && listenToScroll) {
				scrollToEnterDetails();
			}
		}
		doCreateTrip();
	}

	public abstract void doCreateTrip();

	public abstract void showProgress(boolean show);

	public void showCheckout() {
		show(new CheckoutDefault());
		userAccountRefresher.ensureAccountIsRefreshed();
	}

	@Override
	public boolean back() {
		if (CheckoutDefault.class.getName().equals(getCurrentState())) {
			return true;
		}
		return super.back();
	}

	public void onLoginSuccessful() {
		showProgress(true);

		User user = userStateManager.getUserSource().getUser();

		loginWidget.bind(false, true, user, getLineOfBusiness());
		mainContactInfoCardView.onLogin();
		paymentInfoCardView.getViewmodel().getUserAuthenticationState().onNext(true);
		hintContainer.setVisibility(GONE);
		show(new CheckoutDefault());
		doCreateTripAndScrollToCheckout();
	}

	public AlertDialog createLogOutAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		String msg = Phrase.from(getContext(), R.string.sign_out_confirmation_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format().toString();
		builder.setMessage(msg);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				logoutUser();
				OmnitureTracking.trackLogOutAction(OmnitureTracking.LogOut.SUCCESS);
			}
		});

		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				OmnitureTracking.trackLogOutAction(OmnitureTracking.LogOut.CANCEL);
			}
		});

		return builder.create();
	}

	private void logoutUser() {
		userStateManager.signOut();
		showProgress(true);
		mainContactInfoCardView.onLogout();
		paymentInfoCardView.getViewmodel().getUserAuthenticationState().onNext(false);
		hintContainer.setVisibility(VISIBLE);
		showCheckout();
	}

	@Override
	public void accountLoginClicked() {
		Bundle args = AccountLibActivity.createArgumentsBundle(getLineOfBusiness(), new CheckoutLoginExtender());
		userStateManager.signIn((Activity) getContext(), args);
	}

	@Override
	public void accountLogoutClicked() {
		createLogOutAlertDialog().show();
	}

	@Override
	public void collapsed(ExpandableCardView view) {
	}

	@Override
	public void expanded(ExpandableCardView view) {
	}

	public boolean isCheckoutFormComplete() {
		return paymentInfoCardView.isComplete() &&
			(hotelMaterialFormTestEnabled ? travelerSummaryCard.getStatus() == TravelerCheckoutStatus.COMPLETE : mainContactInfoCardView.isComplete()) ;
	}

	public Observer<Boolean> expandPaymentObserver = new Observer<Boolean>() {

		@Override
		public void onComplete() {
		}

		@Override
		public void onError(Throwable e) {

		}

		@Override
		public void onSubscribe(@NonNull Disposable d) {

		}

		@Override
		public void onNext(Boolean expand) {
			if (expand) {
				currentExpandedCard = null;
				show(paymentInfoCardView);
			}
		}
	};

	protected void updateLoginWidget() {
		loginWidget.bind(false, userStateManager.isUserAuthenticated(),
			userStateManager.isUserAuthenticated() ? userStateManager.getUserSource().getUser() : null, getLineOfBusiness());
	}

	protected void selectFirstAvailableCardIfOnlyOneAvailable() {
		if (userStateManager.isUserAuthenticated()) {
			User user = userStateManager.getUserSource().getUser();
			List<StoredCreditCard> storedCreditCards = new ArrayList<>();

			if (user != null) {
				storedCreditCards = user.getStoredCreditCards();
			}

			if (paymentInfoCardView.getSectionBillingInfo().getBillingInfo() != null && !paymentInfoCardView
				.getSectionBillingInfo().getBillingInfo().isCreditCardDataEnteredManually()
				&& storedCreditCards.size() == 1 && Db.sharedInstance.getTemporarilySavedCard() == null) {
				paymentInfoCardView.selectFirstAvailableCard();
			}
			else if (storedCreditCards.size() == 0 && Db.sharedInstance.getTemporarilySavedCard() != null) {
				paymentInfoCardView.getStoredCreditCardListener().onTemporarySavedCreditCardChosen(
					Db.sharedInstance.getTemporarilySavedCard());
			}
		}
	}


	protected void openTravelerPresenter() {
		show(travelersPresenter);
		travelersPresenter.showSelectOrEntryState();
	}

	private void updateCheckoutOverviewUiTransition(boolean forward) {
		if (forward) {
			slideToContainer.setVisibility(INVISIBLE);
			// Space to avoid keyboard hiding the view behind.
			int spacerHeight = (int) getResources().getDimension(R.dimen.checkout_expanded_space_height);
			ViewGroup.LayoutParams params = space.getLayoutParams();
			params.height = spacerHeight;
			space.setLayoutParams(params);
		}
		else {
			checkoutFormWasUpdated();
			updateSpacerHeight();
			if (getLineOfBusiness() == LineOfBusiness.HOTELS && listenToScroll) {
				scrollToEnterDetails();
			}
		}
	}
}
