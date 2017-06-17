package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoadingOverlayWidget extends LinearLayout {

	public LoadingOverlayWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.loading_overlay, this);
	}

	//@InjectView(R.id.overlay_loading_text)
	View loadingText;

	//@InjectView(R.id.overlay_title_container)
	View overlayTitleContainer;

	//@InjectView(R.id.loading_overlay)
	View loadingOverlay;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		overlayTitleContainer.getLayoutParams().height += statusBarHeight;
		overlayTitleContainer.setPadding(0, statusBarHeight, 0, 0);
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

	public void setBackground(int id) {
		overlayTitleContainer.setBackgroundResource(id);
	}

	public void setBackgroundColor(@ColorInt int color) {
		overlayTitleContainer.setBackgroundColor(color);
	}

	public void setBackgroundAttr(Drawable id) {
		overlayTitleContainer.setBackground(id);
	}
}
