package com.expedia.bookings.widget;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.interfaces.ToolbarListener;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class CheckoutBasePresenter extends Presenter implements SlideToWidgetLL.ISlideToListener {

	protected abstract LineOfBusiness getLineOfBusiness();

	public CheckoutBasePresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.widget_checkout_base, this);
	}

	@InjectView(R.id.checkout_scroll)
	ScrollView scrollView;

	@InjectView(R.id.checkout_toolbar)
	Toolbar toolbar;

	@InjectView(R.id.main_contact_info_card_view)
	public TravelerContactDetailsWidget mainContactInfoCardView;

	@InjectView(R.id.payment_info_card_view)
	public PaymentWidget paymentInfoCardView;

	@InjectView(R.id.slide_to_purchase_layout)
	ViewGroup slideToContainer;

	@InjectView(R.id.summary_container)
	CardView summaryContainer;

	@InjectView(R.id.login_widget)
	AccountLoginWidget loginWidget;

	@InjectView(R.id.hint_container)
	ViewGroup hintContainer;

	@InjectView(R.id.legal_information_text_view)
	TextView legalInformationText;

	@InjectView(R.id.slide_to_purchase_widget)
	public SlideToWidgetLL slideWidget;

	@InjectView(R.id.purchase_total_text_view)
	TextView sliderTotalText;

	MenuItem menuNext;
	MenuItem menuDone;

	ExpandableCardView lastExpandedCard;
	ExpandableCardView currentExpandedCard;

	@Override
	protected void onFinishInflate() {
		ButterKnife.inject(this);
		setupToolbar();

		addTransition(defaultToExpanded);
		slideWidget.addSlideToListener(this);

		loginWidget.setLineOfBusiness(getLineOfBusiness());
		loginWidget.setToolbarListener(toolbarListener);
		loginWidget.setLoginStatusListener(mLoginStatusListener);
		mainContactInfoCardView.setToolbarListener(toolbarListener);
		paymentInfoCardView.setToolbarListener(toolbarListener);
		hintContainer.setVisibility(User.isLoggedIn(getContext()) ? GONE : VISIBLE);
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

		menuNext = toolbar.getMenu().findItem(R.id.menu_next);
		menuNext.setVisible(false);

		menuDone = toolbar.getMenu().findItem(R.id.menu_done);
		menuDone.setVisible(false);

		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_checkout:
					Ui.hideKeyboard(CheckoutBasePresenter.this);
					menuItem.setVisible(false);
					slideToContainer.setVisibility(View.VISIBLE);
					return true;
				case R.id.menu_next:
					currentExpandedCard.setNextFocus();
					return true;
				case R.id.menu_done:
					currentExpandedCard.onDonePressed();
					Ui.hideKeyboard(CheckoutBasePresenter.this);
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

	// Listener to update the toolbar status when a widget(Login, Driver Info, Payment) is being interacted with
	ToolbarListener toolbarListener = new ToolbarListener() {
		@Override
		public void setActionBarTitle(String title) {
			toolbar.setTitle(title);
		}

		@Override
		public void onWidgetExpanded(ExpandableCardView cardView) {
			lastExpandedCard = currentExpandedCard;
			currentExpandedCard = cardView;
			show(new WidgetExpanded());
		}

		@Override
		public void onWidgetClosed() {
			back();
		}

		@Override
		public void onEditingComplete() {
			menuNext.setVisible(false);
			menuDone.setVisible(true);
		}
	};

	void isCheckoutComplete() {
		if (mainContactInfoCardView.isComplete() && paymentInfoCardView.isComplete()) {
			animateInSlideTo(true);
			OmnitureTracking.trackAppCarCheckoutSlideToPurchase(getContext(), paymentInfoCardView.getCardType());
		}
		else {
			animateInSlideTo(false);
		}
	}

	public static class CheckoutDefault {
	}

	public static class WidgetExpanded {
	}

	private Presenter.Transition defaultToExpanded = new Presenter.Transition(CheckoutDefault.class,
		WidgetExpanded.class) {

		@Override
		public void startTransition(boolean forward) {
			if (forward) {
				summaryContainer.setVisibility(GONE);
				if (loginWidget != currentExpandedCard) {
					loginWidget.setVisibility(GONE);
				}
				hintContainer.setVisibility(GONE);
				if (mainContactInfoCardView != currentExpandedCard) {
					mainContactInfoCardView.setVisibility(GONE);
				}
				if (paymentInfoCardView != currentExpandedCard) {
					paymentInfoCardView.setVisibility(GONE);
				}
				legalInformationText.setVisibility(GONE);
				if (lastExpandedCard != null && lastExpandedCard != currentExpandedCard) {
					lastExpandedCard.setExpanded(false, false);
				}
			}
			else {
				currentExpandedCard.setExpanded(false, false);
				summaryContainer.setVisibility(VISIBLE);
				loginWidget.setVisibility(VISIBLE);
				hintContainer.setVisibility(User.isLoggedIn(getContext()) ? GONE : VISIBLE);
				mainContactInfoCardView.setVisibility(VISIBLE);
				if (paymentInfoCardView.isCreditCardRequired()) {
					paymentInfoCardView.setVisibility(VISIBLE);
				}
				legalInformationText.setVisibility(VISIBLE);
				Ui.hideKeyboard(CheckoutBasePresenter.this);
			}

			toolbar.setTitle(forward ? currentExpandedCard.getActionBarTitle()
				: getContext().getString(R.string.cars_checkout_text));
			Drawable nav = getResources().getDrawable(forward ? R.drawable.ic_close_white_24dp : R.drawable.ic_arrow_back_white_24dp).mutate();
			nav.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
			toolbar.setNavigationIcon(nav);
			menuNext.setVisible(forward ? true : false);
			menuDone.setVisible(false);
		}

		@Override
		public void updateTransition(float f, boolean forward) {

		}

		@Override
		public void endTransition(boolean forward) {

		}

		@Override
		public void finalizeTransition(boolean forward) {
			if (forward) {
				slideToContainer.setVisibility(INVISIBLE);
			}
			else {
				isCheckoutComplete();
			}
		}
	};

	AccountLoginWidget.LogInStatusListener mLoginStatusListener = new AccountLoginWidget.LogInStatusListener() {
		@Override
		public void onLoginStarted() {

		}

		@Override
		public void onLoginCompleted() {
			mainContactInfoCardView.onLogin();
			paymentInfoCardView.onLogin();
			isCheckoutComplete();
			hintContainer.setVisibility(GONE);
			OmnitureTracking.trackAppCarCheckoutLoginSuccess(getContext());
		}

		@Override
		public void onLoginFailed() {

		}

		@Override
		public void onLogout() {
			mainContactInfoCardView.onLogout();
			paymentInfoCardView.onLogout();
			isCheckoutComplete();
			hintContainer.setVisibility(VISIBLE);
		}
	};

	private void animateInSlideTo(boolean visible) {
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
	}
}
