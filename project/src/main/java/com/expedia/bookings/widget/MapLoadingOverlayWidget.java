package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AnimUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MapLoadingOverlayWidget extends FrameLayout {

	public MapLoadingOverlayWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.map_loading_overlay, this);
	}

	@InjectView(R.id.overlay_loading_text)
	View loadingText;

	@InjectView(R.id.loading_overlay)
	View loadingOverlay;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void animate(boolean forward) {
		if (forward) {
			AnimUtils.slideUp(loadingText);
			AnimUtils.fadeIn(loadingOverlay);
		}
		else {
			AnimUtils.slideDown(loadingText);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
}
