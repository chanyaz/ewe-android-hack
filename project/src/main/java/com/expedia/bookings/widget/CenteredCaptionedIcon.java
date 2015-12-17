package com.expedia.bookings.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.larvalabs.svgandroid.widget.SVGView;
import com.mobiata.android.util.Ui;

public class CenteredCaptionedIcon extends RelativeLayout {
	SVGView mSvg;
	TextView mCaption;
	TextView mActionButton;

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
		mActionButton = Ui.findView(widget, R.id.action_button);

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
		setCaption(caption, null);
	}

	public void setCaption(CharSequence caption, final String url) {
		mCaption.setText(caption);
		if (url != null) {
			mCaption.setMovementMethod(LinkMovementMethod.getInstance());
			mCaption.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					getContext().startActivity(intent);
				}
			});
		}
	}

	public CharSequence getCaption() {
		return mCaption.getText();
	}

	public void setSVG(int rawResId) {
		mSvg.setSVG(rawResId);
	}

	public void setActionButton(int resId, OnClickListener listener) {
		mActionButton.setVisibility(View.VISIBLE);
		mActionButton.setText(resId);
		mActionButton.setOnClickListener(listener);
	}

	public void clearActionButton() {
		mActionButton.setVisibility(View.GONE);
		mActionButton.setOnClickListener(null);
	}

}
