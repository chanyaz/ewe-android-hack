package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.AnimatorListenerShort;
import com.expedia.bookings.animation.ResizeAnimator;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.ViewHelper;

public class ItinCard<T extends ItinCardData> extends RelativeLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	public interface OnItinCardClickListener {
		public void onCloseButtonClicked();

		public void onShareButtonClicked(ItinContentGenerator<?> generator);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC ENUMS
	//////////////////////////////////////////////////////////////////////////////////////

	public enum DisplayState {
		COLLAPSED, EXPANDED
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private OnItinCardClickListener mOnItinCardClickListener;

	private ItinContentGenerator<? extends ItinCardData> mItinContentGenerator;

	private DisplayState mDisplayState = DisplayState.COLLAPSED;
	private boolean mShowSummary;
	private boolean mSelectCard;
	private boolean mShadeCard;

	private int mTitleLayoutHeight;
	private int mActionButtonLayoutHeight;

	private int mExpandedCardHeaderImageHeight;
	private int mMiniCardHeaderImageHeight;

	private View mTopExtraPaddingView;
	private View mBottomExtraPaddingView;

	private ViewGroup mCardLayout;
	private ViewGroup mTitleLayout;
	private ViewGroup mTitleContentLayout;
	private ViewGroup mHeaderLayout;
	private ViewGroup mHeaderTextLayout;
	private ViewGroup mSummaryLayout;
	private ViewGroup mDetailsLayout;
	private ItinActionsSection mActionButtonLayout;

	private ImageView mItinTypeImageView;
	private ImageView mFixedItinTypeImageView;

	private ScrollView mScrollView;
	private ItinHeaderImageView mHeaderImageView;
	private ImageView mHeaderOverlayImageView;
	private TextView mHeaderTextView;
	private TextView mHeaderTextDateView;
	private View mSelectedView;
	private View mHeaderShadeView;
	private View mSummaryDividerView;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCard(Context context) {
		this(context, null);
	}

	public ItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);

		inflate(context, R.layout.widget_itin_card, this);

		final Resources res = getResources();
		mTitleLayoutHeight = res.getDimensionPixelSize(R.dimen.itin_title_height);
		mActionButtonLayoutHeight = res.getDimensionPixelSize(R.dimen.itin_action_button_height);
		mExpandedCardHeaderImageHeight = res.getDimensionPixelSize(R.dimen.itin_card_expanded_image_height);
		mMiniCardHeaderImageHeight = res.getDimensionPixelSize(R.dimen.itin_card_mini_image_height);

		mTopExtraPaddingView = Ui.findView(this, R.id.top_extra_padding_view);
		mBottomExtraPaddingView = Ui.findView(this, R.id.bottom_extra_padding_view);

		mCardLayout = Ui.findView(this, R.id.card_layout);
		mTitleLayout = Ui.findView(this, R.id.title_layout);
		mTitleContentLayout = Ui.findView(this, R.id.title_content_layout);
		mHeaderLayout = Ui.findView(this, R.id.header_layout);
		mHeaderTextLayout = Ui.findView(this, R.id.header_text_layout);
		mSummaryLayout = Ui.findView(this, R.id.summary_layout);
		mDetailsLayout = Ui.findView(this, R.id.details_layout);
		mActionButtonLayout = Ui.findView(this, R.id.action_button_layout);

		mItinTypeImageView = Ui.findView(this, R.id.itin_type_image_view);
		mFixedItinTypeImageView = Ui.findView(this, R.id.fixed_itin_type_image_view);

		mScrollView = Ui.findView(this, R.id.scroll_view);
		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mHeaderOverlayImageView = Ui.findView(this, R.id.header_overlay_image_view);
		mHeaderTextView = Ui.findView(this, R.id.header_text_view);
		mHeaderTextDateView = Ui.findView(this, R.id.header_text_date_view);
		mSelectedView = Ui.findView(this, R.id.selected_view);
		mHeaderShadeView = Ui.findView(this, R.id.header_mask);
		mSummaryDividerView = Ui.findView(this, R.id.summary_divider_view);

		Ui.findView(this, R.id.close_image_button).setOnClickListener(mOnClickListener);
		Ui.findView(this, R.id.share_image_button).setOnClickListener(mOnClickListener);

		updateHeaderImageHeight();
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

	public void setOnItinCardClickListener(OnItinCardClickListener onItinCardClickListener) {
		mOnItinCardClickListener = onItinCardClickListener;
	}

	public void bind(final T itinCardData) {
		mItinContentGenerator = ItinContentGenerator.createGenerator(getContext(), itinCardData);

		// Title
		View titleView = mItinContentGenerator.getTitleView(mTitleContentLayout);
		if (titleView != null) {
			mTitleContentLayout.removeAllViews();
			mTitleContentLayout.addView(titleView);
		}

		// Type icon
		mItinTypeImageView.setImageResource(mItinContentGenerator.getTypeIconResId());
		mFixedItinTypeImageView.setImageResource(mItinContentGenerator.getTypeIconResId());

		// Image
		mHeaderImageView.setType(getType());

		String headerImageUrl = mItinContentGenerator.getHeaderImageUrl();
		if (headerImageUrl != null) {
			UrlBitmapDrawable.loadImageView(headerImageUrl, mHeaderImageView);
		}
		else {
			mHeaderImageView.setImageResource(mItinContentGenerator.getHeaderImagePlaceholderResId());
		}

		// Header text
		mHeaderTextView.setText(mItinContentGenerator.getHeaderText());
		mHeaderTextDateView.setText(mItinContentGenerator.getHeaderTextDate());

		// Summary text
		View summaryView = mItinContentGenerator.getSummaryView(mSummaryLayout);
		if (summaryView != null) {
			mSummaryLayout.removeAllViews();
			mSummaryLayout.addView(summaryView);
		}

		// Buttons
		mActionButtonLayout.bind(mItinContentGenerator.getSummaryLeftButton(),
				mItinContentGenerator.getSummaryRightButton());

		// Selected
		mSelectedView.setVisibility(mSelectCard ? View.VISIBLE : View.GONE);

		//Shade
		if (mShadeCard) {
			float shadeAlpha = 0.5f;
			mHeaderShadeView.setVisibility(View.VISIBLE);
			if (mDisplayState.equals(DisplayState.COLLAPSED) && mItinTypeImageView.getVisibility() == View.VISIBLE) {
				ViewHelper.setAlpha(mItinTypeImageView, shadeAlpha);
			}
		}
		else {
			mHeaderShadeView.setVisibility(View.GONE);
		}
	}

	public void inflateDetailsView() {
		View detailsView = mItinContentGenerator.getDetailsView(mDetailsLayout);
		if (detailsView != null) {
			mDetailsLayout.removeAllViews();
			mDetailsLayout.addView(detailsView);
		}
	}

	public void destroyDetailsView() {
		mDetailsLayout.removeAllViews();
	}

	public void setShowSummary(boolean showSummary) {
		if (mShowSummary != showSummary) {
			mShowSummary = showSummary;
			updateHeaderImageHeight();
		}
	}

	public void updateSummaryVisibility() {
		mSummaryLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
		mActionButtonLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
	}

	public void updateHeaderImageHeight() {
		final int height = mShowSummary ? mExpandedCardHeaderImageHeight : mMiniCardHeaderImageHeight;
		mHeaderLayout.getLayoutParams().height = height;
		mHeaderLayout.requestLayout();
		mHeaderImageView.getLayoutParams().height = height;
		mHeaderImageView.requestLayout();
	}

	public void setShowExtraTopPadding(boolean show) {
		mTopExtraPaddingView.setVisibility(show ? VISIBLE : GONE);
	}

	public void setShowExtraBottomPadding(boolean show) {
		mBottomExtraPaddingView.setVisibility(show ? VISIBLE : GONE);
	}

	public void setCardSelected(boolean selected) {
		mSelectCard = selected;
	}

	public void setCardShaded(boolean shade) {
		mShadeCard = shade;
	}

	public AnimatorSet collapse(boolean startAnimation) {
		mDisplayState = DisplayState.COLLAPSED;
		updateClickable();

		mHeaderImageView.setMode(mShowSummary ? ItinHeaderImageView.MODE_SUMMARY : ItinHeaderImageView.MODE_MINI);

		AnimatorSet animSet = getCollapseAnimatorSet();
		if (startAnimation) {
			animSet.start();
		}
		return animSet;
	}

	private AnimatorSet getCollapseAnimatorSet() {

		List<Animator> animators = new ArrayList<Animator>();

		final int startY = mScrollView.getScrollY();
		final int stopY = 0;

		//Title
		ValueAnimator titleResizeAnimator = ResizeAnimator.buildResizeAnimator(mTitleLayout, mTitleLayoutHeight, 0);
		titleResizeAnimator.setDuration(300);
		titleResizeAnimator.addListener(new AnimatorListenerShort() {

			@Override
			public void onAnimationEnd(Animator arg0) {
				updateSummaryVisibility();

				mScrollView.scrollTo(0, 0);

				mSummaryDividerView.setVisibility(GONE);
				mDetailsLayout.setVisibility(GONE);

				destroyDetailsView();

			}
		});
		titleResizeAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				mScrollView.scrollTo(0, (int) (((stopY - startY) * arg0.getAnimatedFraction()) + startY));
			}

		});
		animators.add(titleResizeAnimator);

		// Past overlay
		animators.add(ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 1).setDuration(400));

		//Header Text
		animators.add(ObjectAnimator.ofFloat(mHeaderTextLayout, "alpha", 1).setDuration(400));
		animators.add(ObjectAnimator.ofFloat(mHeaderTextLayout, "translationY", 0).setDuration(400));

		// Type Icon
		if (mItinContentGenerator.getHideDetailsTypeIcon()) {
			Animator typeImageAnimator = ObjectAnimator.ofFloat(mItinTypeImageView, "alpha", 1).setDuration(400);
			typeImageAnimator.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationStart(Animator arg0) {
					mItinTypeImageView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					mItinTypeImageView.setVisibility(View.VISIBLE);
					mFixedItinTypeImageView.setVisibility(View.GONE);
				}
			});
			animators.add(typeImageAnimator);
		}
		else {
			ValueAnimator dummy = ValueAnimator.ofInt(0, 1).setDuration(300);
			dummy.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationEnd(Animator arg0) {
					ViewHelper.setAlpha(mItinTypeImageView, 1f);
					mItinTypeImageView.setVisibility(View.VISIBLE);
					mFixedItinTypeImageView.setVisibility(View.GONE);
				}
			});
			animators.add(dummy);
		}

		//Summary View views
		if (!mShowSummary) {
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderLayout, mMiniCardHeaderImageHeight));
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderImageView, mMiniCardHeaderImageHeight));
			animators.add(ResizeAnimator.buildResizeAnimator(mActionButtonLayout, 0).setDuration(
					300));
		}

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animators);
		return set;

	}

	public AnimatorSet expand(boolean startAnimation) {
		mDisplayState = DisplayState.EXPANDED;

		inflateDetailsView();
		updateClickable();

		mHeaderImageView.setMode(ItinHeaderImageView.MODE_FULL);

		mSummaryDividerView.setVisibility(VISIBLE);
		mDetailsLayout.setVisibility(VISIBLE);

		ArrayList<Animator> animators = new ArrayList<Animator>();
		Animator titleLayoutResizeAnimator = ResizeAnimator.buildResizeAnimator(mTitleLayout, 0, mTitleLayoutHeight);
		animators.add(titleLayoutResizeAnimator);
		if (mActionButtonLayout.getVisibility() != VISIBLE) {
			mSummaryLayout.setVisibility(VISIBLE);
			mActionButtonLayout.setVisibility(VISIBLE);
			Animator actionButtonResizeAnimator = ResizeAnimator.buildResizeAnimator(mActionButtonLayout,
					mActionButtonLayoutHeight);
			animators.add(actionButtonResizeAnimator);
		}
		animators.add(getExpandAnimatorSet());
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(animators);
		if (startAnimation) {
			animSet.start();
		}
		return animSet;
	}

	private AnimatorSet getExpandAnimatorSet() {

		List<Animator> animators = new ArrayList<Animator>();

		//Past overlay
		ObjectAnimator headerOverlayAlphaAnimator = ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 0)
				.setDuration(200);
		animators.add(headerOverlayAlphaAnimator);

		// Header text
		if (mItinContentGenerator.getHideDetailsTitle()) {
			ObjectAnimator headerTextAlphaAnimator = ObjectAnimator.ofFloat(mHeaderTextLayout, "alpha", 0).setDuration(
					200);
			ObjectAnimator headerTextTranslationAnimator = ObjectAnimator.ofFloat(mHeaderTextLayout, "translationY",
					-50)
					.setDuration(400);
			animators.add(headerTextTranslationAnimator);
			animators.add(headerTextAlphaAnimator);
		}

		//Type icon
		if (mItinContentGenerator.getHideDetailsTypeIcon()) {
			ObjectAnimator itinTypeImageAlphaAnimator = ObjectAnimator.ofFloat(mItinTypeImageView, "alpha", 0);
			itinTypeImageAlphaAnimator.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationEnd(Animator animator) {
					mItinTypeImageView.setVisibility(View.INVISIBLE);
				}
			});
			itinTypeImageAlphaAnimator.setDuration(200);
			animators.add(itinTypeImageAlphaAnimator);
		}
		else {
			ValueAnimator dummy = ValueAnimator.ofInt(0, 1).setDuration(300);
			dummy.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationStart(Animator arg0) {
					ViewHelper.setAlpha(mItinTypeImageView, 0f);
					mItinTypeImageView.setVisibility(View.INVISIBLE);
					mFixedItinTypeImageView.setVisibility(View.VISIBLE);
				}
			});
			animators.add(dummy);
		}

		//Summary View views
		if (!mShowSummary) {
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderLayout, mExpandedCardHeaderImageHeight));
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderImageView, mExpandedCardHeaderImageHeight));
		}

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animators);

		//Sometimes the scroll view doesnt work correctly after expansion so we try a requestlayout
		set.addListener(new AnimatorListenerShort() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				if (mScrollView != null) {
					mScrollView.requestLayout();
				}
			}
		});

		return set;
	}

	// Type icon position and size
	public void updateLayout() {
		if (mDisplayState == DisplayState.COLLAPSED) {
			mItinTypeImageView.setVisibility(View.VISIBLE);
			float typeImageHeight = mItinTypeImageView.getHeight();
			float typeImageHalfHeight = typeImageHeight / 2;
			float headerImageHeight = mHeaderImageView.getHeight();
			float typeImageY = (headerImageHeight - mHeaderTextLayout.getHeight()) / 2;

			float percent = 0;
			float percentIcon = 0;

			Rect headerImageVisibleRect = new Rect();
			if (getLocalVisibleRect(headerImageVisibleRect)) {
				percent = (float) headerImageVisibleRect.height() / (float) headerImageHeight;
			}

			percent = Math.min(1.0f, Math.max(0.5f, percent));
			if (getTop() <= 0) {
				percent = 1f;
			}

			percentIcon = mShowSummary ? percent : Math.min(0.75f, percent);

			final float typeImageTranslationY = mCardLayout.getTop() + typeImageY - typeImageHalfHeight;
			final float viewTranslationY = Math.max(0, (headerImageHeight - (percent * headerImageHeight)) / 2);

			ViewHelper.setTranslationY(mItinTypeImageView, typeImageTranslationY);
			ViewHelper.setScaleX(mItinTypeImageView, percentIcon);
			ViewHelper.setScaleY(mItinTypeImageView, percentIcon);
			ViewHelper.setTranslationY(mHeaderTextLayout, (mItinTypeImageView.getHeight() * percentIcon) / 2);
			ViewHelper.setTranslationY(mCardLayout, viewTranslationY);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	protected boolean hasDetails() {
		return mItinContentGenerator.hasDetails();
	}

	protected Type getType() {
		return mItinContentGenerator.getItinCardData().getTripComponentType();
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

	//////////////////////////////////////////////////////////////////////////////////////
	// LISTENERS
	//////////////////////////////////////////////////////////////////////////////////////

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.close_image_button: {
				if (mOnItinCardClickListener != null) {
					mOnItinCardClickListener.onCloseButtonClicked();
				}
				break;
			}
			case R.id.share_image_button: {
				if (mOnItinCardClickListener != null) {
					mOnItinCardClickListener.onShareButtonClicked(mItinContentGenerator);
				}
				break;
			}
			}
		}
	};
}
