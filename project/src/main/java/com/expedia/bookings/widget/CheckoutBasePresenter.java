package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.ToolbarListener;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class CheckoutBasePresenter extends Presenter implements SlideToWidgetJB.ISlideToListener {

	public CheckoutBasePresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.checkout_scroll)
	ScrollView scrollView;

	@InjectView(R.id.checkout_toolbar)
	Toolbar toolbar;

	@InjectView(R.id.main_contact_info_card_view)
	TravelerContactDetailsWidget mainContactInfoCardView;

	@InjectView(R.id.payment_info_card_view)
	PaymentWidget paymentInfoCardView;

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
	SlideToWidgetJB slideWidget;

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

		loginWidget.setToolbarListener(toolbarListener);
		loginWidget.setLoginStatusListener(mLoginStatusListener);
		mainContactInfoCardView.setToolbarListener(toolbarListener);
		paymentInfoCardView.setToolbarListener(toolbarListener);
	}

	public void setupToolbar() {
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		toolbar.setTitle(getContext().getString(R.string.cars_checkout_text));
		//toolbar.setTitleTextColor(Color.WHITE);
		//toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		//toolbar.setBackgroundColor(getResources().getColor(R.color.cars_primary_color));
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
			((Activity) getContext()).onBackPressed();
		}

		@Override
		public void onEditingComplete() {
			menuNext.setVisible(false);
			menuDone.setVisible(true);
		}
	};

	void isCheckoutComplete() {
		if (mainContactInfoCardView.isComplete() && paymentInfoCardView.isComplete()) {
			slideToContainer.setVisibility(VISIBLE);
		}
		else {
			slideToContainer.setVisibility(GONE);
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
				Ui.showKeyboard(CheckoutBasePresenter.this, null);
			}
			else {
				currentExpandedCard.setExpanded(false, false);
				summaryContainer.setVisibility(VISIBLE);
				loginWidget.setVisibility(VISIBLE);
				hintContainer.setVisibility(VISIBLE);
				mainContactInfoCardView.setVisibility(VISIBLE);
				if (paymentInfoCardView.isCreditCardRequired()) {
					paymentInfoCardView.setVisibility(VISIBLE);
				}
				legalInformationText.setVisibility(VISIBLE);
				Ui.hideKeyboard(CheckoutBasePresenter.this);
			}

			toolbar.setTitle(forward ? currentExpandedCard.getActionBarTitle()
				: getContext().getString(R.string.cars_checkout_text));
			toolbar.setNavigationIcon(forward ? R.drawable.ic_close_white_24dp : R.drawable.ic_arrow_back_white_24dp);
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
				slideToContainer.setVisibility(GONE);
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
		}

		@Override
		public void onLoginFailed() {

		}

		@Override
		public void onLogout() {
			mainContactInfoCardView.onLogout();
			paymentInfoCardView.onLogout();
			isCheckoutComplete();
		}
	};
}
