package com.expedia.bookings.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.mobiata.android.util.AndroidUtils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;

/**
 * This class implements a parallax-like scrolling effect specifically for 
 * the phone UI Hotel Details screen.
 * 
 * @author doug@mobiata.com
 *
 */
public class HotelDetailsScrollView extends CustomScrollerScrollView {
	private static final String TAG = HotelDetailsScrollView.class.getSimpleName();

	ViewGroup mMapScrollView;
	View mGalleryContainer;
	HotelDetailsGallery mGallery;

	private int mGalleryHeight = 0;
	private int mInitialScrollTop = 0;
	private int mIntroOffset = 0;
	private boolean mHasBeenTouched = false;

	//TODO: this won't be needed once minSdk >= 11
	AnimatorProxy mGalleryAnimatorProxy;

	ValueAnimator mAnimator;

	SegmentedLinearInterpolator mIGalleryScroll, mIGalleryScale, mIMapScroll;

	public HotelDetailsScrollView(Context context) {
		this(context, null);
	}

	public HotelDetailsScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.scrollViewStyle);
	}

	public HotelDetailsScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mIntroOffset = getResources().getDimensionPixelSize(R.dimen.hotel_details_intro_offset);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		mIGalleryScroll = null;
		mIGalleryScale = null;
		mIMapScroll = null;

		if (hasGallery()) {
			mGalleryHeight = getResources().getDimensionPixelSize(R.dimen.gallery_size);
			int h = b - t;
			mInitialScrollTop = h - mGalleryHeight;
		}

		if (!mHasBeenTouched) {
			scrollTo(0, mInitialScrollTop);
			doCounterscroll();
		}
	}

	@TargetApi(11)
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		initGallery(h);
		initMap();
	}

	/**
	 * A side effect of this function is that mGalleryContainer gets initialized
	 * if the gallery view is present.
	 * @return
	 */
	private boolean hasGallery() {
		if (mGalleryContainer == null) {
			mGalleryContainer = findViewById(R.id.hotel_details_mini_gallery_fragment_container);
		}
		return mGalleryContainer != null;
	}

	private void initGallery(int h) {
		if (!hasGallery()) {
			return;
		}
		if (mGallery == null) {
			mGallery = (HotelDetailsGallery) findViewById(R.id.images_gallery);
			mGallery.setInvalidateView(mGalleryContainer);
		}
		if (mGalleryAnimatorProxy == null && AndroidUtils.getSdkVersion() < 11) {
			mGalleryAnimatorProxy = AnimatorProxy.wrap(mGalleryContainer);
		}

		ViewGroup.LayoutParams lp = mGalleryContainer.getLayoutParams();
		lp.height = h;
		mGalleryContainer.setLayoutParams(lp);

		mInitialScrollTop = h - mGalleryHeight;
	}

	private void initMap() {
		mMapScrollView = (ViewGroup) findViewById(R.id.hotel_details_map_fragment_container);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		doCounterscroll();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean result = super.onTouchEvent(ev);

		mHasBeenTouched = true;

		if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
			if (isScrollerFinished()) {
				snapGallery();
			}
		}

		return result;
	}

	private void doCounterscroll() {
		int t = getScrollY();
		if (mGalleryContainer != null) {
			galleryCounterscroll(t);
		}
		if (mMapScrollView != null) {
			mapCounterscroll(t);
		}
	}

	public void snapGallery() {
		int from = getScrollY();
		if (from < mInitialScrollTop) {
			int threshold = getHeight() / 3;
			int to = from < threshold ? 0 : mInitialScrollTop;
			animateScrollY(from, to);
		}
	}

	public void toggleFullScreenGallery() {
		int from = getScrollY();
		int to = from != 0 ? 0 : mInitialScrollTop;
		animateScrollY(from, to);
	}

	private void animateScrollY(int from, int to) {
		if (mAnimator != null && mAnimator.isRunning()) {
			return;
		}
		if (from == to) {
			return;
		}

		mAnimator = ObjectAnimator.ofInt(this, "scrollY", from, to).setDuration(200);
		mAnimator.start();
	}

	@TargetApi(11)
	private void galleryCounterscroll(int parentScroll) {
		// Setup interpolator for Gallery counterscroll (if needed)
		if (mIGalleryScroll == null) {
			int screenHeight = getHeight();
			PointF p1 = new PointF(0, screenHeight - mGalleryHeight / 2);
			PointF p2 = new PointF(mGalleryHeight, screenHeight - mGalleryHeight);
			PointF p3 = new PointF(screenHeight, (screenHeight - mGalleryHeight + mIntroOffset) / 2);
			mIGalleryScroll = new SegmentedLinearInterpolator(p1, p2, p3);
		}

		// Setup interpolator for Gallery scaling (if needed)
		if (mIGalleryScale == null) {
			int screenHeight = getHeight();
			PointF p4 = new PointF(0, 1);
			PointF p5 = new PointF(mGalleryHeight, 1);
			PointF p6 = new PointF(screenHeight, 0.7f * screenHeight / mGalleryHeight);
			mIGalleryScale = new SegmentedLinearInterpolator(p4, p5, p6);
		}

		// The number of y-pixels available to the gallery
		int availableHeight = getHeight() - parentScroll;

		float scale = mIGalleryScale.get(availableHeight);
		int counterscroll = (int) mIGalleryScroll.get(availableHeight);

		if (AndroidUtils.getSdkVersion() < 11) {
			mGalleryAnimatorProxy.setPivotX(getWidth() / 2);
			mGalleryAnimatorProxy.setPivotY(counterscroll + mGalleryHeight / 2);
			mGalleryAnimatorProxy.setScaleX(scale);
			mGalleryAnimatorProxy.setScaleY(scale);
		}
		else {
			mGalleryContainer.setPivotX(getWidth() / 2);
			mGalleryContainer.setPivotY(counterscroll + mGalleryHeight / 2);
			mGalleryContainer.setScaleX(scale);
			mGalleryContainer.setScaleY(scale);
		}

		mGalleryContainer.scrollTo(0, -counterscroll);
	}

	private void mapCounterscroll(int parentScroll) {
		// Setup interpolator for Map counterscroll (if needed)
		if (mIMapScroll == null) {
			// The middle portion of the map that we want visible as long as possible
			int mapCriticalHeight = getResources().getDimensionPixelSize(R.dimen.mini_map_critical_section);
			int mapHeight = mMapScrollView.findViewById(R.id.mini_map).getHeight();
			int frameHeight = mMapScrollView.getHeight();
			PointF p1 = new PointF(mMapScrollView.getTop() - this.getHeight(), (mapHeight - mapCriticalHeight) / 2);
			PointF p2 = new PointF(mMapScrollView.getBottom(), frameHeight - (mapHeight - mapCriticalHeight) / 2);
			mIMapScroll = new SegmentedLinearInterpolator(p1, p2);
		}

		int counterscroll = (int) mIMapScroll.get(parentScroll);
		mMapScrollView.scrollTo(0, counterscroll);
	}

	@Override
	public Object initScroller() {
		if (AndroidUtils.getSdkVersion() < 9) {
			return new HotelDetailsScroller(this);
		}
		else {
			return new HotelDetailsOverScroller(this);
		}
	}

	public int getInitialScrollTop() {
		return mInitialScrollTop;
	}

	/**
	 * Created with a number of points to be interpreted as a segmented linear function.
	 * Will return the expected y value for any passed x value. If the "x" value passed
	 * in is outside the range of the given x values, then the first and last segments
	 * will be extended to meet that value.
	 *
	 */
	private static class SegmentedLinearInterpolator {
		PointF[] mPoints;

		public SegmentedLinearInterpolator(PointF... points) {
			mPoints = points;
		}

		public float get(float x) {
			for (int i = 0; i <= mPoints.length - 2; i++) {
				float x1 = mPoints[i].x;
				float x2 = mPoints[i + 1].x;
				float y1 = mPoints[i].y;
				float y2 = mPoints[i + 1].y;
				if (x >= x1 && x <= x2 || (i == 0 && x < x1) || (i == mPoints.length - 2 && x > x2)) {
					return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
				}
			}
			return 0f;
		}
	}

}
