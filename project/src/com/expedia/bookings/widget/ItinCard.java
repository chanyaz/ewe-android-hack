package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public abstract class ItinCard extends RelativeLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// ABSTRACT METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	// Type

	public abstract int getTypeIconResId();

	public abstract Type getType();

	// Header image

	protected abstract String getHeaderImageUrl(TripComponent tripComponent);

	protected abstract String getHeaderText(TripComponent tripComponent);

	// Views

	protected abstract View getTitleView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent);

	protected abstract View getSummaryView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent);

	protected abstract View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent);

	// Action buttons

	protected abstract SummaryButton getSummaryLeftButton();

	protected abstract SummaryButton getSummaryRightButton();

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	protected final class SummaryButton {
		private int mIconResId;
		private String mText;
		private OnClickListener mOnClickListener;

		public SummaryButton(int iconResId, String text, OnClickListener onClickListener) {
			mIconResId = iconResId;
			mText = text;
			mOnClickListener = onClickListener;
		}

		public int getIconResId() {
			return mIconResId;
		}

		public String getText() {
			return mText;
		}

		public OnClickListener getOnClickListener() {
			return mOnClickListener;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ViewGroup mCardLayout;
	private ViewGroup mTitleLayout;
	private ViewGroup mTitleContentLayout;
	private ViewGroup mSummaryLayout;
	private ViewGroup mDetailsLayout;
	private ViewGroup mSummaryButtonLayout;

	private ImageView mItinTypeImageView;
	private ImageView mItinTypeStaticImageView;

	private ScrollView mScrollView;
	private OptimizedImageView mHeaderImageView;
	private TextView mHeaderTextView;

	private TextView mSummaryLeftButton;
	private TextView mSummaryRightButton;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCard(Context context) {
		this(context, null);
	}

	public ItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);

		inflate(context, R.layout.widget_itin_card, this);

		mCardLayout = Ui.findView(this, R.id.card_layout);
		mTitleLayout = Ui.findView(this, R.id.title_layout);
		mTitleContentLayout = Ui.findView(this, R.id.title_content_layout);
		mSummaryLayout = Ui.findView(this, R.id.summary_layout);
		mDetailsLayout = Ui.findView(this, R.id.details_layout);
		mSummaryButtonLayout = Ui.findView(this, R.id.summary_button_layout);

		mItinTypeImageView = Ui.findView(this, R.id.itin_type_image_view);
		mItinTypeStaticImageView = Ui.findView(this, R.id.itin_type_static_image_view);

		mScrollView = Ui.findView(this, R.id.scroll_view);
		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mHeaderTextView = Ui.findView(this, R.id.header_text_view);

		mSummaryLeftButton = Ui.findView(this, R.id.summary_left_button);
		mSummaryRightButton = Ui.findView(this, R.id.summary_right_button);

		mScrollView.setOnScrollListener(mOnScrollListener);

		setWillNotDraw(false);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean hasFocusable() {
		// TODO: This feels very wrong. Find out why onItemClick isn't working and fix it for real
		return false;
		//return super.hasFocusable();
	}

	public void bind(final ItinCardData itinCardData) {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		TripComponent tripComponent = itinCardData.getTripComponent();

		// Title
		View titleView = getTitleView(layoutInflater, mTitleContentLayout, tripComponent);
		if (titleView != null) {
			mTitleContentLayout.removeAllViews();
			mTitleContentLayout.addView(titleView);
		}

		// Type icon
		mItinTypeImageView.setImageResource(getTypeIconResId());
		mItinTypeStaticImageView.setImageResource(getTypeIconResId());

		// Image
		String headerImageUrl = getHeaderImageUrl(tripComponent);
		if (headerImageUrl != null) {
			UrlBitmapDrawable.loadImageView(headerImageUrl, mHeaderImageView);
		}
		else {
			Log.t("Null image for %s", tripComponent.toString());
		}

		// Header text
		mHeaderTextView.setText(getHeaderText(tripComponent));

		// Summary text

		View summaryView = getSummaryView(layoutInflater, mSummaryLayout, tripComponent);
		if (summaryView != null) {
			mSummaryLayout.removeAllViews();
			mSummaryLayout.addView(summaryView);
		}

		// Details view
		View detailsView = getDetailsView(layoutInflater, mDetailsLayout, tripComponent);
		if (detailsView != null) {
			mDetailsLayout.removeAllViews();
			mDetailsLayout.addView(detailsView);
		}

		// Buttons
		SummaryButton leftButton = getSummaryLeftButton();
		if (leftButton != null) {
			mSummaryLeftButton.setCompoundDrawablesWithIntrinsicBounds(leftButton.getIconResId(), 0, 0, 0);
			mSummaryLeftButton.setText(leftButton.getText());
			mSummaryLeftButton.setOnClickListener(leftButton.getOnClickListener());
		}

		SummaryButton rightButton = getSummaryRightButton();
		if (rightButton != null) {
			mSummaryRightButton.setCompoundDrawablesWithIntrinsicBounds(rightButton.getIconResId(), 0, 0, 0);
			mSummaryRightButton.setText(rightButton.getText());
			mSummaryRightButton.setOnClickListener(rightButton.getOnClickListener());
		}
	}

	public void showSummary(boolean show) {
		final int visibility = show ? VISIBLE : GONE;

		mSummaryLayout.setVisibility(visibility);
		mSummaryButtonLayout.setVisibility(visibility);
	}

	public void hideDetails() {

	}

	public void showDetails(final boolean show) {
		mItinTypeImageView.setVisibility(show ? INVISIBLE : VISIBLE);
		mItinTypeStaticImageView.setVisibility(show ? VISIBLE : INVISIBLE);

		mTitleLayout.setVisibility(show ? VISIBLE : GONE);
		mDetailsLayout.setVisibility(show ? VISIBLE : GONE);

		ObjectAnimator.ofFloat(mSummaryButtonLayout, "alpha", 1).start();
	}

	public void updateLayout() {
		if (getTop() <= 0) {
			return;
		}

		int itinTypeImageHeight = mItinTypeImageView.getHeight();
		int itinTypeImageHalfHeight = itinTypeImageHeight / 2;
		int headerImageHeight = mHeaderImageView.getHeight();
		int headerImageHalfHeight = headerImageHeight / 2;

		float percent = 0;

		Rect headerImageVisibleRect = new Rect();
		if (getLocalVisibleRect(headerImageVisibleRect)) {
			percent = (float) headerImageVisibleRect.height() / (float) headerImageHeight;
		}

		percent = Math.min(1.0f, Math.max(0.25f, percent));

		final int typeImageTranslationY = headerImageHalfHeight - itinTypeImageHalfHeight;
		final int viewTranslationY = Math.max(0, (headerImageHeight - (int) (percent * (float) headerImageHeight)));

		ViewHelper.setTranslationY(mItinTypeImageView, typeImageTranslationY);
		ViewHelper.setScaleX(mItinTypeImageView, percent);
		ViewHelper.setScaleY(mItinTypeImageView, percent);

		ViewHelper.setScaleX(mItinTypeStaticImageView, percent);
		ViewHelper.setScaleY(mItinTypeStaticImageView, percent);

		ViewHelper.setTranslationY(mCardLayout, viewTranslationY);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDraw(Canvas canvas) {
		updateLayout();
		super.onDraw(canvas);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// LISTENERS
	//////////////////////////////////////////////////////////////////////////////////////

	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			updateLayout();
		}

	};
}