package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.utils.TouchControlHelper;
import com.expedia.bookings.utils.Ui;

/**
 * This is a specialized StickyRelativeLayout designed specifically for ETP
 * placeholder in material Hotel Details.
 */
public class HotelEtpStickyHeaderLayout extends StickyRelativeLayout {

	private Rect mVisible = new Rect();
	private View mContainer;
	private View dropShadow;

	private TouchControlHelper mTouchHelper = new TouchControlHelper();

	public HotelEtpStickyHeaderLayout(Context context) {
		super(context);
		init();
	}

	public HotelEtpStickyHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HotelEtpStickyHeaderLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mTouchHelper.setConsumeTouch(true);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Pair<Boolean, Boolean> touchControl = mTouchHelper.onInterceptTouchEvent(ev);
		if (touchControl.first) {
			return touchControl.second;
		}
		else {
			return super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Pair<Boolean, Boolean> touchControl = mTouchHelper.onTouchEvent(ev);
		if (touchControl.first) {
			return touchControl.second;
		}
		else {
			return super.onTouchEvent(ev);
		}
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		mContainer = Ui.findView(this, R.id.etp_placeholder);
	}

	@Override
	public void stick() {
		if (!isEnabled() || getVisibility() == GONE
			|| Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelNoStickyETP)) {
			return;
		}

		View parent = (View) getParent();
		parent.getLocalVisibleRect(mVisible);

		if (dropShadow == null) {
			dropShadow = Ui.findView(parent, R.id.pay_later_drop_shadow);
		}

		float offset = Math
			.max(0, mVisible.top - getTop() + Ui.getToolbarSize(getContext()) + Ui.getStatusBarHeight(getContext()));
		mContainer.setTranslationY(offset);
		dropShadow.setTranslationY(offset);

		if (offset != 0) {
			dropShadow.setVisibility(View.VISIBLE);
		}
		else {
			dropShadow.setVisibility(View.GONE);
		}
	}

}
