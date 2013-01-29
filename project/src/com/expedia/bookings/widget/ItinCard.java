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
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.view.ViewHelper;

public abstract class ItinCard extends RelativeLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// ABSTRACT METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public abstract int getTypeIconResId();

	public abstract Type getType();

	protected abstract String getHeaderImageUrl(TripComponent tripComponent);

	protected abstract String getHeaderText(TripComponent tripComponent);

	protected abstract View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent);

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ViewGroup mCardLayout;
	private ViewGroup mSummaryLayout;
	private ViewGroup mDetailsLayout;
	private ViewGroup mSummaryButtonLayout;

	private OptimizedImageView mHeaderImageView;
	private ImageView mItinTypeImageView;
	private TextView mHeaderTextView;

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
		mSummaryLayout = Ui.findView(this, R.id.summary_layout);
		mDetailsLayout = Ui.findView(this, R.id.details_layout);
		mSummaryButtonLayout = Ui.findView(this, R.id.summary_button_layout);

		mItinTypeImageView = Ui.findView(this, R.id.itin_type_image_view);
		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mHeaderTextView = Ui.findView(this, R.id.header_text_view);

		setWillNotDraw(false);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(final TripComponent tripComponent) {
		// Type icon
		mItinTypeImageView.setImageResource(getTypeIconResId());

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

		// Details view
		//		View detailsView = getDetailsView(LayoutInflater.from(getContext()), mDetailsLayout, tripComponent);
		//		if (detailsView != null) {
		//			mDetailsLayout.removeAllViews();
		//			mDetailsLayout.addView(detailsView);
		//		}

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setFocusable(false);
		}

	}

	public void showSummary(boolean show) {
		final int visibility = show ? VISIBLE : GONE;

		mSummaryLayout.setVisibility(visibility);
		mSummaryButtonLayout.setVisibility(visibility);
	}

	public void showDetails(final boolean show) {
		mDetailsLayout.setVisibility(show ? VISIBLE : GONE);
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
}