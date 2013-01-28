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
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;

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
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private final int TYPE_IMAGE_START_SIZE;
	private final int TYPE_IMAGE_END_SIZE;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ViewGroup mInnerContainer;
	private ViewGroup mImageContainer;
	private ViewGroup mExpandedContainer;
	private ScrollView mDetailsScrollView;
	private OptimizedImageView mCardImage;
	private ImageView mFloatTypeIcon;
	private TextView mItinHeaderText;

	private int mLastDimen = 0;
	private int mSecondLastDimen = 0;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCard(Context context) {
		this(context, null);
	}

	public ItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);

		TYPE_IMAGE_START_SIZE = (int) getResources().getDimension(R.dimen.itin_list_icon_start_size);
		TYPE_IMAGE_END_SIZE = (int) getResources().getDimension(R.dimen.itin_list_icon_end_size);

		inflate(context, R.layout.widget_itin_card, this);

		mInnerContainer = Ui.findView(this, R.id.inner_itin_container);
		mImageContainer = Ui.findView(this, R.id.itin_image_container);
		mCardImage = Ui.findView(this, R.id.itin_bg);
		mFloatTypeIcon = Ui.findView(this, R.id.float_type_icon);
		mExpandedContainer = Ui.findView(this, R.id.itin_expanded_container);
		mDetailsScrollView = Ui.findView(this, R.id.details_scroll_view);
		mItinHeaderText = Ui.findView(this, R.id.itin_heading_text);

		setWillNotDraw(false);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(final TripComponent tripComponent) {
		// Type icon
		mFloatTypeIcon.setImageResource(getTypeIconResId());

		// Image
		String headerImageUrl = getHeaderImageUrl(tripComponent);
		if (headerImageUrl != null) {
			UrlBitmapDrawable.loadImageView(headerImageUrl, mCardImage);
		}
		else {
			Log.t("Null image for %s", tripComponent.toString());
		}

		// Header text
		mItinHeaderText.setText(getHeaderText(tripComponent));

		// Details view
		View detailsView = getDetailsView(LayoutInflater.from(getContext()), mDetailsScrollView, tripComponent);
		if (detailsView != null) {
			mDetailsScrollView.removeAllViews();
			mDetailsScrollView.addView(detailsView);
		}
	}

	public void showSummary(boolean show) {
		mExpandedContainer.setVisibility(show ? VISIBLE : GONE);
	}

	public void showDetails(final boolean show) {
		mDetailsScrollView.setVisibility(show ? VISIBLE : GONE);
	}

	public void updateTypeIconPosition() {
		Rect cardRect = new Rect();
		if (getLocalVisibleRect(cardRect)) {
			//View is at least partly visible

			int floatImageTopMargin = 0;
			int floatImageHeight = mFloatTypeIcon.getHeight();
			int floatImageHalfHeight = floatImageHeight / 2;
			int imageContainerHeight = mImageContainer.getHeight();
			int imageContainerHalfHeight = imageContainerHeight / 2;
			int innerItinContainerTopMargin = mInnerContainer.getTop();

			int maxTopMargin = innerItinContainerTopMargin + imageContainerHalfHeight - floatImageHeight;

			Rect imageContainerRect = new Rect();
			if (mImageContainer.getLocalVisibleRect(imageContainerRect)) {
				int imageContainerCenterY = imageContainerRect.centerY();
				floatImageTopMargin = innerItinContainerTopMargin + imageContainerCenterY - floatImageHeight;
			}

			//Bounds
			if (floatImageTopMargin > maxTopMargin) {
				floatImageTopMargin = maxTopMargin;
			}
			if (floatImageTopMargin < 0) {
				floatImageTopMargin = 0;
			}

			double factor = 1e2;
			double percentage = (0.0 + floatImageTopMargin) / maxTopMargin;
			percentage = Math.round(percentage * factor) / factor;
			int dimen = (int) (TYPE_IMAGE_START_SIZE + Math.round(percentage
					* (TYPE_IMAGE_END_SIZE - TYPE_IMAGE_START_SIZE)));

			Log.t("Percentage: %f - dimen: %d", percentage, dimen);

			RelativeLayout.LayoutParams params = (LayoutParams) mFloatTypeIcon.getLayoutParams();
			boolean changed = params.topMargin != floatImageTopMargin || params.height != dimen
					|| params.width != dimen;
			if (changed && !isWiggling(dimen)) {
				params.topMargin = floatImageTopMargin;
				params.height = dimen;
				params.width = dimen;
				mFloatTypeIcon.setLayoutParams(params);
			}
		}
		else {
			//View is invisible
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private boolean isWiggling(int newDimen) {
		boolean retVal = false;
		if (mSecondLastDimen == newDimen) {
			retVal = true;
		}

		//Shift
		mSecondLastDimen = mLastDimen;
		mLastDimen = newDimen;

		return retVal;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDraw(Canvas canvas) {
		updateTypeIconPosition();
		super.onDraw(canvas);
	}
}