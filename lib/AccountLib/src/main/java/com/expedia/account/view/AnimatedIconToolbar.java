package com.expedia.account.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.expedia.account.R;
import com.expedia.account.graphics.ArrowXDrawable;

public class AnimatedIconToolbar extends Toolbar {

	private static int DEFAULT_STROKE_COLOR = Color.WHITE;

	private ArrowXDrawable mArrowXDrawable;

	public AnimatedIconToolbar(Context context) {
		super(context);
		init(context, null);
	}

	public AnimatedIconToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public AnimatedIconToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@SuppressLint("CustomViewStyleable")
	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.acct__AnimatedIconToolbar, 0, 0);
		int color = ta.getColor(R.styleable.acct__AnimatedIconToolbar_acct__ait_strokeColor, DEFAULT_STROKE_COLOR);
		ta.recycle();

		float upButtonSizePx = 24 * getResources().getDisplayMetrics().density;
		mArrowXDrawable = new ArrowXDrawable(upButtonSizePx);
		mArrowXDrawable.setStrokeColor(color);
		mArrowXDrawable.setParameter(1);
		mArrowXDrawable.setFlip(true);
		setNavigationIcon(mArrowXDrawable);
	}

	public void styleizeFromAccountView(TypedArray a) {
		setStrokeColor(a.getColor(R.styleable.acct__AccountView_acct__toolbar_icon_color,
			getResources().getColor(R.color.acct__default_toolbar_icon_color)));
	}

	public void setStrokeColor(int color) {
		mArrowXDrawable.setStrokeColor(color);
	}

	public void showNavigationIconAsX() {
		if (mArrowXDrawable.getParameter() < 1) {
			mArrowXDrawable.setFlip(false);
			ObjectAnimator anim = ObjectAnimator.ofFloat(mArrowXDrawable, ArrowXDrawable.PARAMETER, 1);
			anim.setInterpolator(new DecelerateInterpolator());
			anim.setDuration(300);
			anim.start();
		}
		setNavigationContentDescription(R.string.acct__Toolbar_nav_close_icon_cont_desc);
	}

	public void showNavigationIconAsBack() {
		if (mArrowXDrawable.getParameter() > 0) {
			mArrowXDrawable.setFlip(true);
			ObjectAnimator anim = ObjectAnimator.ofFloat(mArrowXDrawable, ArrowXDrawable.PARAMETER, 0);
			anim.setInterpolator(new DecelerateInterpolator());
			anim.setDuration(300);
			anim.start();
		}
		setNavigationContentDescription(R.string.acct__Toolbar_nav_back_icon_cont_desc);
	}

}
