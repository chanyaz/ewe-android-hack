package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.ResizeAnimation;
import com.expedia.bookings.animation.ResizeAnimation.AnimationStepListener;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

@SuppressWarnings("unchecked")
public abstract class ItinCard<T extends ItinCardData> extends RelativeLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC ENUYMS
	//////////////////////////////////////////////////////////////////////////////////////

	public enum DisplayState {
		COLLAPSED, EXPANDED
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// ABSTRACT METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	// Type

	public abstract int getTypeIconResId();

	public abstract Type getType();

	// Title share button

	protected abstract void onShareButtonClick(T itinCardData);

	// Header image

	protected abstract String getHeaderImageUrl(T itinCardData);

	protected abstract String getHeaderText(T itinCardData);

	// Views

	protected abstract View getTitleView(LayoutInflater inflater, ViewGroup container, T itinCardData);

	protected abstract View getSummaryView(LayoutInflater inflater, ViewGroup container, T itinCardData);

	protected abstract View getDetailsView(LayoutInflater inflater, ViewGroup container, T itinCardData);

	// Action buttons

	protected abstract SummaryButton getSummaryLeftButton(T itinCardData);

	protected abstract SummaryButton getSummaryRightButton(T itinCardData);

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

	private T mItinCardData;

	private DisplayState mDisplayState = DisplayState.COLLAPSED;
	private boolean mShowSummary = false;

	private int mTitleLayoutHeight;
	private int mActionButtonLayoutHeight;

	private ViewGroup mCardLayout;
	private ViewGroup mTitleLayout;
	private ViewGroup mTitleContentLayout;
	private ViewGroup mSummaryLayout;
	private ViewGroup mDetailsLayout;
	private ViewGroup mActionButtonLayout;

	private ImageView mItinTypeImageView;

	private ImageButton mCloseImageButton;
	private ImageButton mShareImageButton;

	private ScrollView mScrollView;
	private OptimizedImageView mHeaderImageView;
	private ImageView mHeaderOverlayImageView;
	private TextView mHeaderTextView;
	private View mSummaryDividerView;

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

		mTitleLayoutHeight = getResources().getDimensionPixelSize(R.dimen.itin_title_height);
		mActionButtonLayoutHeight = getResources().getDimensionPixelSize(R.dimen.itin_action_button_height);

		mCardLayout = Ui.findView(this, R.id.card_layout);
		mTitleLayout = Ui.findView(this, R.id.title_layout);
		mTitleContentLayout = Ui.findView(this, R.id.title_content_layout);
		mSummaryLayout = Ui.findView(this, R.id.summary_layout);
		mDetailsLayout = Ui.findView(this, R.id.details_layout);
		mActionButtonLayout = Ui.findView(this, R.id.action_button_layout);

		mItinTypeImageView = Ui.findView(this, R.id.itin_type_image_view);

		mCloseImageButton = Ui.findView(this, R.id.close_image_button);
		mShareImageButton = Ui.findView(this, R.id.share_image_button);

		mScrollView = Ui.findView(this, R.id.scroll_view);
		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mHeaderOverlayImageView = Ui.findView(this, R.id.header_overlay_image_view);
		mHeaderTextView = Ui.findView(this, R.id.header_text_view);
		mSummaryDividerView = Ui.findView(this, R.id.summary_divider_view);

		mSummaryLeftButton = Ui.findView(this, R.id.summary_left_button);
		mSummaryRightButton = Ui.findView(this, R.id.summary_right_button);

		updateClickable();
		updateLayout();

		setWillNotDraw(false);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// VIEW OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean hasFocusable() {
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(final ItinCardData itinCardData) {
		final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		mItinCardData = (T) itinCardData;

		// Title
		View titleView = getTitleView(layoutInflater, mTitleContentLayout, (T) itinCardData);
		if (titleView != null) {
			mTitleContentLayout.removeAllViews();
			mTitleContentLayout.addView(titleView);
		}

		mShareImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onShareButtonClick((T) itinCardData);
			}
		});

		// Type icon
		mItinTypeImageView.setImageResource(getTypeIconResId());

		// Image
		String headerImageUrl = getHeaderImageUrl((T) itinCardData);
		if (headerImageUrl != null) {
			UrlBitmapDrawable.loadImageView(headerImageUrl, mHeaderImageView);
		}
		else {
			Log.t("Null image for %s", itinCardData.toString());
		}

		// Header text
		mHeaderTextView.setText(getHeaderText((T) itinCardData));

		// Summary text

		View summaryView = getSummaryView(layoutInflater, mSummaryLayout, (T) itinCardData);
		if (summaryView != null) {
			mSummaryLayout.removeAllViews();
			mSummaryLayout.addView(summaryView);
		}

		// Details view
		View detailsView = getDetailsView(layoutInflater, mDetailsLayout, (T) itinCardData);
		if (detailsView != null) {
			mDetailsLayout.removeAllViews();
			mDetailsLayout.addView(detailsView);
		}

		// Buttons
		SummaryButton leftButton = getSummaryLeftButton((T) itinCardData);
		if (leftButton != null) {
			mSummaryLeftButton.setCompoundDrawablesWithIntrinsicBounds(leftButton.getIconResId(), 0, 0, 0);
			mSummaryLeftButton.setText(leftButton.getText());
			mSummaryLeftButton.setOnClickListener(leftButton.getOnClickListener());
		}

		SummaryButton rightButton = getSummaryRightButton((T) itinCardData);
		if (rightButton != null) {
			mSummaryRightButton.setCompoundDrawablesWithIntrinsicBounds(rightButton.getIconResId(), 0, 0, 0);
			mSummaryRightButton.setText(rightButton.getText());
			mSummaryRightButton.setOnClickListener(rightButton.getOnClickListener());
		}
	}

	public void inflateDetailsView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		View detailsView = getDetailsView(layoutInflater, mDetailsLayout, (T) mItinCardData);
		if (detailsView != null) {
			mDetailsLayout.removeAllViews();
			mDetailsLayout.addView(detailsView);
		}
	}

	public void destroyDetailsView() {
		mDetailsLayout.removeAllViews();
	}

	public void setShowSummary(boolean showSummary) {
		mShowSummary = showSummary;
	}

	public void updateSummaryVisibility() {
		mSummaryLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
		mActionButtonLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
	}

	public void collapse() {
		mDisplayState = DisplayState.COLLAPSED;
		updateClickable();

		final int startY = mScrollView.getScrollY();
		final int stopY = 0;

		ResizeAnimation titleAnimation = new ResizeAnimation(mTitleLayout, mTitleLayoutHeight, 0);
		titleAnimation.setDuration(300);
		titleAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				updateSummaryVisibility();

				mScrollView.scrollTo(0, 0);

				mSummaryDividerView.setVisibility(GONE);
				mDetailsLayout.setVisibility(GONE);

				destroyDetailsView();
			}
		});
		titleAnimation.setAnimationStepListener(new AnimationStepListener() {
			@Override
			public void onAnimationStep(Animation animation, float interpolatedTime) {
				mScrollView.scrollTo(0, (int) (((stopY - startY) * interpolatedTime) + startY));
			}
		});

		mTitleLayout.startAnimation(titleAnimation);

		if (!mShowSummary) {
			ResizeAnimation actionButtonAnimation = new ResizeAnimation(mActionButtonLayout, 0);
			actionButtonAnimation.setDuration(300);

			mActionButtonLayout.startAnimation(actionButtonAnimation);
		}

		// Alpha
		ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 1).setDuration(400).start();
		ObjectAnimator.ofFloat(mHeaderTextView, "alpha", 1).setDuration(400).start();
		ObjectAnimator.ofFloat(mItinTypeImageView, "alpha", 1).setDuration(400).start();

		// TranslationY
		ObjectAnimator.ofFloat(mHeaderTextView, "translationY", 0).setDuration(400).start();
	}

	public void expand() {
		mDisplayState = DisplayState.EXPANDED;

		inflateDetailsView();
		updateClickable();

		mSummaryDividerView.setVisibility(VISIBLE);
		mDetailsLayout.setVisibility(VISIBLE);

		mTitleLayout.startAnimation(new ResizeAnimation(mTitleLayout, 0, mTitleLayoutHeight));

		if (mActionButtonLayout.getVisibility() != VISIBLE) {
			mSummaryLayout.setVisibility(VISIBLE);
			mActionButtonLayout.setVisibility(VISIBLE);
			mActionButtonLayout.startAnimation(new ResizeAnimation(mActionButtonLayout, mActionButtonLayoutHeight));
		}

		// Alpha
		ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 0).setDuration(200).start();
		ObjectAnimator.ofFloat(mHeaderTextView, "alpha", 0).setDuration(200).start();
		ObjectAnimator.ofFloat(mItinTypeImageView, "alpha", 0).setDuration(200).start();

		// TranslationY
		ObjectAnimator.ofFloat(mHeaderTextView, "translationY", -50).setDuration(400).start();
	}

	// Type icon position and size

	public void updateLayout() {
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
		if (getTop() <= 0) {
			percent = 1f;
		}

		final int typeImageTranslationY = mHeaderImageView.getTop() + headerImageHalfHeight - itinTypeImageHalfHeight;
		final int viewTranslationY = Math.max(0, (headerImageHeight - (int) (percent * (float) headerImageHeight)) / 2);

		ViewHelper.setTranslationY(mItinTypeImageView, typeImageTranslationY);
		ViewHelper.setScaleX(mItinTypeImageView, percent);
		ViewHelper.setScaleY(mItinTypeImageView, percent);

		ViewHelper.setTranslationY(mCardLayout, viewTranslationY);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void updateClickable() {
		mScrollView.setEnabled(mDisplayState == DisplayState.EXPANDED);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		updateLayout();
	}
}