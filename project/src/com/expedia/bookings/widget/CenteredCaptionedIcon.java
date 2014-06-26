package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.larvalabs.svgandroid.widget.SVGView;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class CenteredCaptionedIcon extends RelativeLayout {
	SVGView mSvg;
	TextView mCaption;

	public CenteredCaptionedIcon(Context context) {
		super(context);
		init(context, null);
	}

	public CenteredCaptionedIcon(Context context, AttributeSet attr) {
		super(context, attr);
		init(context, attr);
	}

	public CenteredCaptionedIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attr) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View widget = inflater.inflate(R.layout.widget_centered_captioned_icon, this);

		mSvg = Ui.findView(widget, R.id.svg);
		mCaption = Ui.findView(widget, R.id.caption);

		if (attr != null) {
			TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.CenteredCaptionedIcon, 0, 0);

			if (ta.hasValue(R.styleable.CenteredCaptionedIcon_caption)) {
				setCaption(ta.getString(R.styleable.CenteredCaptionedIcon_caption));
			}

			if (ta.hasValue(R.styleable.CenteredCaptionedIcon_svg)) {
				setSVG(ta.getResourceId(R.styleable.CenteredCaptionedIcon_svg, 0));
			}

			ta.recycle();
		}
	}

	public void setCaption(CharSequence caption) {
		mCaption.setText(caption);
	}

	public CharSequence getCaption() {
		return mCaption.getText();
	}

	public void setSVG(int rawResId) {
		mSvg.setSVG(rawResId);
	}

}
