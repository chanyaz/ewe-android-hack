package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LxRulesWidget extends LinearLayout {
	public LxRulesWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		inflate(context, R.layout.widget_lx_rules, this);
	}

	//@InjectView(R.id.cancellation_policy_text_view)
	TextView cancellationPolicy;

	//@InjectView(R.id.lx_rules_toolbar)
	Toolbar toolbar;

	private String tripId;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		setupToolbar();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onCreateTripSucceeded(Events.LXCreateTripSucceeded event) {
		this.tripId = event.createTripResponse.tripId;
		updateCancellationPolicyDisplayText(event.activity.freeCancellationMinHours);
	}

	//@OnClick(R.id.rules_and_restrictions)
	public void showRulesAndRestrictions() {
		String e3EndpointUrl = Ui.getApplication(getContext()).appComponent().endpointProvider().getE3EndpointUrl();
		showLegalPage(LXDataUtils.getRulesRestrictionsUrl(e3EndpointUrl, tripId), R.string.rules_and_restrictions);
	}

	//@OnClick(R.id.terms_and_conditions)
	public void showTermsAndConditions() {
		showLegalPage(PointOfSale.getPointOfSale().getTermsAndConditionsUrl(), R.string.terms_and_conditions);
	}

	//@OnClick(R.id.privacy_policy)
	public void showPrivacyPolicy() {
		showLegalPage(PointOfSale.getPointOfSale().getPrivacyPolicyUrl(), R.string.privacy_policy);
	}

	private void showLegalPage(String legalPageUrl, int titleResId) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
		builder.setInjectExpediaCookies(true);
		builder.setTitle(titleResId);
		builder.setUrl(legalPageUrl);
		getContext().startActivity(builder.getIntent());
	}

	private void setupToolbar() {
		Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp).mutate();
		drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(drawable);
		toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		toolbar.setTitle(getResources().getString(R.string.legal_information));
		toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance);
		toolbar.setSubtitleTextAppearance(getContext(), R.style.ToolbarSubtitleTextAppearance);
		toolbar.setBackgroundColor(Ui.obtainThemeColor(getContext(), R.attr.primary_color));

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbar.setPadding(0, statusBarHeight, 0, 0);
	}

	private void updateCancellationPolicyDisplayText(int freeCancellationMinHours) {
		cancellationPolicy.setText(
			LXDataUtils.getCancelationPolicyDisplayText(getContext(), freeCancellationMinHours));
	}
}
