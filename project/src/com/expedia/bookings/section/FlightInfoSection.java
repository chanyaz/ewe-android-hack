package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;

public class FlightInfoSection extends LinearLayout {

	private ImageView mIconImageView;
	private TextView mInfoTextView;

	public FlightInfoSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mIconImageView = Ui.findView(this, R.id.icon_image_view);

		mInfoTextView = Ui.findView(this, R.id.info_text_view);
		mInfoTextView.setTypeface(FontCache.getTypeface(Font.ROBOTO_LIGHT));
	}

	public void bind(int iconResId, CharSequence text) {
		mIconImageView.setImageResource(iconResId);

		mInfoTextView.setText(text);
	}

	public static FlightInfoSection inflate(LayoutInflater inflater, ViewGroup container) {
		return (FlightInfoSection) inflater.inflate(R.layout.section_flight_info, container, false);
	}
}
