package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
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

	ViewGroup mGalleryScrollView;
	ViewGroup mMapScrollView;

	int mInitialScrollTop = 0;

	AnimatorProxy mGalleryAnimatorProxy;
	ValueAnimator mAnimator;

	public HotelDetailsScrollView(Context context) {
		this(context, null);
	}

	public HotelDetailsScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.scrollViewStyle);
	}

	public HotelDetailsScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		int screenHeight = b - t;
		int galleryHeight = getResources().getDimensionPixelSize(R.dimen.gallery_size);
		mInitialScrollTop = screenHeight - galleryHeight;

		scrollTo(0, mInitialScrollTop);
		doCounterscroll();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// Gallery Layout
		int galleryHeight = getResources().getDimensionPixelSize(R.dimen.gallery_size);

		mGalleryScrollView = (ViewGroup) findViewById(R.id.gallery_scroll_view);
		View galleryContainer = findViewById(R.id.hotel_details_mini_gallery_fragment_container);
		mGalleryAnimatorProxy = AnimatorProxy.wrap(galleryContainer);

		HotelDetailsGallery gallery = (HotelDetailsGallery) findViewById(R.id.images_gallery);
		gallery.setInvalidateView(mGalleryScrollView);

		ViewGroup.LayoutParams lp = mGalleryScrollView.getLayoutParams();
		lp.height = h;
		mGalleryScrollView.setLayoutParams(lp);

		int paddingTop = (int) (h - galleryHeight / 2.0);
		int paddingBottom = (int) ((h - galleryHeight));
		findViewById(R.id.hotel_details_mini_gallery_fragment_container).setPadding(0, paddingTop, 0, paddingBottom);

		mGalleryAnimatorProxy.setPivotY(paddingTop + galleryHeight / 2);
		mGalleryAnimatorProxy.setPivotX(w / 2);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		doCounterscroll();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean result = super.onTouchEvent(ev);

		if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
			if (isScrollerFinished()) {
				snapGallery();
			}
		}

		return result;
	}

	private void doCounterscroll() {
		int t = getScrollY();
		galleryCounterscroll(t);
		mapCounterscroll(t);
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

		mAnimator = ObjectAnimator.ofInt(this, "scrollY", from, to);
		mAnimator.start();
	}

	private void galleryCounterscroll(int parentScroll) {
		// Gallery Layout
		int galleryHeight = getResources().getDimensionPixelSize(R.dimen.gallery_size);
		int screenHeight = getHeight();
		int availableHeight = screenHeight - parentScroll;

		// at x <= galleryHeight, scale = 1.0
		float scale = 1f;
		int counterscroll = availableHeight / 2;

		if (availableHeight > galleryHeight) {

			// at x = galleryHeight, scale = 1.0
			// at x = screenHeight, scale = 0.7 * scaledHeight / galleryHeight

			float x1 = galleryHeight;
			float x2 = screenHeight;
			float y1 = 1f;
			float y2 = 0.7f * screenHeight / galleryHeight;

			// Linear interpolation
			float x = screenHeight - parentScroll;
			float pct = (x - x1) / (x2 - x1);
			scale = y1 + (y2 - y1) * pct;

			float y3 = counterscroll;
			float y4 = counterscroll - getResources().getDimensionPixelSize(R.dimen.hotel_details_intro_offset) / 2;
			counterscroll = (int) (y3 + (y4 - y3) * pct);
		}

		mGalleryAnimatorProxy.setScaleX(scale);
		mGalleryAnimatorProxy.setScaleY(scale);
		mGalleryScrollView.scrollTo(0, counterscroll);
	}

	private void mapCounterscroll(int parentScroll) {

		if (mMapScrollView == null) {
			mMapScrollView = (ViewGroup) findViewById(R.id.map_scroll_view);
		}

		// The portion of the map that we want visible as long as possible (presumably the middle part)
		int criticalHeight = 40;
		int criticalTop = (int) (mMapScrollView.getChildAt(0).getHeight() / 2.0 - criticalHeight / 2.0);

		// hitRect = the position of this view within the mHostLayout
		Rect hitRect = new Rect();
		mMapScrollView.getHitRect(hitRect);

		// Compute the range where the view is (at least partially) visible
		int maxVisibleScroll = hitRect.bottom;
		int minVisibleScroll = hitRect.top - getHeight();

		if (parentScroll < minVisibleScroll + criticalHeight || parentScroll > maxVisibleScroll - criticalHeight) {
			// Child isn't visible, no need to bother with it
			return;
		}

		float pct = ((float) parentScroll - minVisibleScroll) / (maxVisibleScroll - minVisibleScroll);

		// Scroll the child so that desiredy is located pct% down

		int scrollTo = criticalTop - (int) (pct * (hitRect.height() - criticalHeight));

		mMapScrollView.scrollTo(0, scrollTo);
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
}
