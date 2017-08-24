package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;

/**
 * This widget "sticks" to the top or bottom of its parent when the parent scrolls. It employs
 * setTranslateY() on itself to make sure the whole view is visible on the screen at all times.
 * <p/>
 * <pre>
 * &lt;com.expedia.bookings.widget.StickyRelativeLayout
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content" &gt;
 *
 *     &lt;TextView
 *         android:id="@+id/my_text"
 *         android:layout_width="wrap_content"
 *         android:layout_height="48dp"
 *         android:background="#e54e4e52"
 *         android:text="@string/important_header_text" /&gt;
 * &lt;/com.expedia.bookings.widget.StickyRelativeLayout&gt;
 * </pre>
 * <p/>
 *
 * @author doug@mobiata.com
 */
public class StickyRelativeLayout extends RelativeLayout {

	// Do not re-allocate these all the time
	private final int[] mLocation = new int[2];
	private final Rect mVisible = new Rect();

	public StickyRelativeLayout(Context context) {
		super(context);
		init(context, null, 0);
	}

	public StickyRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public StickyRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		hook();
		getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		hook();
		getLocationOnScreen(mLocation);
		stick();
	}

	/**
	 * Move this container via setTranslationY() if it's not completely visible on the screen.
	 * Does nothing if this view is not enabled.
	 */
	public void stick() {
		if (!isEnabled()) {
			return;
		}

		View parent = (View) getParent();
		parent.getLocalVisibleRect(mVisible);

		if (mVisible.top > getTop()) {
			setTranslationY(mVisible.top - getTop());
		}
		else if (mVisible.bottom < getBottom()) {
			setTranslationY(mVisible.bottom - getBottom());
		}
		else {
			setTranslationY(0f);
		}
	}

	private final ScrollView.OnScrollListener mOnScrollListener = new ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			getLocationOnScreen(mLocation);
			stick();
		}
	};

	// TODO: find a better way to be notified when the position of this view changes.
	private final OnGlobalLayoutListener mLayoutListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			// TODO: this is called many times per second. why?
			int old = mLocation[1];
			getLocationOnScreen(mLocation);
			if (mLocation[1] != old) {
				stick();
			}
		}
	};

	/**
	 * This function hooks into all ancestor onScrollListener's to grab a notification for
	 * when the scroll changes, so that we're sure to handle it.
	 */
	private void hook() {
		ViewParent parent = this.getParent();
		while (parent != null) {
			if (parent instanceof ScrollView) {
				((ScrollView) parent).addOnScrollListener(mOnScrollListener);
			}
			parent = parent.getParent();
		}
	}
}
