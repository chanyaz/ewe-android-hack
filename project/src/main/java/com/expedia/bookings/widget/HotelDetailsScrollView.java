package com.expedia.bookings.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import com.expedia.bookings.R;

/**
 * This class implements a parallax-like scrolling effect specifically for
 * the phone UI Hotel Details screen.
 *
 * @author doug@mobiata.com
 *
 */
public class HotelDetailsScrollView extends GalleryScrollView {

	public interface HotelDetailsMiniMapClickedListener {
		public void onHotelDetailsMiniMapClicked();
	}

	ViewGroup mMapOuterContainer;
	TouchableFrameLayout mMapContainer;
	View mGalleryContainer;
	RecyclerGallery mGallery;
	AlphaImageView mVipAccessIcon;

	private int mGalleryHeight = 0;
	private int mInitialScrollTop = 0;
	private int mIntroOffset = 0;
	private boolean mHasBeenTouched = false;

	SegmentedLinearInterpolator mIGalleryScroll, mIGalleryScale, mIMapScroll;

	private HotelDetailsMiniMapClickedListener mListener;

	public HotelDetailsScrollView(Context context) {
		this(context, null);
	}

	public HotelDetailsScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
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

		initVipAccessIcon();
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
			mGallery = (RecyclerGallery) findViewById(R.id.images_gallery);
		}

		ViewGroup.LayoutParams lp = mGalleryContainer.getLayoutParams();
		lp.height = h;
		mGalleryContainer.setLayoutParams(lp);

		mInitialScrollTop = h - mGalleryHeight;
	}

	public void setHotelDetailsMiniMapClickedListener(HotelDetailsMiniMapClickedListener listener) {
		mListener = listener;
	}

	private void initMap() {
		mMapOuterContainer = (ViewGroup) findViewById(R.id.hotel_details_map_fragment_outer_container);
		if (mMapOuterContainer != null) {
			mMapContainer = (TouchableFrameLayout) mMapOuterContainer.findViewById(R.id.hotel_details_map_fragment_container);
			mMapContainer.setBlockNewEventsEnabled(true);
			mMapContainer.setTouchListener(new TouchableFrameLayout.TouchListener() {
				@Override
				public void onInterceptTouch(MotionEvent ev) {
					// ignore
				}

				@Override
				public void onTouch(MotionEvent ev) {
					if (mListener != null) {
						if (ev.getAction() == MotionEvent.ACTION_UP) {
							mListener.onHotelDetailsMiniMapClicked();
						}
					}
				}
			});
		}
	}

	private void initVipAccessIcon() {
		if (mVipAccessIcon == null) {
			mVipAccessIcon = (AlphaImageView) findViewById(R.id.vip_badge);
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		doCounterscroll();

		if (mVipAccessIcon != null) {
			int alpha = 255;
			int totalHeight = mGalleryHeight - mIntroOffset;
			if (t < 0) {
				// Clamp it in case of overscroll
				alpha = 0;
			}
			else if (t < totalHeight) {
				float percentage = t / ((float) (totalHeight));
				alpha = (int) (percentage * 255.0f);
			}
			mVipAccessIcon.setDrawAlpha(alpha);

			// #1791: Disable the icon clicks when it's not visible
			mVipAccessIcon.setEnabled(alpha != 0);
		}
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
		if (mMapOuterContainer != null) {
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

		mGalleryContainer.setPivotX(getWidth() / 2);
		mGalleryContainer.setPivotY(counterscroll + mGalleryHeight / 2);
		mGalleryContainer.setScaleX(scale);
		mGalleryContainer.setScaleY(scale);

		mGalleryContainer.scrollTo(0, -counterscroll);
	}

	private void mapCounterscroll(int parentScroll) {
		// Setup interpolator for Map counterscroll (if needed)
		if (mIMapScroll == null) {
			int mapHeight = mMapContainer.getHeight();
			int screenHeight = this.getHeight();
			int mapTop = mMapOuterContainer.getTop() + mMapOuterContainer.getPaddingTop();
			int mapBottom = mMapOuterContainer.getBottom() - mMapOuterContainer.getPaddingBottom();
			int frameHeight = mapBottom - mapTop;

			int mapTopScreenBottom = mapTop - screenHeight; // when map top is at screen bottom
			PointF p1 = new PointF(mapTopScreenBottom, mapHeight / 2);

			int mapBottomScreenBottom = mapBottom - screenHeight; // when map bottom is at screen bottom
			PointF p2 = new PointF(mapBottomScreenBottom, mapHeight - frameHeight);

			int mapTopScreenTop = mapTop; // when map top is at screen top
			PointF p3 = new PointF(mapTopScreenTop, 0);

			int mapBottomScreenTop = mapBottom; // when map bottom is at screen top
			PointF p4 = new PointF(mapBottomScreenTop, mapHeight / 2 - frameHeight);

			mIMapScroll = new SegmentedLinearInterpolator(p1, p2, p3, p4);
		}

		int counterscroll = (int) mIMapScroll.get(parentScroll);

		mMapOuterContainer.scrollTo(0, counterscroll);
	}

	@Override
	protected OverScroller initScroller() {
		return new FullscreenGalleryOverScroller(this);
	}

	public int getInitialScrollTop() {
		return mInitialScrollTop;
	}

}
