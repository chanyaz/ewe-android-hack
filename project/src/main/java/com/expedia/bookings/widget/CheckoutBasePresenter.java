package com.expedia.bookings.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.interfaces.ToolbarListener;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.tracking.HotelV2Tracking;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class CheckoutBasePresenter extends Presenter implements SlideToWidgetLL.ISlideToListener,
	UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener {

	protected abstract LineOfBusiness getLineOfBusiness();

	public CheckoutBasePresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.widget_checkout_base, this);
	}

	@InjectView(R.id.checkout_scroll)
	public ScrollView scrollView;

	@InjectView(R.id.scroll_content)
	public LinearLayout checkoutContent;

	@InjectView(R.id.checkout_toolbar)
	Toolbar toolbar;

	@InjectView(R.id.main_contact_info_card_view)
	public TravelerContactDetailsWidget mainContactInfoCardView;

	@InjectView(R.id.payment_info_card_view)
	public PaymentWidget paymentInfoCardView;

	@InjectView(R.id.slide_to_purchase_layout)
	public ViewGroup slideToContainer;

	@InjectView(R.id.summary_container)
	public FrameLayout summaryContainer;

	public View mSummaryProgressLayout;

	@InjectView(R.id.login_widget)
	public AccountButton loginWidget;

	@InjectView(R.id.hint_container)
	ViewGroup hintContainer;

	@InjectView(R.id.legal_information_text_view)
	public TextView legalInformationText;

	@InjectView(R.id.layout_confirm_tos)
	public AcceptTermsWidget acceptTermsWidget;

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

	private boolean listenToScroll = true;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		setupToolbar();

		addTransition(defaultToExpanded);
		addTransition(defaultToReady);
		addTransition(defaultToCheckoutFailed);
		addDefaultTransition(defaultTransition);

		slideWidget.addSlideToListener(this);

		loginWidget.setListener(this);
		mainContactInfoCardView.setLineOfBusiness(getLineOfBusiness());
		paymentInfoCardView.setLineOfBusiness(getLineOfBusiness());
		mainContactInfoCardView.setToolbarListener(toolbarListener);
		paymentInfoCardView.setToolbarListener(toolbarListener);
		hintContainer.setVisibility(User.isLoggedIn(getContext()) ? GONE : VISIBLE);
		legalInformationText.setMovementMethod(LinkMovementMethod.getInstance());
		slideToContainer.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Consume touches so they don't pass behind
				return true;
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

		if (getLineOfBusiness() == LineOfBusiness.HOTELSV2) {
			scrollView.addOnScrollListener(checkoutScrollListener);
		}
	}

	public void setupToolbar() {
		Drawable nav = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp).mutate();
		nav.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(nav);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});

		toolbar.setTitle(getContext().getString(R.string.cars_checkout_text));
		toolbar.inflateMenu(R.menu.cars_checkout_menu);

		menuDone = toolbar.getMenu().findItem(R.id.menu_done);
		// Let's start with not showing the menuDone button
		menuDone.setVisible(false);

		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_done:
					if (menuItem.getTitle().equals(getResources().getString(R.string.done)) || menuItem.getTitle().equals(getResources().getString(R.string.coupon_submit_button))) {
						currentExpandedCard.onMenuButtonPressed();
						Ui.hideKeyboard(CheckoutBasePresenter.this);
					}
					else if (menuItem.getTitle().equals(getResources().getString(R.string.next))) {
						if (getLineOfBusiness() == LineOfBusiness.HOTELSV2 && listenToScroll) {
							scrollToEnterDetails();
						}
						else {
							currentExpandedCard.setNextFocus();
						}
					}
					return true;
				}

				return false;
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = Ui.obtainThemeColor(getContext(), R.attr.primary_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, scrollView, color));
		}
	}

	public void resetMenuButton() {
		if (getLineOfBusiness() == LineOfBusiness.HOTELSV2) {
			menuDone.setVisible(true);
			menuDone.setTitle(R.string.next);
		}
		else {
			menuDone.setVisible(false);
		}
	}

	private void scrollToEnterDetails() {
		Ui.hideKeyboard(CheckoutBasePresenter.this);

		int targetScrollY = loginWidget.getTop() - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
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

		scrollView.postDelayed(new Runnable() {
			@Override
			public void run() {
				scrollAnimation.start();
			}
		}, 100L);
	}

	com.expedia.bookings.widget.ScrollView.OnScrollListener checkoutScrollListener = new ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			if (listenToScroll) {
				int top = loginWidget.getTop() - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
				if (y >= top) {
					menuDone.setVisible(false);
				}
				else {
					menuDone.setVisible(true);
				}
			}
		}
	};

	// Listener to update the toolbar status when a widget(Login, Driver Info, Payment) is being interacted with
	public ToolbarListener toolbarListener = new ToolbarListener() {
		@Override
		public void setActionBarTitle(String title) {
			toolbar.setTitle(title);
		}

		@Override
		public void onWidgetExpanded(ExpandableCardView cardView) {
			lastExpandedCard = currentExpandedCard;
			currentExpandedCard = cardView;
			menuDone.setTitle(currentExpandedCard.getMenuButtonTitle());
			show(new WidgetExpanded());
		}

		@Override
		public void onWidgetClosed() {
			back();
		}

		@Override
		public void onEditingComplete() {
			menuDone.setVisible(true);
		}

		@Override
		public void setMenuLabel(String label) {
			menuDone.setTitle(label);
		}

		@Override
		public void showRightActionButton(boolean show) {
			menuDone.setVisible(show);
		}
	};

	public void animateInSlideToPurchase(boolean visible) {
		// If its already in position, don't do it again
		if (slideToContainer.getVisibility() == (visible ? VISIBLE : INVISIBLE)) {
			return;
		}

		slideToContainer.setTranslationY(visible ? slideToContainer.getHeight() : 0);
		slideToContainer.setVisibility(VISIBLE);
		ObjectAnimator animator = ObjectAnimator
			.ofFloat(slideToContainer, "translationY", visible ? 0 : slideToContainer.getHeight());
		animator.setDuration(300);
		animator.start();

		if (visible) {
			scrollView.post(new Runnable() {
				@Override
				public void run() {
					scrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
			if (getLineOfBusiness() == LineOfBusiness.HOTELSV2) {
				new HotelV2Tracking().trackHotelV2SlideToPurchase();
			}
			else {
				OmnitureTracking.trackCheckoutSlideToPurchase(getLineOfBusiness(), getContext(),
					paymentInfoCardView.getCardType());
			}
		}
	}

	public void checkoutFormWasUpdated() {
		if (mainContactInfoCardView.isComplete() && paymentInfoCardView.isComplete()) {
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
		public void finalizeTransition(boolean forward) {
			showProgress(true);
			paymentInfoCardView.setCreditCardRequired(false);
			for (int i = 0; i < checkoutContent.getChildCount(); i ++) {
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
					hintContainer.setVisibility(forward ? User.isLoggedIn(getContext()) ? GONE : VISIBLE : INVISIBLE);
				}
				else if (v == paymentInfoCardView) {
					paymentInfoCardView
						.setVisibility(forward && paymentInfoCardView.isCreditCardRequired() ? VISIBLE : INVISIBLE);
				}
				else {
					v.setVisibility(forward ? VISIBLE : INVISIBLE);
				}
			}
			summaryContainer.setVisibility(VISIBLE);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			showProgress(!forward);
			if (forward) {
				checkoutFormWasUpdated();
			}
			else {
				animateInSlideToPurchase(false);
			}
			updateSpacerHeight();
			listenToScroll = true;
		}
	};

	protected void updateSpacerHeight() {
		float scrollViewActualHeight = scrollView.getHeight() - scrollView.getPaddingTop();
		if (scrollViewActualHeight - legalInformationText.getBottom() < slideToContainer.getHeight()) {
			ViewGroup.LayoutParams params = space.getLayoutParams();
			params.height = slideToContainer.getVisibility() == VISIBLE ? slideToContainer.getHeight() : 0;
			space.setLayoutParams(params);
		}
	}

	private Transition defaultToCheckoutFailed = new Transition(CheckoutDefault.class, CheckoutFailed.class) {
		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			showProgress(false);
		}
	};

	private Presenter.Transition defaultToExpanded = new Presenter.Transition(Ready.class,
		WidgetExpanded.class) {

		@Override
		public void startTransition(boolean forward) {
			if (forward) {
				for (int i = 0; i < checkoutContent.getChildCount(); i++) {
					View v = checkoutContent.getChildAt(i);
					if (v instanceof ExpandableCardView && v == currentExpandedCard) {
						continue;
					}
					v.setVisibility(GONE);
				}
				listenToScroll = false;
				if (lastExpandedCard != null && lastExpandedCard != currentExpandedCard) {
					lastExpandedCard.setExpanded(false, false);
				}
			}
			else {
				currentExpandedCard.setExpanded(false, false);
				for (int i = 0; i < checkoutContent.getChildCount(); i++) {
					View v = checkoutContent.getChildAt(i);
					if (v == hintContainer) {
						hintContainer.setVisibility(User.isLoggedIn(getContext()) ? GONE : VISIBLE);
					}
					else if (v == paymentInfoCardView) {
						paymentInfoCardView.setVisibility(paymentInfoCardView.isCreditCardRequired() ? VISIBLE : GONE);
					}
					else {
						v.setVisibility(VISIBLE);
					}
				}

				Ui.hideKeyboard(CheckoutBasePresenter.this);
				resetMenuButton();
				listenToScroll = true;
			}

			toolbar.setTitle(forward ? currentExpandedCard.getActionBarTitle()
				: getContext().getString(R.string.cars_checkout_text));
			Drawable nav = getResources().getDrawable(forward ? R.drawable.ic_close_white_24dp : R.drawable.ic_arrow_back_white_24dp).mutate();
			nav.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
			toolbar.setNavigationIcon(nav);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			if (forward) {
				slideToContainer.setVisibility(INVISIBLE);
				// Space to avoid keyboard hiding the view behind.
				int spacerHeight = (int) getResources().getDimension(R.dimen.car_expanded_space_height);
				ViewGroup.LayoutParams params = space.getLayoutParams();
				params.height = spacerHeight;
				space.setLayoutParams(params);
			}
			else {
				checkoutFormWasUpdated();
				updateSpacerHeight();
			}
			if (!forward) {
				scrollToEnterDetails();
			}
		}
	};

	public void clearCCNumber() {
		try {
			paymentInfoCardView.creditCardNumber.setText("");
			Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setNumber(null);
			Db.getBillingInfo().setNumber(null);
			paymentInfoCardView.bind();
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
		doCreateTrip();
		if (User.isLoggedIn(getContext())) {
			listenToScroll = true;
			scrollToEnterDetails();
		}
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
		loginWidget.bind(false, true, Db.getUser(), getLineOfBusiness());
		mainContactInfoCardView.onLogin();
		paymentInfoCardView.onLogin();
		hintContainer.setVisibility(GONE);
		showCheckout();
	}

	@Override
	public void accountLoginClicked() {
		Bundle args = AccountLibActivity.createArgumentsBundle(getLineOfBusiness(), new CheckoutLoginExtender());
		User.signIn((Activity) getContext(), args);
	}

	@Override
	public void accountLogoutClicked() {
		User.signOut(getContext());
		showProgress(true);
		mainContactInfoCardView.onLogout();
		paymentInfoCardView.onLogout();
		hintContainer.setVisibility(VISIBLE);
		showCheckout();
	}
}
