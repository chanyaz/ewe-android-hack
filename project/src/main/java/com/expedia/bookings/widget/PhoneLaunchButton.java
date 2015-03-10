package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PhoneLaunchButton extends FrameLayout {

	private String text;
	private Drawable icon;
	private int color;

	@InjectView(R.id.lob_bg_to_scale)
	public ViewGroup scaleBg;

	@InjectView(R.id.lob_btn_bg)
	public ViewGroup bgView;

	@InjectView(R.id.icon)
	public ImageView iconView;

	@InjectView(R.id.text)
	public TextView textView;

	private float squashedRatio;

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
			color = ta.getColor(R.styleable.PhoneLaunchButton_btn_color, 0);
			ta.recycle();
		}
	}

	public PhoneLaunchButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	public void onFinishInflate() {
		bgView.setBackgroundColor(color);
		textView.setText(text);
		iconView.setImageDrawable(icon);
		scaleBg.setPivotY(getBottom());
		float squashedHeight = getResources().getDimension(R.dimen.launch_lob_squashed_height);
		float fullHeight = getResources().getDimension(R.dimen.launch_lob_container_height);
		squashedRatio =  squashedHeight / fullHeight;
	}

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		iconView.setPivotX(iconView.getWidth() / 2);
		iconView.setPivotY(-iconView.getTop() + iconView.getPaddingTop());
		textView.setPivotX(textView.getWidth() / 2);
		textView.setPivotY(-textView.getTop());
	}

	private static final float minIconSize = 0.3f;
	private static final float maxIconSize = 0.6f;

	public void scaleTo(float f) {
		float normalized = (float) ((f - squashedRatio) / (1.0 - squashedRatio));

		// bound text scale between 0 and 1
		float boundedText = normalized < 0.0f ? 0.0f : (normalized > 1.0f ? 1.0f : normalized);

		float boundedIcon;
		// Bound icon scale between minIconSize and maxIconSize
		if (1 - f < minIconSize) {
			boundedIcon = minIconSize;
		}
		else if (1 - f > maxIconSize) {
			boundedIcon = maxIconSize;
		}
		else {
			boundedIcon = 1 - f;
		}

		iconView.setScaleX(f);
		iconView.setScaleY(f);
		iconView.setAlpha(boundedIcon);
		textView.setScaleX(f);
		textView.setScaleY(f);
		textView.setAlpha(boundedText);
		scaleBg.setScaleY(f);
	}

}
