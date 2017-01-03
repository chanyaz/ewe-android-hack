package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PhoneLaunchButton extends FrameLayout {

	private String text;
	private float textSize;
	private Drawable icon;
	private int disabledBg;
	private int bgColor;
	private int iconPaddingTop;
	private int bgCardHeight;
	private float fullHeight;
	private float minIconSize;
	private boolean fiveLobBtn;

	@InjectView(R.id.lob_btn_bg)
	public CardView bgView;

	@InjectView(R.id.icon)
	public ImageView iconView;

	@InjectView(R.id.text)
	public TextView textView;

	private float squashedRatio;
	private boolean isNetworkAvailable = true;
	private boolean firstTimeMeasure = true;
	private float scaleToFactor = 1;

	public PhoneLaunchButton(Context context) {
		this(context, null);
	}

	public PhoneLaunchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PhoneLaunchButton);
			text = ta.getString(R.styleable.PhoneLaunchButton_btn_text);
			textSize = ta.getDimension(R.styleable.PhoneLaunchButton_btn_text_size,
					getResources().getDimension(R.dimen.launch_lob_button_text_size));
			icon = ta.getDrawable(R.styleable.PhoneLaunchButton_btn_icon);
			disabledBg = getResources().getColor(R.color.disabled_lob_btn);
			bgColor = ta.getColor(R.styleable.PhoneLaunchButton_btn_bg, -1);
			bgCardHeight = (int) ta.getDimension(R.styleable.PhoneLaunchButton_bg_card_height,
				getResources().getDimension(R.dimen.launch_lob_height));
			iconPaddingTop = (int) ta.getDimension(R.styleable.PhoneLaunchButton_bg_icon_padding_top,
				getResources().getDimension(R.dimen.launch_lob_button_top_padding));
			fullHeight = ta.getDimension(R.styleable.PhoneLaunchButton_refernce_container_height,
				getResources().getDimension(R.dimen.launch_lob_container_height));
			minIconSize = ta.getFloat(R.styleable.PhoneLaunchButton_icon_min_scale, 0.5f);
			fiveLobBtn = ta.getBoolean(R.styleable.PhoneLaunchButton_five_lob_btn, false);
			ta.recycle();
		}

		LayoutInflater.from(getContext())
			.inflate(fiveLobBtn ? R.layout.widget_phone_launch_five_btn : R.layout.widget_phone_launch_btn, this);
		ButterKnife.inject(this);
	}

	public PhoneLaunchButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ViewCompat.setElevation(textView, 2 * bgView.getCardElevation());
		ViewCompat.setElevation(iconView, 2 * bgView.getCardElevation());
		bgView.setCardBackgroundColor(bgColor);
		textView.setText(text);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		iconView.setImageDrawable(icon);
		iconView.setPadding(0, iconPaddingTop, 0, 0);
		FontCache.setTypeface(textView, FontCache.Font.ROBOTO_LIGHT);
		bgView.setPivotY(getBottom());
		bgView.setMinimumHeight(bgCardHeight);
		float squashedHeight = getResources().getDimension(R.dimen.launch_lob_squashed_height);
		squashedRatio =  squashedHeight / fullHeight;
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
		iconView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				iconView.getViewTreeObserver().removeOnPreDrawListener(this);
				if (fiveLobBtn) {
					iconView.setPivotX(iconView.getWidth() / 2);
					iconView.setPivotY(0);
				}
				else {
					iconView.setPivotX(iconView.getWidth() / 2);
					iconView.setPivotY(-iconView.getTop() + iconView.getPaddingTop());
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
					textView.setPivotX(textView.getWidth() / 2);
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
		if (isNetworkAvailable) {
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

	public void transformToDefaultState() {
		if (!isNetworkAvailable) {
			isNetworkAvailable = true;
			scaleTo(1.0f);
			bgView.setCardBackgroundColor(bgColor);
			textView.setAlpha(1.0f);
		}
	}

	public void transformToNoDataState() {
		if (isNetworkAvailable) {
			isNetworkAvailable = false;
			scaleTo(1.0f);
			bgView.setCardBackgroundColor(disabledBg);
			textView.setAlpha(0.5f);
		}
	}

	private static final float minIconAlpha = 0.3f;
	private static final float maxIconAlpha = 0.6f;
	private static final float maxIconSize = 1.0f;

	public void scaleTo(float f) {
		scaleToFactor = f;

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
		bgView.setScaleY(f);

		if (!fiveLobBtn) {
			iconView.setAlpha(iconAlpha);
			textView.setScaleX(f);
			textView.setScaleY(f);
			textView.setAlpha(textAlpha);
		}
		else {
			float textScale = 1 - (1 - normalized) * 0.15f;
			textView.setScaleX(textScale);
			textView.setScaleY(textScale);

			float textViewMarginBottom = getResources().getDimensionPixelOffset(R.dimen.launch_tile_margin_side);
			float textTransY = bgView.getHeight() * f - textView.getHeight()*textScale - textViewMarginBottom * normalized;
			textView.setTranslationY(textTransY);

			iconView.setTranslationY((textTransY - (iconView.getHeight() * iconSize)) / 2);
		}
	}

}
