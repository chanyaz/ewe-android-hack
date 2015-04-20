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
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PhoneLaunchDoubleRowButton extends FrameLayout {

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
	private float fullHeight;

	public PhoneLaunchDoubleRowButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.widget_phone_launch_double_row_btn, this);
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

	@Override
	public void onFinishInflate() {
		Ui.setViewBackground(bgView, bg);
		textView.setText(text);
		FontCache.setTypeface(textView, FontCache.Font.ROBOTO_LIGHT);
		iconView.setImageDrawable(icon);
		bgView.setPivotY(getBottom());
		float squashedHeight = getResources().getDimension(R.dimen.launch_lob_squashed_height);
		float fullHeight = getResources().getDimension(R.dimen.launch_lob_double_row_container_height);
		squashedRatio =  squashedHeight / fullHeight;
	}

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		fullHeight = getResources().getDimension(R.dimen.launch_lob_double_row_height);
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
		if (isEnabled()) {
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
			case R.id.activities_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(getContext(), LineOfBusiness.LX);
				NavUtils.goToLocalExpert(getContext(), animOptions);
				break;
			default:
				throw new RuntimeException("No onClick defined for PhoneLaunchButton with id: " + getId());
			}
		}
		else {
			AnimUtils.doTheHarlemShake(this);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			scaleTo(1.0f);
			Ui.setViewBackground(bgView, bg);
			textView.setAlpha(1.0f);
		}
		else {
			scaleTo(1.0f);
			Ui.setViewBackground(bgView, disabledBg);
			textView.setAlpha(0.5f);
		}
	}

	private static final float minIconSize = 0.5f;
	private static final float maxIconSize = 1.0f;

	public void scaleTo(float f) {
		float iconSize;
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
		textView.setTranslationY((f * fullHeight) - fullHeight);
		bgView.setScaleY(f);
	}

}

