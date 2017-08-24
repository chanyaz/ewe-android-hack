package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mobiata.android.Log;

import static android.view.ViewGroup.LayoutParams;

/**
 * This View copies the size of another View.
 * <p/>
 * It was developed to address the following situation:
 * You have a view that overlays a scroll view. When the overlay is visible we can not scroll to the content
 * behind the overlay. If we stick one of these in the scrollview, and call mimicViewSize() on the overlay,
 * the overlays space will be added to the scroll view, and we can bring our content into view. Hooray!
 */
public class SizeCopyView extends View {

	private boolean mVertical;//Do we copy the vertical size
	private boolean mHorizontal;//Do we copy the horizontal size
	private boolean mZeroOnHidden;//If true, we set this instance to be size = 0 if the supplied view is not visible.
	private WeakReference<View> mViewRef;//WeakRef to the view we want to copy the size of.

	//Accounting vars so we aren't updating layout params when nothing changes.
	private int mLastVis;
	private float mLastAlpha;
	private int mLastHeight;
	private int mLastWidth;

	public SizeCopyView(Context context) {
		super(context);
	}

	public SizeCopyView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public SizeCopyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		reset();
	}

	private final ViewTreeObserver.OnPreDrawListener mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
		@Override
		public boolean onPreDraw() {
			if (mViewRef != null && mViewRef.get() != null && viewHasChanged(mViewRef.get())) {
				updateViewSize(mViewRef.get());
			}
			return true;
		}
	};

	/**
	 * Call this function to make this instance of SizeCopyView mimic the size of the view argument.
	 * <p/>
	 * Note: The supplied view is stored in a weakreference.
	 *
	 * @param view         - the view to mimic the size of
	 * @param vertical     - should we mimic the vertical size
	 * @param horizontal   - should we mimic the horizontal size
	 * @param zeroOnHidden - if the provided view is invisible (visibility or alpha), should we set this view to 0x0px?
	 */
	public void mimicViewSize(View view, boolean vertical, boolean horizontal, boolean zeroOnHidden) {
		updateViewInfo(view);
		mViewRef = new WeakReference<View>(view);
		mVertical = vertical;
		mHorizontal = horizontal;
		mZeroOnHidden = zeroOnHidden;
		view.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
	}

	/**
	 * Reset this view to no longer track an earlier supplied view size
	 */
	public void reset() {
		if (mViewRef != null && mViewRef.get() != null) {
			mViewRef.get().getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
		}
		mViewRef = null;
		mLastVis = 0;
		mLastAlpha = 0;
		mLastWidth = 0;
		mLastHeight = 0;
	}

	private void updateViewSize(View view) {
		if (view != null) {
			LayoutParams params = getLayoutParams();
			if (params == null) {
				params = new LayoutParams(0, 0);
			}
			if (mZeroOnHidden && (view.getVisibility() != View.VISIBLE || view.getAlpha() == 0)) {
				if (mVertical) {
					params.height = 0;
				}
				if (mHorizontal) {
					params.width = 0;
				}
			}
			else {
				if (mVertical) {
					params.height = view.getHeight();
				}
				if (mHorizontal) {
					params.width = view.getWidth();
				}
			}
			Log.d("SizeCopyView - setLayoutParams params.height:" + params.height + " params.width:" + params.width);
			setLayoutParams(params);
		}
	}

	private boolean viewHasChanged(View view) {
		if (mLastVis != view.getVisibility() || mLastAlpha != view.getAlpha() || mLastHeight != view.getHeight()
			|| mLastWidth != view.getWidth()) {
			updateViewInfo(view);
			return true;
		}
		return false;
	}

	private void updateViewInfo(View view) {
		mLastVis = view.getVisibility();
		mLastAlpha = view.getAlpha();
		mLastHeight = view.getHeight();
		mLastWidth = view.getWidth();
	}
}
