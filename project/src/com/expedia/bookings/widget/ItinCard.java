package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.AnimatorListenerShort;
import com.expedia.bookings.animation.ResizeAnimator;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.AndroidUtils;
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
	private int mCollapsedTop;

	private boolean mShowSummary;
	private boolean mSelectCard;
	private boolean mShadeCard;

	private int mTitleLayoutHeight;
	private int mItinCardTopPadding;
	private int mItinCardBottomPadding;
	private int mItinSummarySectionHeight;
	private int mActionButtonLayoutHeight;

	private int mExpandedCardHeaderImageHeight;
	private int mMiniCardHeaderImageHeight;

	// Views from the ItinCard itself
	private View mTopExtraPaddingView;
	private View mBottomExtraPaddingView;

	private ViewGroup mCardLayout;
	private ViewGroup mTitleLayout;
	private ViewGroup mTitleContentLayout;
	private ViewGroup mHeaderLayout;
	private ViewGroup mHeaderTextLayout;
	private ViewGroup mSummarySectionLayout;
	private ViewGroup mSummaryLayout;
	private ImageView mChevronImageView;
	private ViewGroup mDetailsLayout;
	private ItinActionsSection mActionButtonLayout;

	private ImageView mItinTypeImageView;
	private ImageView mFixedItinTypeImageView;

	private ScrollView mScrollView;
	private ParallaxContainer mHeaderImageContainer;
	private ItinHeaderImageView mHeaderImageView;
	private ImageView mHeaderOverlayImageView;
	private TextView mHeaderTextView;
	private TextView mHeaderTextDateView;
	private View mSelectedView;
	private View mHeaderShadeView;
	private View mSummaryDividerView;

	// Views generated an ItinContentGenerator (that get reused)
	private View mHeaderView;
	private View mSummaryView;
	private View mDetailsView;

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
		mItinCardTopPadding = res.getDimensionPixelSize(R.dimen.itin_card_top_padding);
		mItinCardBottomPadding = res.getDimensionPixelSize(R.dimen.itin_card_bottom_padding);
		mItinSummarySectionHeight = res.getDimensionPixelSize(R.dimen.itin_summary_section_height);
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
		mSummarySectionLayout = Ui.findView(this, R.id.summary_section_layout);
		mSummaryLayout = Ui.findView(this, R.id.summary_layout);
		mChevronImageView = Ui.findView(this, R.id.chevron_image_view);
		mDetailsLayout = Ui.findView(this, R.id.details_layout);
		mActionButtonLayout = Ui.findView(this, R.id.action_button_layout);

		mItinTypeImageView = Ui.findView(this, R.id.itin_type_image_view);
		mFixedItinTypeImageView = Ui.findView(this, R.id.fixed_itin_type_image_view);

		mScrollView = Ui.findView(this, R.id.scroll_view);
		mHeaderImageContainer = Ui.findView(this, R.id.header_image_container);
		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mHeaderOverlayImageView = Ui.findView(this, R.id.header_overlay_image_view);
		mHeaderTextView = Ui.findView(this, R.id.header_text_view);
		mHeaderTextDateView = Ui.findView(this, R.id.header_text_date_view);
		mSelectedView = Ui.findView(this, R.id.selected_view);
		mHeaderShadeView = Ui.findView(this, R.id.header_mask);
		mSummaryDividerView = Ui.findView(this, R.id.summary_divider_view);

		mSummarySectionLayout.setOnClickListener(mOnClickListener);
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

	/**
	 * If the card is expanded, returns the original height of the collapsed card. May
	 * return 0 if the card has not yet been expanded.
	 * @return
	 */
	public int getCollapsedHeight() {
		int height = mShowSummary
				? mExpandedCardHeaderImageHeight
						+ mItinCardTopPadding
						+ mItinCardBottomPadding
						+ mItinSummarySectionHeight
						+ mActionButtonLayoutHeight
						+ 5 // why 5?

				: mMiniCardHeaderImageHeight
						+ mItinCardTopPadding
						+ mItinCardBottomPadding
						+ 5; // why 5?

		return height;
	}

	/**
	 * If the card is expanded, returns the original getTop() of the collapsed card. May
	 * return 0 if the card has not yet been expanded.
	 * @return
	 */
	public int getCollapsedTop() {
		return mCollapsedTop;
	}

	/**
	 * Is the MotionEvent happening atop our Summary Buttons?
	 * @param event - MotionEvent designated for the ItinCard
	 * This motion event will already have its offsetLocation set to the top of the ItinCard.
	 * @return true if touch would effect the itin card summary buttons
	 */
	public boolean isTouchOnSummaryButtons(MotionEvent event) {
		if (mActionButtonLayout != null && mActionButtonLayout.getVisibility() == View.VISIBLE && event != null) {
			int ex = (int) event.getX();
			int ey = (int) event.getY();

			int ax1 = mActionButtonLayout.getLeft();
			int ax2 = mActionButtonLayout.getRight();
			int ay1 = mActionButtonLayout.getTop();
			int ay2 = mActionButtonLayout.getBottom();

			if (ax1 <= ex && ax2 >= ex && ay1 <= ey && ay2 >= ey) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Pass touch event to our Summary Buttons
	 * @param event - MotionEvent designated for the ItinCard
	 * This motion event will already have its offsetLocation set to the top of the ItinCard.
	 * @return true
	 */
	public boolean doSummaryButtonTouch(MotionEvent event) {
		if (mActionButtonLayout != null && mActionButtonLayout.getVisibility() == View.VISIBLE && event != null) {
			MotionEvent childEvent = MotionEvent.obtain(event);
			childEvent.offsetLocation(0, -mActionButtonLayout.getTop());
			mActionButtonLayout.dispatchTouchEvent(childEvent);
			childEvent.recycle();
			return true;
		}
		return false;
	}

	public void setOnItinCardClickListener(OnItinCardClickListener onItinCardClickListener) {
		mOnItinCardClickListener = onItinCardClickListener;
	}

	public void bind(final T itinCardData) {
		if (mItinContentGenerator != null && mItinContentGenerator.getType() != itinCardData.getTripComponentType()) {
			throw new RuntimeException("Attempted to reuse an ItinCard for two different types of cards!"
					+ "  Previously used " + mItinContentGenerator.getType() + ", reused with"
					+ itinCardData.getTripComponentType());
		}

		mItinContentGenerator = ItinContentGenerator.createGenerator(getContext(), itinCardData);

		// Title
		boolean wasNull = mHeaderView == null;
		if (wasNull && mTitleContentLayout.getChildCount() > 0) {
			Log.w("Somehow we were trying to re-add the title View even though we had a View to recycle; component type="
					+ itinCardData.getTripComponentType() + " id=" + itinCardData.getId() + " itinCardId=" + toString());
			mHeaderView = mTitleContentLayout.getChildAt(0);
			wasNull = false;
		}

		mHeaderView = mItinContentGenerator.getTitleView(mHeaderView, mTitleContentLayout);
		if (wasNull && mHeaderView != null) {
			mTitleContentLayout.addView(mHeaderView);
		}

		// Type icon
		mItinTypeImageView.setImageResource(mItinContentGenerator.getTypeIconResId());
		mFixedItinTypeImageView.setImageResource(mItinContentGenerator.getTypeIconResId());

		// Header image parallax effect
		mHeaderImageContainer.setEnabled(mDisplayState.equals(DisplayState.EXPANDED));

		// Image
		mHeaderImageView.setType(getType());
		int placeholderResId = mItinContentGenerator.getHeaderImagePlaceholderResId();

		// We currently use the size of the screen, as that is what is required by us of the Expedia image API
		Point size = AndroidUtils.getScreenSize(getContext());
		UrlBitmapDrawable drawable = mItinContentGenerator.getHeaderBitmapDrawable(size.x, size.y);
		if (drawable != null) {
			drawable.configureImageView(mHeaderImageView);
		}
		else {
			mHeaderImageView.setImageResource(placeholderResId);
		}

		// Header text
		mHeaderTextView.setText(mItinContentGenerator.getHeaderText());
		mHeaderTextDateView.setText(mItinContentGenerator.getHeaderTextDate());

		// Summary text
		wasNull = mSummaryView == null;
		if (wasNull && mSummaryLayout.getChildCount() > 0) {
			Log.w("Somehow we were trying to re-add the summary View even though we had a View to recycle; component type="
					+ itinCardData.getTripComponentType() + " id=" + itinCardData.getId() + " itinCardId=" + toString());
			mSummaryView = mSummaryLayout.getChildAt(0);
			wasNull = false;
		}

		mSummaryView = mItinContentGenerator.getSummaryView(mSummaryView, mSummaryLayout);
		if (wasNull && mSummaryView != null) {
			mSummaryLayout.addView(mSummaryView);
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
		View detailsView = mItinContentGenerator.getDetailsView(null, mDetailsLayout);
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
		mSummarySectionLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
		mActionButtonLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
	}

	public void updateHeaderImageHeight() {
		final int height = mShowSummary ? mExpandedCardHeaderImageHeight : mMiniCardHeaderImageHeight;
		ResizeAnimator.setHeight(mHeaderLayout, height);

		// TODO: the "82" here is somewhat magical, and is related to the distance of mHeaderImageContainer
		// from the top of the screen. The parallax is not perfect when an image scales up from mini to
		// expanded, I don't know why.
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);
		int offsetBottom = metrics.heightPixels - (int) (82 * metrics.density);
		mHeaderImageContainer.setOffsetBottom(offsetBottom);

		ResizeAnimator.setHeight(mHeaderImageView, height);
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

	public AnimatorSet collapse(boolean animate) {
		mDisplayState = DisplayState.COLLAPSED;
		updateClickable();

		List<Animator> animators = new ArrayList<Animator>();

		final int startY = mScrollView.getScrollY();
		final int stopY = 0;

		//Title
		if (animate) {
			ValueAnimator titleResizeAnimator = ResizeAnimator.buildResizeAnimator(mTitleLayout, mTitleLayoutHeight, 0);
			titleResizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					mScrollView.scrollTo(0, (int) (((stopY - startY) * arg0.getAnimatedFraction()) + startY));
				}
			});
			animators.add(titleResizeAnimator);
		}
		else {
			ResizeAnimator.setHeight(mTitleLayout, 0);
			mScrollView.scrollTo(0, 0);
		}

		// Past overlay
		if (animate) {
			animators.add(ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 1).setDuration(400));
		}
		else {
			ViewHelper.setAlpha(mHeaderOverlayImageView, 1f);
		}

		//Header Text
		if (animate) {
			animators.add(ObjectAnimator.ofFloat(mHeaderTextLayout, "alpha", 1).setDuration(400));
			animators.add(ObjectAnimator.ofFloat(mHeaderTextLayout, "translationY", 0).setDuration(400));
		}
		else {
			ViewHelper.setAlpha(mHeaderTextLayout, 1f);
			ViewHelper.setTranslationY(mHeaderTextLayout, 0f);
		}

		// Type Icon
		if (mItinContentGenerator.getHideDetailsTypeIcon()) {
			if (animate) {
				Animator typeImageAnimator = ObjectAnimator
						.ofFloat(mItinTypeImageView, "alpha", 1)
						.setDuration(400);
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
				ViewHelper.setAlpha(mItinTypeImageView, 1f);
				mItinTypeImageView.setVisibility(View.VISIBLE);
				mFixedItinTypeImageView.setVisibility(View.GONE);
			}
		}
		else {
			if (animate) {
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
			else {
				ViewHelper.setAlpha(mItinTypeImageView, 1f);
				mItinTypeImageView.setVisibility(View.VISIBLE);
				mFixedItinTypeImageView.setVisibility(View.GONE);
			}
		}

		// Header image parallax
		if (animate) {
			ValueAnimator parallaxAnimator = ValueAnimator
					.ofInt(mHeaderImageContainer.getScrollY(), 0)
					.setDuration(300);
			parallaxAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					mHeaderImageContainer.scrollTo(0, (Integer) arg0.getAnimatedValue());
				}
			});
			animators.add(parallaxAnimator);
		}
		else {
			mHeaderImageContainer.scrollTo(0, 0);
		}

		//Summary View views
		if (!mShowSummary) {
			if (animate) {
				animators.add(ResizeAnimator.buildResizeAnimator(mHeaderLayout, mMiniCardHeaderImageHeight));
				animators.add(ResizeAnimator.buildResizeAnimator(mHeaderImageView, mMiniCardHeaderImageHeight));
				animators.add(ResizeAnimator.buildResizeAnimator(mActionButtonLayout, 0).setDuration(300));
			}
			else {
				ResizeAnimator.setHeight(mHeaderLayout, mMiniCardHeaderImageHeight);
				ResizeAnimator.setHeight(mHeaderImageView, mMiniCardHeaderImageHeight);
				ResizeAnimator.setHeight(mActionButtonLayout, 0);
			}
		}

		// Chevron rotation
		if (animate) {
			animators.add(ObjectAnimator.ofFloat(mChevronImageView, "rotation", 0f).setDuration(400));
		}
		else {
			ViewHelper.setRotation(mChevronImageView, 0f);
		}

		// Putting it all together
		if (animate) {
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			set.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationEnd(Animator arg0) {
					finishCollapse();
				}
			});
			return set;
		}
		else {
			finishCollapse();
			return null;
		}
	}

	private void finishCollapse() {
		mHeaderImageContainer.setEnabled(false);

		mHeaderImageView.setMode(mShowSummary
				? ItinHeaderImageView.MODE_SUMMARY
				: ItinHeaderImageView.MODE_MINI);

		updateSummaryVisibility();

		mScrollView.scrollTo(0, 0);

		mSummaryDividerView.setVisibility(GONE);
		mDetailsLayout.setVisibility(GONE);

		destroyDetailsView();
	}

	public AnimatorSet expand(boolean animate) {
		mDisplayState = DisplayState.EXPANDED;

		mCollapsedTop = getTop();

		inflateDetailsView();
		updateClickable();

		ViewHelper.setTranslationY(mCardLayout, 0);

		mHeaderImageView.setMode(ItinHeaderImageView.MODE_FULL);

		mSummaryDividerView.setVisibility(VISIBLE);
		mDetailsLayout.setVisibility(VISIBLE);

		ArrayList<Animator> animators = new ArrayList<Animator>();
		if (animate) {
			Animator titleLayoutResizeAnimator = ResizeAnimator
					.buildResizeAnimator(mTitleLayout, 0, mTitleLayoutHeight);
			animators.add(titleLayoutResizeAnimator);
		}
		else {
			ResizeAnimator.setHeight(mTitleLayout, mTitleLayoutHeight);
		}

		if (mActionButtonLayout.getVisibility() != VISIBLE) {
			mSummarySectionLayout.setVisibility(VISIBLE);
			mActionButtonLayout.setVisibility(VISIBLE);
			if (animate) {
				Animator actionButtonResizeAnimator = ResizeAnimator.buildResizeAnimator(
						mActionButtonLayout, mActionButtonLayoutHeight);
				animators.add(actionButtonResizeAnimator);
			}
			else {
				ResizeAnimator.setHeight(mActionButtonLayout, mActionButtonLayoutHeight);
			}
		}

		//Past overlay
		if (animate) {
			ObjectAnimator headerOverlayAlphaAnimator = ObjectAnimator
					.ofFloat(mHeaderOverlayImageView, "alpha", 0f)
					.setDuration(200);
			animators.add(headerOverlayAlphaAnimator);
		}
		else {
			ViewHelper.setAlpha(mHeaderOverlayImageView, 0f);
		}

		// Header text
		if (mItinContentGenerator.getHideDetailsTitle()) {
			if (animate) {
				ObjectAnimator headerTextAlphaAnimator = ObjectAnimator
						.ofFloat(mHeaderTextLayout, "alpha", 0f)
						.setDuration(200);
				ObjectAnimator headerTextTranslationAnimator = ObjectAnimator
						.ofFloat(mHeaderTextLayout, "translationY", -50f)
						.setDuration(400);
				animators.add(headerTextTranslationAnimator);
				animators.add(headerTextAlphaAnimator);
			}
			else {
				ViewHelper.setAlpha(mHeaderTextLayout, 0f);
				ViewHelper.setTranslationY(mHeaderTextLayout, -50f);
			}
		}

		//Type icon
		if (mItinContentGenerator.getHideDetailsTypeIcon()) {
			if (animate) {
				ObjectAnimator itinTypeImageAlphaAnimator = ObjectAnimator
						.ofFloat(mItinTypeImageView, "alpha", 0)
						.setDuration(200);
				itinTypeImageAlphaAnimator.addListener(new AnimatorListenerShort() {
					@Override
					public void onAnimationEnd(Animator animator) {
						mItinTypeImageView.setVisibility(View.INVISIBLE);
					}
				});
				animators.add(itinTypeImageAlphaAnimator);
			}
			else {
				ViewHelper.setAlpha(mItinTypeImageView, 0f);
				mItinTypeImageView.setVisibility(View.INVISIBLE);
			}
		}
		else {
			if (animate) {
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
			else {
				ViewHelper.setAlpha(mItinTypeImageView, 0f);
				mItinTypeImageView.setVisibility(View.INVISIBLE);
				mFixedItinTypeImageView.setVisibility(View.VISIBLE);
			}
		}

		//Summary View views
		if (!mShowSummary) {
			if (animate) {
				animators.add(ResizeAnimator.buildResizeAnimator(mHeaderLayout, mExpandedCardHeaderImageHeight));
				animators.add(ResizeAnimator.buildResizeAnimator(mHeaderImageView, mExpandedCardHeaderImageHeight));
			}
			else {
				ResizeAnimator.setHeight(mHeaderLayout, mExpandedCardHeaderImageHeight);
				ResizeAnimator.setHeight(mHeaderImageView, mExpandedCardHeaderImageHeight);
			}
		}

		// Chevron rotation
		if (animate) {
			animators.add(ObjectAnimator.ofFloat(mChevronImageView, "rotation", 180f).setDuration(400));
		}
		else {
			ViewHelper.setRotation(mChevronImageView, 180f);
		}

		// Putting it all together
		if (animate) {
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			set.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationEnd(Animator arg0) {
					finishExpand();
				}
			});
			return set;
		}
		else {
			finishExpand();
			return null;
		}
	}

	private void finishExpand() {
		// Sometimes the scroll view doesnt work correctly after expansion so we try a requestlayout
		if (mScrollView != null) {
			mScrollView.requestLayout();
		}

		// Enable the parallaxy header image
		mHeaderImageContainer.setEnabled(mDisplayState.equals(DisplayState.EXPANDED));
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
		mSummarySectionLayout.setClickable(mDisplayState == DisplayState.EXPANDED);
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
			case R.id.summary_section_layout: {
				if (mDisplayState.equals(DisplayState.EXPANDED) && mOnItinCardClickListener != null) {
					mOnItinCardClickListener.onCloseButtonClicked();
				}
				break;
			}
			}
		}
	};
}
