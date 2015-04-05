package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;

/**
 * Exposes an easy-to-use listener interface for detecting when the children of this layout
 * are clicked. It calculates a content Rect as the union of all child elements. Gaps between
 * children will therefore count as part of the content.
 *
 * Also exposes a converse listener, for detecting a click _outside_ all child content.
 *
 * interesting methods here are:
 *
 * setOnContentClickedListener
 * setOutsideContentClickedListener
 *
 * Created by dmelton on 11/4/14.
 */
public class ContentClickableRelativeLayout extends RelativeLayout {

	private boolean mIsStale;
	private Rect mContentBounds;
	private GestureDetector mDetector;

	private int mContentPadding;

	private View.OnClickListener mContentClickedListener;
	private View.OnClickListener mOutsideContentClickedListener;

	public ContentClickableRelativeLayout(Context context) {
		super(context);
		init(context, null);
	}

	public ContentClickableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ContentClickableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attr) {
		if (attr != null) {
			TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.ContentClickableRelativeLayout, 0, 0);
			mContentPadding = ta.getDimensionPixelSize(R.styleable.ContentClickableRelativeLayout_layoutContentPadding, 0);
			ta.recycle();
		}

		setClickable(true);
		mIsStale = true;
		mContentBounds = new Rect();
		mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				measureContent();
				if (mContentBounds.contains((int)e.getX(), (int)e.getY())) {
					if (mContentClickedListener != null) {
						mContentClickedListener.onClick(ContentClickableRelativeLayout.this);
					}
				}
				else {
					if (mOutsideContentClickedListener != null) {
						mOutsideContentClickedListener.onClick(ContentClickableRelativeLayout.this);
					}
				}
				return true;
			}
		});
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mIsStale = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	public void setOnContentClickedListener(View.OnClickListener listener) {
		mContentClickedListener = listener;
	}

	public void setOutsideContentClickedListener(View.OnClickListener listener) {
		mOutsideContentClickedListener = listener;
	}

	private void measureContent() {
		if (!mIsStale) {
			return;
		}
		mIsStale = false;
		mContentBounds.setEmpty();
		if (getChildCount() == 0) {
			return;
		}
		getChildAt(0).getHitRect(mContentBounds);
		for (int i = 1; i < getChildCount(); i++) {
			View child = getChildAt(i);
			mContentBounds.union(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
		}

		mContentBounds.inset(-mContentPadding, -mContentPadding);
	}
}
