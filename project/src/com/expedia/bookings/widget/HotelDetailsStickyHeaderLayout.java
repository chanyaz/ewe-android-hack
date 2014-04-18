package com.expedia.bookings.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.ColorAvgUtils;
import com.expedia.bookings.bitmaps.ColorScheme;
import com.expedia.bookings.utils.Ui;

/**
 * This is a specialized StickyRelativeLayout designed specifically for Tablet Hotel Details. Once
 * the view hits the top of its parent when scrolled, additional animations are performed:
 * <p/>
 * 1. The background changes from 90% opaque to 100% opaque
 * 2. The hotel rating and user reviews bar is faded out from 100% to 0%
 * 3. The apparent height of the bar decreases
 * <p/>
 * This is highly dependent on the layout in fragment_tablet_hotel_details.xml. Change that, and
 * this will probably croak.
 */
public class HotelDetailsStickyHeaderLayout extends StickyRelativeLayout {

	private Rect mVisible = new Rect();

	private float mCompactHeaderHeight;
	private ColorDrawable mDominantColorBackground;

	private View mHotelHeaderImage;
	private View mSaleTextView;
	private View mVipBadge;
	private View mStarRatingContainer;
	private View mGradientMask;
	private View mDominantMask;
	private View mHotelName;

	public HotelDetailsStickyHeaderLayout(Context context) {
		super(context);
		init();
	}

	public HotelDetailsStickyHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HotelDetailsStickyHeaderLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		Resources res = getResources();
		mCompactHeaderHeight = res.getDimension(R.dimen.tablet_details_compact_header_height);

		resetDominantColor();
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mHotelHeaderImage = Ui.findView(this, R.id.hotel_header_image);
		mSaleTextView = Ui.findView(this, R.id.sale_text_view);
		mVipBadge = Ui.findView(this, R.id.vip_badge);
		mStarRatingContainer = Ui.findView(this, R.id.star_rating_container);
		mHotelName = Ui.findView(this, R.id.hotel_header_hotel_name);
		mGradientMask = Ui.findView(this, R.id.gradient_header_mask);
		mDominantMask = Ui.findView(this, R.id.dominant_color_header_mask);
		mDominantMask.setBackgroundDrawable(mDominantColorBackground);
	}

	@Override
	@TargetApi(11)
	public void stick() {
		if (!isEnabled()) {
			return;
		}

		View parent = (View) getParent();
		parent.getLocalVisibleRect(mVisible);

		float offset = Math.max(0f, mVisible.top - getTop());
		int height = getHeight();

		// The height difference between when this view is fully expanded,
		// and when it is maximally compacted
		float headerHeightDiff = height - mCompactHeaderHeight;

		// extra: the additional amount, beyond the top of the parent, which
		// this view should scroll. The crux of this whole object.
		float extra = Math.min(offset, headerHeightDiff);

		setTranslationY(offset - extra);

		float extraPct = extra / headerHeightDiff;

		mDominantColorBackground.setAlpha((int) (extraPct * 229f));

		mHotelHeaderImage.setTranslationY(extra / 2);

		mGradientMask.setAlpha(1 - extraPct);

		float nameOffset = height - mCompactHeaderHeight / 2 - mHotelName.getHeight() / 2 - mHotelName.getTop();
		mHotelName.setTranslationY(extraPct * nameOffset);

		mStarRatingContainer.setAlpha(1 - extraPct);

		mSaleTextView.setTranslationY(extra);
		mSaleTextView.setAlpha(1 - extraPct);

		mVipBadge.setTranslationY(extra);
		mVipBadge.setAlpha(1 - extraPct);
	}

	public void setDominantColor(ColorScheme colorScheme) {
		int colorDarkened = ColorAvgUtils.darken(colorScheme.primaryAccent, 0.4f);
		setDominantColor(colorDarkened);
	}

	public void resetDominantColor() {
		setDominantColor(getResources().getColor(R.color.hotel_details_sticky_header_background));
	}

	@TargetApi(11)
	private void setDominantColor(int color) {
		if (mDominantColorBackground == null) {
			mDominantColorBackground = new ColorDrawable();
			mDominantColorBackground.setAlpha(0);
		}
		int alpha = mDominantColorBackground.getAlpha();
		int overlayWithAlpha = 0x01000000 * alpha | 0x00ffffff & color;
		mDominantColorBackground.setColor(overlayWithAlpha);
		mDominantColorBackground.invalidateSelf();
	}

}
