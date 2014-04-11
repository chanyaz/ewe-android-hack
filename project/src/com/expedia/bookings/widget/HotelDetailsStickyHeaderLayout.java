package com.expedia.bookings.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

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
	private ColorDrawable mBackground;

	private View mAddToTripButton;
	private View mHotelNameRatingContainer;
	private View mRatingContainer;

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
		mBackground = new ColorDrawable(res.getColor(R.color.hotel_details_sticky_header_background));
		setBackgroundDrawable(mBackground);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mAddToTripButton = Ui.findView(this, R.id.button_add_to_trip);
		mHotelNameRatingContainer = Ui.findView(this, R.id.hotel_name_rating_container);
		mRatingContainer = Ui.findView(this, R.id.rating_container);
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

		float headerHeightDiff = getHeight() - mCompactHeaderHeight;

		// extra: the additional amount, beyond the top of the parent, which
		// this view should scroll. The crux of this whole object.
		float extra = Math.min(headerHeightDiff, offset / 2);

		float extraPct = extra / headerHeightDiff;

		mBackground.setAlpha(229 + (int) (extraPct * 26f));

		setTranslationY(offset - extra);

		mAddToTripButton.setTranslationY(extra / 2);

		mHotelNameRatingContainer.setTranslationY(extra);

		mRatingContainer.setTranslationY(-extra / 2);
		mRatingContainer.setAlpha(1f - extraPct);
	}

}
