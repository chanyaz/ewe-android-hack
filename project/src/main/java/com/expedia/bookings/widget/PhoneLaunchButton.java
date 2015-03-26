package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PhoneLaunchButton extends FrameLayout {

	private String text;
	private Drawable icon;
	private Drawable bg;
	private Drawable disabledBg;

	@InjectView(R.id.lob_btn_bg)
	public ViewGroup bgView;

	@InjectView(R.id.icon)
	public ImageView iconView;

	@InjectView(R.id.text)
	public TextView textView;

	private float squashedRatio;
	private boolean isNetworkAvailable = true;

	public PhoneLaunchButton(Context context) {
		this(context, null);
	}

	public PhoneLaunchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(getContext()).inflate(R.layout.widget_phone_launch_btn, this);
		ButterKnife.inject(this);

		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PhoneLaunchButton);
			text = ta.getString(R.styleable.PhoneLaunchButton_btn_text);
			icon = ta.getDrawable(R.styleable.PhoneLaunchButton_btn_icon);
			bg = ta.getDrawable(R.styleable.PhoneLaunchButton_btn_bg);
			disabledBg = getResources().getDrawable(R.drawable.bg_lob_disabled);
			ta.recycle();
		}
	}

	public PhoneLaunchButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	public void onFinishInflate() {
		Ui.setViewBackground(bgView, bg);
		textView.setText(text);
		iconView.setImageDrawable(icon);
		FontCache.setTypeface(textView, FontCache.Font.ROBOTO_LIGHT);
		bgView.setPivotY(getBottom());
		float squashedHeight = getResources().getDimension(R.dimen.launch_lob_squashed_height);
		float fullHeight = getResources().getDimension(R.dimen.launch_lob_container_height);
		squashedRatio =  squashedHeight / fullHeight;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	public void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		iconView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				iconView.getViewTreeObserver().removeOnPreDrawListener(this);
				iconView.setPivotX(iconView.getWidth() / 2);
				iconView.setPivotY(-iconView.getTop() + iconView.getPaddingTop());
				return true;
			}
		});
		textView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				textView.getViewTreeObserver().removeOnPreDrawListener(this);
				textView.setPivotX(textView.getWidth() / 2);
				textView.setPivotY(-textView.getTop());
				return true;
			}
		});
	}

	@OnClick(R.id.lob_btn_bg)
	public void onBgClick(View v) {
		if (isNetworkAvailable) {
			Bundle animOptions = AnimUtils.createActivityScaleBundle(v);
			switch (getId()) {
			case R.id.hotels_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(getContext(), LineOfBusiness.HOTELS);
				NavUtils.goToHotels(getContext(), animOptions);
				break;
			case R.id.flights_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(getContext(), LineOfBusiness.FLIGHTS);
				NavUtils.goToFlights(getContext(), animOptions);
				break;
			case R.id.cars_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(getContext(), LineOfBusiness.CARS);
				NavUtils.goToCars(getContext(), animOptions);
				break;
			default:
				throw new RuntimeException("No onClick defined for PhoneLaunchButton with id: " + getId());
			}
		}
		else {
			AnimUtils.doTheHarlemShake(this);
		}
	}

	@Subscribe
	public void onNetworkAvailable(Events.LaunchOnlineState event) {
		isNetworkAvailable = true;
		Ui.setViewBackground(bgView, bg);
		textView.setAlpha(1.0f);
	}

	@Subscribe
	public void onNetworkUnavailable(Events.LaunchOfflineState event) {
		isNetworkAvailable = false;
		Ui.setViewBackground(bgView, disabledBg);
		textView.setAlpha(0.5f);
	}

	private static final float minIconAlpha = 0.3f;
	private static final float maxIconAlpha = 0.6f;
	private static final float minIconSize = 0.5f;
	private static final float maxIconSize = 1.0f;

	public void scaleTo(float f) {
		float normalized = (float) ((f - squashedRatio) / (1.0 - squashedRatio));

		// bound text scale between 0 and 1
		float textAlpha = normalized < 0.0f ? 0.0f : (normalized > 1.0f ? 1.0f : normalized);

		float iconAlpha;
		float iconSize;

		// Bound icon alpha scale between minIconAlpha and maxIconAlpha
		if (1 - f < minIconAlpha) {
			iconAlpha = minIconAlpha;
		}
		else if (1 - f > maxIconAlpha) {
			iconAlpha = maxIconAlpha;
		}
		else {
			iconAlpha = 1 - f;
		}
		// Bound icon size scale between minIconSize and maxIconSize
		if (f < minIconSize) {
			iconSize = minIconSize;
		}
		else if (f > maxIconSize) {
			iconSize = maxIconSize;
		}
		else {
			iconSize = f;
		}

		iconView.setScaleX(iconSize);
		iconView.setScaleY(iconSize);
		iconView.setAlpha(iconAlpha);
		textView.setScaleX(f);
		textView.setScaleY(f);
		textView.setAlpha(textAlpha);
		bgView.setScaleY(f);
	}

}
