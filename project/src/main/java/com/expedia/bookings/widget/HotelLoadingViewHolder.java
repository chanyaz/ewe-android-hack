package com.expedia.bookings.widget;

import android.view.View;
import android.widget.LinearLayout;
import com.expedia.bookings.R;
import butterknife.InjectView;

public class HotelLoadingViewHolder extends LoadingViewHolder {

	//@InjectView(R.id.text_layout)
	protected LinearLayout textLayoutView;

	public HotelLoadingViewHolder(View view) {
		super(view);
	}

	public void resizeHeight(int imageViewHeight, int textLayoutHeight) {
		backgroundImageView.getLayoutParams().height = imageViewHeight;
		textLayoutView.getLayoutParams().height = textLayoutHeight;
	}
}
