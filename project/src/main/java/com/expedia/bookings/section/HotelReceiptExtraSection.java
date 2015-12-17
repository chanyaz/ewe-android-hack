package com.expedia.bookings.section;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

public class HotelReceiptExtraSection extends RelativeLayout {

	private TextView mTitle;
	private TextView mPrice;

	public HotelReceiptExtraSection(Context context) {
		super(context);
	}

	public HotelReceiptExtraSection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTitle = Ui.findView(this, R.id.price_title);
		mPrice = Ui.findView(this, R.id.price_text_view);
	}

	public void bind(CharSequence title, String rateString) {
		if (!TextUtils.isEmpty(title)) {
			mTitle.setText(title);
		}
		else {
			throw new RuntimeException("You probably shouldn't be binding a HotelReceiptExtraSection without a title.");
		}
		if (!TextUtils.isEmpty(title)) {
			mPrice.setText(rateString);
			mPrice.setVisibility(View.VISIBLE);
		}
		else {
			mPrice.setVisibility(View.GONE);
		}
	}




}
