package com.expedia.account.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by doug on 5/29/15.
 */
public class KeyboardObservingFrameLayout extends FrameLayout {
	public KeyboardObservingFrameLayout(Context context) {
		super(context);
	}

	public KeyboardObservingFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KeyboardObservingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(21)
	public KeyboardObservingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		setupObservantMover();
	}

	public View getContent() {
		return getChildAt(0);
	}

	/**
	 * Sets the layout_marginBottom on this FrameLayout. Use caution
	 * when calling this, as it will request a layout pass.
	 *
	 * @param to
	 */
	public void setBottomMargin(int to) {
		MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
		if (mlp.bottomMargin != to) {
			mlp.bottomMargin = to;
			requestLayout();
		}
	}

	public int getBottomMargin() {
		MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
		return mlp.bottomMargin;
	}

	///////////////////////////////////////////////////////////////////////////
	// Detect whether the keyboard is visible
	///////////////////////////////////////////////////////////////////////////

	private boolean mIsFirstMeasure = true;
	private boolean mIsKeyboardVisible = false;

	private Rect mDecorViewRect = new Rect();
	private Point mDisplaySize = new Point();

	private int mKeyboardHeight;

	// Content height = height of the display excluding status bar or nav bar.
	// But mDisplaySize already doesn't include the nav bar height.
	// So why is Status Bar height included in the display height
	// but Nav Bar isn't? ¯\_(ツ)_/¯
	private int getContentHeight() {
		Activity activity = (Activity) getContext();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(mDecorViewRect);
		int statusBarHeight = mDecorViewRect.top;
		activity.getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
		int screenHeight = mDisplaySize.y;
		return screenHeight - statusBarHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int viewHeight = MeasureSpec.getSize(heightMeasureSpec) + getBottomMargin();
		int contentHeight = getContentHeight();
		int predictedKeyboardHeight = contentHeight - viewHeight;
		boolean isKeyboardVisible = predictedKeyboardHeight > 128;
		if (isKeyboardVisible) {
			mKeyboardHeight = predictedKeyboardHeight;
		}

		if (mIsFirstMeasure || isKeyboardVisible != mIsKeyboardVisible) {
			mIsKeyboardVisible = isKeyboardVisible;
			mKeyboardVisibilityChanged = true;
			invalidate();
		}

		mIsFirstMeasure = false;

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public final boolean isKeyboardVisible() {
		return mIsKeyboardVisible;
	}

	public final int getKeyboardHeight() {
		return mKeyboardHeight;
	}

	/**
	 * Implement this in children
	 * @param isKeyboardVisible
	 */
	public void onKeyboardVisibilityChanged(boolean isKeyboardVisible) {
	}

	///////////////////////////////////////////////////////////////////////////
	// This observes changes in the position of the observee, and if the position
	// changes, this will animate its movement from one position to the next
	///////////////////////////////////////////////////////////////////////////

	private ViewTreeObserver.OnPreDrawListener mPreDrawListener;

	private boolean mKeyboardVisibilityChanged = false;

	private void setupObservantMover() {
		if (mPreDrawListener != null) {
			return;
		}
		mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
			private int mLastTop = -1;

			@Override
			public boolean onPreDraw() {
				int top = getContent().getTop();
				boolean topChanged = mLastTop != top && mLastTop != -1;

				if (topChanged) {
					if (mKeyboardVisibilityChanged) {
						// We want to carefully call this during onPreDraw
						// (and then skip this drawing pass), so that we
						// can avoid extra janky frames
						onKeyboardVisibilityChanged(mIsKeyboardVisible);
						mKeyboardVisibilityChanged = false;
						return false;
					}
					// Only bother with animation if the view is visible
					if (getVisibility() == View.VISIBLE) {
						View content = getContent();
						content.setTranslationY(mLastTop - top);
						content.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
						mLastTop = top;
						return false;
					}
				}
				mLastTop = top;
				return true;
			}
		};
		getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
	}

}
