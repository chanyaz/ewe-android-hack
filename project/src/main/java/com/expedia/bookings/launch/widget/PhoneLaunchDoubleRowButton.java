package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PhoneLaunchDoubleRowButton extends FrameLayout {

	private String text;
	private Drawable icon;
	private int disabledBg;
	private int bgColor;
	private boolean fiveLobBtn;

	private static final float MAX_ICON_SCALE = 1.0f;
	private float minIconSize;
	private float squashedRatio;

	@InjectView(R.id.lob_btn_bg)
	public CardView bgView;

	@InjectView(R.id.icon)
	public ImageView iconView;

	@InjectView(R.id.text)
	public TextView textView;

	private float fullHeight;
	private boolean firstTimeMeasure = true;
	private float scaleToFactor = 1;

	public PhoneLaunchDoubleRowButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PhoneLaunchButton);
			text = ta.getString(R.styleable.PhoneLaunchButton_btn_text);
			icon = ta.getDrawable(R.styleable.PhoneLaunchButton_btn_icon);
			bgColor = ta.getColor(R.styleable.PhoneLaunchButton_btn_bg, -1);
			minIconSize = ta.getFloat(R.styleable.PhoneLaunchButton_icon_min_scale, 0.75f);
			disabledBg = getResources().getColor(R.color.disabled_lob_btn);
			fiveLobBtn = ta.getBoolean(R.styleable.PhoneLaunchButton_five_lob_btn, false);
			ta.recycle();
		}
		LayoutInflater.from(getContext()).inflate(
			fiveLobBtn ? R.layout.widget_phone_launch_double_row_five_btn : R.layout.widget_phone_launch_double_row_btn,
			this);
		ButterKnife.inject(this);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ViewCompat.setElevation(textView, 2 * bgView.getCardElevation());
		ViewCompat.setElevation(iconView, 2 * bgView.getCardElevation());
		bgView.setCardBackgroundColor(bgColor);
		textView.setText(text);
		FontCache.setTypeface(textView, FontCache.Font.ROBOTO_LIGHT);
		iconView.setImageDrawable(icon);
		bgView.setPivotY(getBottom());
		float fullHeight = getResources().getDimension(R.dimen.launch_lob_double_row_container_height);
		float squashedHeight = getResources().getDimension(R.dimen.launch_lob_double_row_squashed_height);
		squashedRatio = squashedHeight / fullHeight;
		if (ProductFlavorFeatureConfiguration.getInstance().isLOBIconCenterAligned()) {
			FrameLayout.LayoutParams layoutParams = (LayoutParams) iconView.getLayoutParams();
			layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
			layoutParams.leftMargin = getResources().getDimensionPixelOffset(R.dimen.launch_lob_margin_left);
			iconView.setLayoutParams(layoutParams);
		}
	}

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		if (firstTimeMeasure) {
			firstTimeMeasure = false;
			initViewProperties();
		}
	}

	private void initViewProperties() {
		fullHeight = getResources().getDimension(R.dimen.launch_lob_double_row_height);
		iconView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				iconView.getViewTreeObserver().removeOnPreDrawListener(this);
				iconView.setPivotX(0);
				if (ProductFlavorFeatureConfiguration.getInstance().isLOBIconCenterAligned()) {
					iconView.setPivotY(0);
					iconView.setTranslationY((bgView.getBottom() - iconView.getBottom()) / 2);
				}
				else {
					iconView.setPivotY(-iconView.getHeight());
					iconView.setTranslationY(getBottom() - iconView.getBottom());
				}
				scaleTo(scaleToFactor);
				return true;
			}
		});
		textView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				textView.getViewTreeObserver().removeOnPreDrawListener(this);
				if (fiveLobBtn) {
					textView.setPivotX(textView.getWidth());
					textView.setPivotY(0);
				}
				else {
					textView.setPivotX(textView.getWidth() / 2);
					textView.setPivotY(-textView.getTop());
				}
				scaleTo(scaleToFactor);
				return true;
			}
		});
	}

	@OnClick(R.id.lob_btn_bg)
	public void onBgClick(View v) {

		if (Db.getMemoryTestActive()) {
			Events.post(new Events.MemoryTestInput(getId()));
			return;
		}

		if (isEnabled()) {
			Bundle animOptions = AnimUtils.createActivityScaleBundle(v);
			switch (getId()) {
			case R.id.hotels_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(LineOfBusiness.HOTELS);
				NavUtils.goToHotels(getContext(), animOptions);
				break;
			case R.id.flights_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(LineOfBusiness.FLIGHTS);
				NavUtils.goToFlights(getContext(), animOptions);
				break;
			case R.id.cars_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(LineOfBusiness.CARS);
				NavUtils.goToCars(getContext(), animOptions);
				break;
			case R.id.activities_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(LineOfBusiness.LX);
				NavUtils.goToActivities(getContext(), animOptions);
				break;
			case R.id.transport_button:
				OmnitureTracking.trackNewLaunchScreenLobNavigation(LineOfBusiness.TRANSPORT);
				NavUtils.goToTransport(getContext(), animOptions);
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
			bgView.setCardBackgroundColor(bgColor);
			textView.setAlpha(1.0f);
		}
		else {
			scaleTo(1.0f);
			bgView.setCardBackgroundColor(disabledBg);
			textView.setAlpha(0.5f);
		}
	}

	public void scaleTo(float f) {
		scaleToFactor = f;

		// Normalize float f between squash widget ratio and 1
		float normalized = (float) ((f - squashedRatio) / (1.0 - squashedRatio));

		float iconScale = (((f - squashedRatio) * (MAX_ICON_SCALE - minIconSize)) / (1.0f - squashedRatio)) + minIconSize;
		float bgScale = f;
		float scaledIconBottom = iconView.getBottom() * iconScale;
		float scaledBgBottom = bgView.getBottom() * bgScale;
		final float iconTranslation;

		if (ProductFlavorFeatureConfiguration.getInstance().isLOBIconCenterAligned()) {
			iconTranslation = ((bgView.getHeight() * bgScale) - (iconView.getHeight() * iconScale)) / 2;
		}
		else {
			iconTranslation = (scaledBgBottom - scaledIconBottom) + (iconView.getBottom() - scaledIconBottom);
		}

		iconView.setScaleX(iconScale);
		iconView.setScaleY(iconScale);
		iconView.setTranslationY(iconTranslation);
		bgView.setScaleY(f);

		if (fiveLobBtn) {
			float textScale = 1 - (1 - normalized) * 0.1f;
			textView.setScaleX(textScale);
			textView.setScaleY(textScale);
			textView.setTranslationY((bgView.getBottom() * f - textView.getHeight() * textScale - getResources()
				.getDimension(R.dimen.launch_tile_margin_side)*normalized) / (2 - normalized));
		}
		else {
			textView.setTranslationY((f * fullHeight) - fullHeight);
		}
	}
}

