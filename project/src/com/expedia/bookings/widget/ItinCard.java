package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.TripComponent;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public abstract class ItinCard extends RelativeLayout {

	private final int TYPE_IMAGE_START_SIZE;
	private final int TYPE_IMAGE_END_SIZE;

	private ViewGroup mOuterContainer;
	private ViewGroup mInnerContainer;
	private ViewGroup mImageContainer;
	private ViewGroup mExpandedContainer;
	private OptimizedImageView mCardImage;
	private ImageView mFloatTypeIcon;
	private TextView mItinHeaderText;

	private int mPaddingBottom;

	private boolean mShowExpanded = false;

	public ItinCard(Context context) {
		super(context);

		Resources res = getResources();
		TYPE_IMAGE_START_SIZE = (int) res.getDimension(R.dimen.itin_list_icon_start_size);
		TYPE_IMAGE_END_SIZE = (int) res.getDimension(R.dimen.itin_list_icon_end_size);

		init(context, null);
	}

	public ItinCard(Context context, AttributeSet attr) {
		super(context, attr);

		Resources res = getResources();
		TYPE_IMAGE_START_SIZE = (int) res.getDimension(R.dimen.itin_list_icon_start_size);
		TYPE_IMAGE_END_SIZE = (int) res.getDimension(R.dimen.itin_list_icon_end_size);

		init(context, attr);
	}

	public ItinCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		Resources res = getResources();
		TYPE_IMAGE_START_SIZE = (int) res.getDimension(R.dimen.itin_list_icon_start_size);
		TYPE_IMAGE_END_SIZE = (int) res.getDimension(R.dimen.itin_list_icon_end_size);

		init(context, attrs);
	}

	private void init(Context context, AttributeSet attr) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_itin_expanded, this);

		mOuterContainer = Ui.findView(view, R.id.outer_itin_container);
		mInnerContainer = Ui.findView(view, R.id.inner_itin_container);
		mImageContainer = Ui.findView(view, R.id.itin_image_container);
		mCardImage = Ui.findView(view, R.id.itin_bg);
		mFloatTypeIcon = Ui.findView(view, R.id.float_type_icon);
		mExpandedContainer = Ui.findView(view, R.id.itin_expanded_container);
		mItinHeaderText = Ui.findView(view, R.id.itin_heading_text);

		mPaddingBottom = getResources().getDimensionPixelSize(R.dimen.itin_list_card_top_image_offset);

		showBottomPadding(true);

		this.setWillNotDraw(false);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		return super.onSaveInstanceState();
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}

	@Override
	public void onDraw(Canvas canvas) {
		updateTypeIconPosition();
		super.onDraw(canvas);
	}

	public void bind(TripComponent tripComponent) {
		mItinHeaderText.setText(tripComponent.getType().toString().toUpperCase() + " Fun Town");
	}

	public void showExpanded(boolean show) {
		mShowExpanded = show;
		mExpandedContainer.setVisibility(mShowExpanded ? View.VISIBLE : View.GONE);
	}

	public void showBottomPadding(boolean show) {
		int padding = show ? mPaddingBottom : 0;
		setPadding(0, 0, 0, padding);
	}

	private int mLastDimen = 0;
	private int mSecondLastDimen = 0;

	public boolean isWiggling(int newDimen) {
		boolean retVal = false;
		if (mSecondLastDimen == newDimen) {
			retVal = true;
		}

		//Shift
		mSecondLastDimen = mLastDimen;
		mLastDimen = newDimen;

		return retVal;
	}

	public void updateTypeIconPosition() {
		Rect cardRect = new Rect();
		if (ItinCard.this.getLocalVisibleRect(cardRect)) {
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

			Log.i("Percentage:" + percentage + " dimen:" + dimen);

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
}
