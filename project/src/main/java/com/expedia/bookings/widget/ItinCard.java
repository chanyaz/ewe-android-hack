package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dgmltn.shareeverywhere.ShareView;
import com.expedia.bookings.R;
import com.expedia.bookings.animation.ResizeAnimator;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.CalendarAPIUtils;

public class ItinCard<T extends ItinCardData> extends RelativeLayout implements PopupMenu.OnMenuItemClickListener,
		ShareView.OnShareTargetSelectedListener {

	//////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	public interface OnItinCardClickListener {
		public void onCloseButtonClicked();
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

	private static final int SHADED_ITIN_TYPE_ALPHA = 128;

	private OnItinCardClickListener mOnItinCardClickListener;

	private ItinContentGenerator<? extends ItinCardData> mItinContentGenerator;

	private DisplayState mDisplayState = DisplayState.COLLAPSED;
	private int mCollapsedTop;

	private boolean mShowSummary;
	private boolean mSelectCard;
	private boolean mShadeCard;

	private int mTitleLayoutHeight;
	private int mItinCardExtraTopPadding;
	private int mItinCardExtraBottomPadding;
	private int mItinSummarySectionHeight;
	private int mActionButtonLayoutHeight;

	private int mFixedItinTypeImageTranslation;

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

	private AlphaImageView mItinTypeImageView;
	private AlphaImageView mFixedItinTypeImageView;

	private ScrollView mScrollView;
	private ParallaxContainer mHeaderImageContainer;
	private ImageView mHeaderImageView;
	private ImageView mHeaderOverlayImageView;
	private TextView mHeaderTextView;
	private TextView mHeaderTextDateView;
	private View mSelectedView;
	private View mHeaderShadeView;
	private View mSummaryDividerView;

	private ShareView mShareView;

	// Views generated an ItinContentGenerator (that get reused)
	private View mHeaderView;
	private View mSummaryView;
	private View mDetailsView;

	// Used in header image view
	private HeaderBitmapDrawable mHeaderBitmapDrawable;

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
		mItinCardExtraTopPadding = res.getDimensionPixelSize(R.dimen.itin_card_extra_top_padding);
		mItinCardExtraBottomPadding = res.getDimensionPixelSize(R.dimen.itin_card_extra_bottom_padding);
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
		Ui.setOnClickListener(this, R.id.close_image_button, mOnClickListener);
		Ui.setOnClickListener(this, R.id.itin_overflow_image_button, mOnClickListener);

		mShareView = Ui.findView(this, R.id.itin_share_view);

		updateHeaderImageHeight();
		updateClickable();
		updateLayout();

		setWillNotDraw(false);

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mChevronImageView.setVisibility(View.INVISIBLE);
		}
		else {
			mChevronImageView.setVisibility(View.VISIBLE);
		}
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
	 * Returns the collapsed height of this card, even if the card is expanded.
	 * @return
	 */
	public int getCollapsedHeight() {
		int height = getResources().getDimensionPixelSize(mShowSummary
				? R.dimen.itin_card_summary_collapsed_height
				: R.dimen.itin_card_collapsed_height);

		height += (mTopExtraPaddingView.getVisibility() == VISIBLE ? mItinCardExtraTopPadding : 0)
				+ (mBottomExtraPaddingView.getVisibility() == VISIBLE ? mItinCardExtraBottomPadding : 0);

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
		if (mItinContentGenerator.isSharedItin()) {
			mItinTypeImageView.setImageBitmap(mItinContentGenerator.getSharedItinCardIcon());
			mFixedItinTypeImageView.setImageBitmap(mItinContentGenerator.getSharedItinCardIcon());
		}
		else {
			mItinTypeImageView.setImageResource(mItinContentGenerator.getTypeIconResId());
			mFixedItinTypeImageView.setImageResource(mItinContentGenerator.getTypeIconResId());
		}

		// Header image parallax effect
		mHeaderImageContainer.setEnabled(mDisplayState.equals(DisplayState.EXPANDED));

		// Header Image
		Resources res = getResources();

		if (mHeaderBitmapDrawable == null) {
			mHeaderBitmapDrawable = new HeaderBitmapDrawable();

			//Setting the placeholder image
			int placeholderResId = mItinContentGenerator.getHeaderImagePlaceholderResId();
			Drawable placeholderDrawable = res.getDrawable(placeholderResId);
			mHeaderBitmapDrawable.setPlaceholderDrawable(placeholderDrawable);

			mHeaderBitmapDrawable.setCornerRadius(res.getDimensionPixelSize(R.dimen.itin_card_corner_radius));

			if (getType() == Type.FLIGHT) {
				mHeaderBitmapDrawable.setMatrixTranslation(0,
					res.getDimensionPixelSize(R.dimen.itin_card_flight_vertical_offset));
			}
			else {
				mHeaderBitmapDrawable.setMatrixTranslation(0, 0);
			}

			mHeaderImageView.setImageDrawable(mHeaderBitmapDrawable);
		}

		// We currently use the size of the screen, as that is what is required by us of the Expedia image API
		Point size = AndroidUtils.getScreenSize(getContext());
		mItinContentGenerator.getHeaderBitmapDrawable(size.x, size.y, mHeaderBitmapDrawable);


		if (mDisplayState == DisplayState.EXPANDED) {
			mHeaderBitmapDrawable.setOverlayDrawable(null);
			mHeaderBitmapDrawable.setCornerMode(CornerMode.NONE);
		}
		else {
			mHeaderBitmapDrawable.setOverlayDrawable(res.getDrawable(R.drawable.card_top_lighting));
			mHeaderBitmapDrawable.setCornerMode(mShowSummary ? CornerMode.TOP : CornerMode.ALL);
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
		if (mShadeCard && mDisplayState != DisplayState.EXPANDED) {
			mHeaderShadeView.setVisibility(View.VISIBLE);
			mItinTypeImageView.setDrawAlpha(SHADED_ITIN_TYPE_ALPHA);
			mFixedItinTypeImageView.setDrawAlpha(SHADED_ITIN_TYPE_ALPHA);
		}
		else {
			mHeaderShadeView.setVisibility(View.GONE);
		}
	}

	/**
	 * This method re-binds the ItinCard with the provided data, under the assumption that
	 * the card is already expanded, and the views need updating.
	 *
	 * @param itinCardData - new data to bind
	 * @throws RuntimeException if this card was not already in EXPANDED mode
	 */
	public void rebindExpandedCard(final T itinCardData) {
		if (itinCardData != null) {
			if (mDisplayState == DisplayState.EXPANDED) {
				bind(itinCardData);
				inflateDetailsView();
				updateClickable();
				finishExpand();
			}
			else {
				throw new RuntimeException("Calling rebindExpandedCard may only be called on already expanded cards!");
			}
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
			updateSummaryVisibility();
			updateHeaderImageHeight();
		}
	}

	public void updateSummaryVisibility() {
		mSummarySectionLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
		mActionButtonLayout.setVisibility(mShowSummary ? VISIBLE : GONE);
		// Let's not show the date text in summaryCard as per design
		mHeaderTextDateView.setVisibility(mShowSummary ? GONE : VISIBLE);
	}

	public void updateHeaderImageHeight() {
		final int height = mShowSummary ? mExpandedCardHeaderImageHeight : mMiniCardHeaderImageHeight;
		ResizeAnimator.setHeight(mHeaderLayout, height);

		// TODO: the "82" here is somewhat magical, and is related to the distance of mHeaderImageContainer
		// from the top of the screen. The parallax is not perfect when an image scales up from mini to
		// expanded, I don't know why.
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int offsetBottom = metrics.heightPixels	- (int) (82 * metrics.density);
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

		// Since we are adding the dropshadow to expanded view for sharedItins, no use in animating.
		if (!mItinContentGenerator.isSharedItin()) {
			// Past overlay
			if (animate) {
				animators.add(ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 1).setDuration(400));
			}
			else {
				mHeaderOverlayImageView.setAlpha(1f);
			}
		}

		//Header Text
		boolean isDateHidden = mHeaderTextDateView.getAlpha() < 1f;
		boolean isTitleHidden = mHeaderTextLayout.getAlpha() < 1f;
		boolean isTitleTranslated = mHeaderTextView.getTranslationY() != 0f;
		if (isDateHidden) {
			if (animate) {
				animators.add(ObjectAnimator
						.ofFloat(mHeaderTextDateView, "alpha", 1f)
						.setDuration(200));
			}
			else {
				mHeaderTextDateView.setAlpha(1f);
			}
		}
		if (isTitleHidden) {
			if (animate) {
				animators.add(ObjectAnimator
						.ofFloat(mHeaderTextLayout, "alpha", 1f)
						.setDuration(200));
			}
			else {
				mHeaderTextLayout.setAlpha(1f);
			}
		}
		if (isTitleTranslated) {
			if (animate) {
				animators.add(ObjectAnimator
						.ofFloat(mHeaderTextView, "translationY", 0f)
						.setDuration(400));
			}
			else {
				mHeaderTextView.setTranslationY(0f);
			}
		}

		// Header Shade (for past itins)
		if (mShadeCard) {
			if (animate) {
				ObjectAnimator shadeAnim = ObjectAnimator.ofFloat(mHeaderShadeView, "alpha", 1f).setDuration(400);
				shadeAnim.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator anim) {
						mFixedItinTypeImageView.setDrawAlpha(SHADED_ITIN_TYPE_ALPHA);
					}
				});
				animators.add(shadeAnim);
				// TODO: this results in flicker
				// animators.add(ObjectAnimator.ofInt(mFixedItinTypeImageView, "drawAlpha", SHADED_ITIN_TYPE_ALPHA));
			}
			else {
				mFixedItinTypeImageView.setDrawAlpha(SHADED_ITIN_TYPE_ALPHA);
			}
		}

		// Type Icon
		if (mItinContentGenerator.getHideDetailsTypeIcon()) {
			if (animate) {
				Animator typeImageAnimator = ObjectAnimator
						.ofFloat(mItinTypeImageView, "alpha", 1)
						.setDuration(400);
				typeImageAnimator.addListener(new AnimatorListenerAdapter() {
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
				mItinTypeImageView.setAlpha(1f);
				mItinTypeImageView.setVisibility(View.VISIBLE);
				mFixedItinTypeImageView.setVisibility(View.GONE);
			}
		}
		// Itin type icon not hidden (for shared itins)
		else {
			float scale = mItinTypeImageView.getScaleX();
			if (animate) {
				// Animate the fixed image smoothly down into the smaller floating Image.
				mItinTypeImageView.setAlpha(0f);
				PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", scale);
				PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", scale);
				ObjectAnimator anim = AnimUtils.ofPropertyValuesHolder(mFixedItinTypeImageView, scaleX, scaleY).setDuration(400);
				anim.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator arg0) {
						mItinTypeImageView.setAlpha(1f);
						mItinTypeImageView.setVisibility(View.VISIBLE);
						mFixedItinTypeImageView.setVisibility(View.GONE);
					}
				});
				animators.add(anim);

				// Make mFixedItinTypeImageView end up aligned with mItinTypeImageView
				animators.add(ObjectAnimator
						.ofFloat(mFixedItinTypeImageView, "translationY", mFixedItinTypeImageTranslation)
						.setDuration(400));
			}
			else {
				mItinTypeImageView.setAlpha(1f);
				mFixedItinTypeImageView.setScaleX(scale);
				mFixedItinTypeImageView.setScaleY(scale);
				mFixedItinTypeImageView.setTranslationY(mFixedItinTypeImageTranslation);
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
			mChevronImageView.setRotation(0f);
		}

		// Putting it all together
		if (animate) {
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			set.addListener(new AnimatorListenerAdapter() {
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

		mHeaderBitmapDrawable.setCornerMode(mShowSummary ? CornerMode.TOP : CornerMode.ALL);

		updateSummaryVisibility();

		mScrollView.scrollTo(0, 0);

		mSummaryDividerView.setVisibility(GONE);
		mDetailsLayout.setVisibility(GONE);

		destroyDetailsView();
	}

	public AnimatorSet expand(boolean animate) {
		// CAUTION: don't setTranslationY here on mTitleLayout.
		// That's already tweaked in updateLayout()

		mDisplayState = DisplayState.EXPANDED;

		mCollapsedTop = getTop();

		inflateDetailsView();
		updateClickable();

		mCardLayout.setTranslationY(0);

		mHeaderBitmapDrawable.setCornerMode(CornerMode.NONE);

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

		// Since we are adding the dropshadow to expanded view for sharedItins, no use in animating.
		if (!mItinContentGenerator.isSharedItin()) {
			//Header image gradient overlay
			if (animate) {
				ObjectAnimator headerOverlayAlphaAnimator = ObjectAnimator
						.ofFloat(mHeaderOverlayImageView, "alpha", 0f)
						.setDuration(200);
				animators.add(headerOverlayAlphaAnimator);
			}
			else {
				mHeaderOverlayImageView.setAlpha(0f);
			}
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
				mHeaderTextLayout.setAlpha(0f);
				mHeaderTextLayout.setTranslationY(-50f);
			}
		}
		else {
			// Even though getHideDetailsTitle() is false,
			// we still want to hide the date text.
			// Also, we'll shift the header text into position here.
			float trans = 20f * getResources().getDisplayMetrics().density;
			if (animate) {
				ObjectAnimator headerDateTextAlphaAnimator = ObjectAnimator
						.ofFloat(mHeaderTextDateView, "alpha", 0f)
						.setDuration(200);
				animators.add(headerDateTextAlphaAnimator);

				if (!mShowSummary) {
					ObjectAnimator headerTextTranslationAnimator = ObjectAnimator
							.ofFloat(mHeaderTextView, "translationY", trans)
							.setDuration(400);
					animators.add(headerTextTranslationAnimator);
				}
			}
			else {
				mHeaderTextDateView.setAlpha(0f);
				if (!mShowSummary) {
					mHeaderTextView.setTranslationY(trans);
				}
			}
		}

		// Header Shade (for past itins)
		if (mShadeCard) {
			if (animate) {
				ObjectAnimator shadeAnim = ObjectAnimator.ofFloat(mHeaderShadeView, "alpha", 0f).setDuration(400);
				shadeAnim.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator anim) {
						mFixedItinTypeImageView.setDrawAlpha(255);
					}
				});
				animators.add(shadeAnim);
				// TODO: this results in flicker
				// animators.add(ObjectAnimator.ofInt(mFixedItinTypeImageView, "drawAlpha", 255));
			}
			else {
				mFixedItinTypeImageView.setDrawAlpha(255);
			}
		}

		// Type icon
		if (mItinContentGenerator.getHideDetailsTypeIcon()) {
			if (animate) {
				ObjectAnimator itinTypeImageAlphaAnimator = ObjectAnimator
						.ofFloat(mItinTypeImageView, "alpha", 0)
						.setDuration(200);
				itinTypeImageAlphaAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animator) {
						mItinTypeImageView.setVisibility(View.INVISIBLE);
					}
				});
				animators.add(itinTypeImageAlphaAnimator);
			}
			else {
				mItinTypeImageView.setAlpha(0f);
				mItinTypeImageView.setVisibility(View.INVISIBLE);
			}
		}
		else {
			// Make mFixedItinTypeImageView start out aligned with mItinTypeImageView
			mFixedItinTypeImageTranslation = mItinTypeImageView.getTop() - mFixedItinTypeImageView.getTop();

			// There's no need to animate anything here if this is the summary card
			if (animate && !mShowSummary) {
				// Animate the floating image smoothly into the full size fixed Image.
				float scale = mItinTypeImageView.getScaleX();

				mFixedItinTypeImageView.setVisibility(View.VISIBLE);
				mItinTypeImageView.setVisibility(View.INVISIBLE);
				PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", scale, 1f);
				PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", scale, 1f);
				ObjectAnimator anim = AnimUtils
						.ofPropertyValuesHolder(mFixedItinTypeImageView, scaleX, scaleY)
						.setDuration(400);
				animators.add(anim);

				animators.add(ObjectAnimator
						.ofFloat(mFixedItinTypeImageView, "translationY", mFixedItinTypeImageTranslation, 0f)
						.setDuration(400));
			}
			else {
				mFixedItinTypeImageView.setScaleX(1f);
				mFixedItinTypeImageView.setScaleY(1f);
				mFixedItinTypeImageView.setTranslationY(0f);
				mItinTypeImageView.setAlpha(0f);
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
			mChevronImageView.setRotation(180f);
		}

		// Putting it all together
		if (animate) {
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			set.addListener(new AnimatorListenerAdapter() {
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
			if (mShowSummary) {
				// This is 9dp, which works, but, this whole method is a nasty TODO cluster
				typeImageY -= 9 * getResources().getDisplayMetrics().density;
			}

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

			mItinTypeImageView.setTranslationY(typeImageTranslationY);
			mItinTypeImageView.setScaleX(percentIcon);
			mItinTypeImageView.setScaleY(percentIcon);
			mHeaderTextLayout.setTranslationY((mItinTypeImageView.getHeight() * percentIcon) / 2);
			mCardLayout.setTranslationY(viewTranslationY);
		}
		else if (mShowSummary
				&& mHeaderTextLayout.getTranslationY() == 0
				&& mFixedItinTypeImageView.getVisibility() == View.VISIBLE
				&& mHeaderTextLayout.getVisibility() == View.VISIBLE) {
			//If we are in expanded mode, and are making use the of the fixtedItinTypeImageView we need to ensure that the text is positioned below it
			//not underneath it. This fixes a rotation bug when a card is expanded and mShowSummary == true

			int height = mFixedItinTypeImageView.getHeight();
			int padding = mFixedItinTypeImageView.getPaddingBottom();
			float centerOffset = (height * 0.5f) - ((height - padding) * 0.5f);
			float translationY = centerOffset * 1.1f;//Add a little extra space between icon and text
			mHeaderTextLayout.setTranslationY(translationY);
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
			case R.id.itin_overflow_image_button: {
				onOverflowButtonClicked(v);
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

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itin_card_share:
			showShareDialog();
			return true;
		case R.id.itin_card_add_to_calendar:
			addToCalendar();
			return true;
		default:
			return false;
		}
	}

	private void onOverflowButtonClicked(View anchor) {
		PopupMenu popup = new PopupMenu(getContext(), anchor);
		popup.setOnMenuItemClickListener(this);

		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_itin_expanded_overflow, popup.getMenu());

		// Only show add to calendar on devices and card types that are supported
		if (CalendarAPIUtils.deviceSupportsCalendarAPI(getContext())) {
			List<Intent> intents = mItinContentGenerator.getAddToCalendarIntents();
			if (intents.isEmpty()) {
				popup.getMenu().removeItem(R.id.itin_card_add_to_calendar);
			}
		}
		else {
			popup.getMenu().removeItem(R.id.itin_card_add_to_calendar);
		}

		popup.show();
	}

	private void showShareDialog() {
		ShareUtils shareUtils = new ShareUtils(getContext());
		mShareView.setShareIntent(shareUtils.getShareIntents(mItinContentGenerator));
		mShareView.setOnShareTargetSelectedListener(this);
		mShareView.showPopup();
	}

	private void addToCalendar() {
		List<Intent> intents = mItinContentGenerator.getAddToCalendarIntents();
		for (Intent intent : intents) {
			getContext().startActivity(intent);
		}
	}

	@Override
	public void onShareTargetSelected(ShareView view, Intent intent) {
		OmnitureTracking.trackItinShareNew(getContext(), mItinContentGenerator.getType(), intent);
	}

}
