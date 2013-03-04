package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.animation.ResizeAnimation;
import com.expedia.bookings.animation.ResizeAnimator;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.Insurance.InsuranceLineOfBusiness;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.ViewHelper;

@SuppressWarnings("unchecked")
public abstract class ItinCard<T extends ItinCardData> extends RelativeLayout {
	//////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	public interface OnItinCardClickListener {
		public void onCloseButtonClicked();

		public void onShareButtonClicked(String subject, String shortMessage, String longMessage);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC ENUMS
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

	protected abstract String getShareSubject(T itinCardData);

	protected abstract String getShareTextShort(T itinCardData);

	protected abstract String getShareTextLong(T itinCardData);

	// Header image

	protected abstract int getHeaderImagePlaceholderResId();

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

		public SummaryButton(int iconResId, int textResId, OnClickListener onClickListener) {
			this(iconResId, getContext().getString(textResId), onClickListener);
		}

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

	private OnItinCardClickListener mOnItinCardClickListener;

	private T mItinCardData;

	private DisplayState mDisplayState = DisplayState.COLLAPSED;
	private boolean mShowSummary;
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
	private ViewGroup mSummaryLayout;
	private ViewGroup mDetailsLayout;
	private ViewGroup mActionButtonLayout;

	private ImageView mItinTypeImageView;

	private ScrollView mScrollView;
	private OptimizedImageView mHeaderImageView;
	private ImageView mHeaderOverlayImageView;
	private TextView mHeaderTextView;
	private View mHeaderShadeView;
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
		mSummaryLayout = Ui.findView(this, R.id.summary_layout);
		mDetailsLayout = Ui.findView(this, R.id.details_layout);
		mActionButtonLayout = Ui.findView(this, R.id.action_button_layout);

		mItinTypeImageView = Ui.findView(this, R.id.itin_type_image_view);

		mScrollView = Ui.findView(this, R.id.scroll_view);
		mHeaderImageView = Ui.findView(this, R.id.header_image_view);
		mHeaderOverlayImageView = Ui.findView(this, R.id.header_overlay_image_view);
		mHeaderTextView = Ui.findView(this, R.id.header_text_view);
		mHeaderShadeView = Ui.findView(this, R.id.header_mask);
		mSummaryDividerView = Ui.findView(this, R.id.summary_divider_view);

		mSummaryLeftButton = Ui.findView(this, R.id.summary_left_button);
		mSummaryRightButton = Ui.findView(this, R.id.summary_right_button);

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

	public void bind(final ItinCardData itinCardData) {
		final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		mItinCardData = (T) itinCardData;

		// Title
		View titleView = getTitleView(layoutInflater, mTitleContentLayout, mItinCardData);
		if (titleView != null) {
			mTitleContentLayout.removeAllViews();
			mTitleContentLayout.addView(titleView);
		}

		// Type icon
		mItinTypeImageView.setImageResource(getTypeIconResId());

		// Image
		String headerImageUrl = getHeaderImageUrl(mItinCardData);
		if (headerImageUrl != null) {
			UrlBitmapDrawable.loadImageView(headerImageUrl, mHeaderImageView);
		}
		else {
			mHeaderImageView.setImageResource(getHeaderImagePlaceholderResId());
		}

		// Header text
		mHeaderTextView.setText(getHeaderText(mItinCardData));

		// Summary text
		View summaryView = getSummaryView(layoutInflater, mSummaryLayout, mItinCardData);
		if (summaryView != null) {
			mSummaryLayout.removeAllViews();
			mSummaryLayout.addView(summaryView);
		}

		// Buttons
		SummaryButton leftButton = getSummaryLeftButton(mItinCardData);
		if (leftButton != null) {
			mSummaryLeftButton.setCompoundDrawablesWithIntrinsicBounds(leftButton.getIconResId(), 0, 0, 0);
			mSummaryLeftButton.setText(leftButton.getText());
			mSummaryLeftButton.setOnClickListener(leftButton.getOnClickListener());
		}

		SummaryButton rightButton = getSummaryRightButton(mItinCardData);
		if (rightButton != null) {
			mSummaryRightButton.setCompoundDrawablesWithIntrinsicBounds(rightButton.getIconResId(), 0, 0, 0);
			mSummaryRightButton.setText(rightButton.getText());
			mSummaryRightButton.setOnClickListener(rightButton.getOnClickListener());
		}

		mSummaryLeftButton.setVisibility(leftButton != null ? VISIBLE : GONE);
		mSummaryRightButton.setVisibility(rightButton != null ? VISIBLE : GONE);
		Ui.findView(this, R.id.action_button_divider).setVisibility(
				(leftButton != null && rightButton != null) ? VISIBLE : GONE);

		//Shade
		if (mShadeCard) {
			float shadeAlpha = 0.5f;
			mHeaderShadeView.setVisibility(View.VISIBLE);
			ViewHelper.setAlpha(mItinTypeImageView, shadeAlpha);
		}
		else {
			mHeaderShadeView.setVisibility(View.GONE);
			ViewHelper.setAlpha(mItinTypeImageView, 1f);
		}
	}

	public void inflateDetailsView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		View detailsView = getDetailsView(layoutInflater, mDetailsLayout, mItinCardData);
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

	public void setCardShaded(boolean shade) {
		mShadeCard = shade;
	}

	public AnimatorSet collapse(boolean startAnimation) {
		mDisplayState = DisplayState.COLLAPSED;
		updateClickable();

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

		ValueAnimator titleResizeAnimator = ResizeAnimator.buildResizeAnimator(mTitleLayout, mTitleLayoutHeight, 0);
		titleResizeAnimator.setDuration(300);
		titleResizeAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				updateSummaryVisibility();

				mScrollView.scrollTo(0, 0);

				mSummaryDividerView.setVisibility(GONE);
				mDetailsLayout.setVisibility(GONE);

				destroyDetailsView();

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub

			}

		});
		titleResizeAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				mScrollView.scrollTo(0, (int) (((stopY - startY) * arg0.getAnimatedFraction()) + startY));
			}

		});
		animators.add(titleResizeAnimator);

		animators.add(ResizeAnimator.buildResizeAnimator(mActionButtonLayout, 0).setDuration(
				300));

		// Alpha
		animators.add(ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 1).setDuration(400));
		animators.add(ObjectAnimator.ofFloat(mHeaderTextView, "alpha", 1).setDuration(400));

		mItinTypeImageView.setVisibility(View.VISIBLE);

		animators.add(ObjectAnimator.ofFloat(mItinTypeImageView, "alpha", 0, 1).setDuration(400));

		// TranslationY
		animators.add(ObjectAnimator.ofFloat(mHeaderTextView, "translationY", 0).setDuration(400));

		if (!mShowSummary) {
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderLayout, mMiniCardHeaderImageHeight));
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderImageView, mMiniCardHeaderImageHeight));
		}

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animators);
		return set;

	}

	public AnimatorSet expand(boolean startAnimation) {
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

		AnimatorSet animSet = getExpandAnimatorSet();
		if (startAnimation) {
			animSet.start();
		}
		return animSet;
	}

	private AnimatorSet getExpandAnimatorSet() {

		List<Animator> animators = new ArrayList<Animator>();

		// Alpha
		ObjectAnimator headerOverlayAlphaAnimator = ObjectAnimator.ofFloat(mHeaderOverlayImageView, "alpha", 0)
				.setDuration(200);
		ObjectAnimator headerTextAlphaAnimator = ObjectAnimator.ofFloat(mHeaderTextView, "alpha", 0).setDuration(200);//.start();
		ObjectAnimator itinTypeImageAlphaAnimator = ObjectAnimator.ofFloat(mItinTypeImageView, "alpha", 0);
		itinTypeImageAlphaAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
			}

			@Override
			public void onAnimationRepeat(Animator animator) {
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				mItinTypeImageView.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animator) {
			}
		});
		itinTypeImageAlphaAnimator.setDuration(200);

		// TranslationY
		ObjectAnimator headerTextTranslationAnimator = ObjectAnimator.ofFloat(mHeaderTextView, "translationY", -50)
				.setDuration(400);

		animators.add(headerOverlayAlphaAnimator);
		animators.add(headerTextAlphaAnimator);
		animators.add(itinTypeImageAlphaAnimator);
		animators.add(headerTextTranslationAnimator);

		if (!mShowSummary) {
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderLayout, mExpandedCardHeaderImageHeight));
			animators.add(ResizeAnimator.buildResizeAnimator(mHeaderImageView, mExpandedCardHeaderImageHeight));
		}

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animators);
		return set;
	}

	// Type icon position and siz
	public void updateLayout() {
		int typeImageHeight = mItinTypeImageView.getHeight();
		int typeImageHalfHeight = typeImageHeight / 2;
		int headerImageHeight = mHeaderImageView.getHeight();
		int expandedTypeImageY = headerImageHeight / 2;
		int miniTypeImageY = (int) (headerImageHeight * 0.4f);
		int typeImageY = mShowSummary ? expandedTypeImageY : miniTypeImageY;

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

		final int typeImageTranslationY = mHeaderImageView.getTop() + typeImageY - typeImageHalfHeight;
		final int viewTranslationY = Math.max(0, (headerImageHeight - (int) (percent * (float) headerImageHeight)) / 2);

		ViewHelper.setTranslationY(mItinTypeImageView, typeImageTranslationY);
		ViewHelper.setScaleX(mItinTypeImageView, percentIcon);
		ViewHelper.setScaleY(mItinTypeImageView, percentIcon);

		ViewHelper.setTranslationY(mCardLayout, viewTranslationY);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	protected boolean hasDetails() {
		return true;
	}

	protected T getItinCardData() {
		return mItinCardData;
	}

	/**
	 * Add this trip's insurance to the passed in container
	 * @param inflater
	 * @param insuranceContainer
	 */
	protected void addInsuranceRows(LayoutInflater inflater, ViewGroup insuranceContainer) {
		if (hasInsurance()) {
			insuranceContainer.removeAllViews();

			int divPadding = getResources().getDimensionPixelSize(R.dimen.itin_flight_segment_divider_padding);
			int viewAddedCount = 0;
			List<Insurance> insuranceList = this.getItinCardData().getTripComponent().getParentTrip()
					.getTripInsurance();

			for (final Insurance insurance : insuranceList) {
				//Air insurance should only be added for flights, other types should be added to all itin card details
				if (!insurance.getLineOfBusiness().equals(InsuranceLineOfBusiness.AIR) || getType().equals(Type.FLIGHT)) {
					if (viewAddedCount > 0) {
						insuranceContainer.addView(getHorizontalDividerView(divPadding));
					}
					View insuranceRow = inflater.inflate(R.layout.snippet_itin_insurance_row, null);
					TextView insuranceName = Ui.findView(insuranceRow, R.id.insurance_name);
					insuranceName.setText(Html.fromHtml(insurance.getPolicyName()).toString());

					View insuranceLinkView = Ui.findView(insuranceRow, R.id.insurance_button);
					insuranceLinkView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
							builder.setUrl(insurance.getTermsUrl());
							builder.setTheme(R.style.FlightTheme);
							builder.setTitle(R.string.insurance);
							getContext().startActivity(builder.getIntent());
						}
					});

					insuranceContainer.addView(insuranceRow);
					viewAddedCount++;
				}
			}
		}
	}

	/**
	 * Does this particular card have displayable insurance info
	 * @return
	 */
	protected boolean hasInsurance() {
		boolean hasInsurance = false;
		if (this.getItinCardData() != null && this.getItinCardData().getTripComponent() != null
				&& this.getItinCardData().getTripComponent().getParentTrip() != null) {

			List<Insurance> insuranceList = this.getItinCardData().getTripComponent().getParentTrip()
					.getTripInsurance();
			if (insuranceList != null && insuranceList.size() > 0) {
				for (int i = 0; i < insuranceList.size(); i++) {
					Insurance ins = insuranceList.get(i);
					if (ins.getLineOfBusiness().equals(InsuranceLineOfBusiness.AIR) && getType().equals(Type.FLIGHT)) {
						hasInsurance = true;
					}
					else if (!ins.getLineOfBusiness().equals(InsuranceLineOfBusiness.AIR)) {
						hasInsurance = true;
					}
				}
			}
		}
		return hasInsurance;
	}

	/**
	 * Get a horizontal divider view with the itin divider color 
	 * @return
	 */
	protected View getHorizontalDividerView(int margin) {
		View v = new View(this.getContext());
		v.setBackgroundColor(getResources().getColor(R.color.itin_divider_color));
		int divHeight = getResources().getDimensionPixelSize(R.dimen.one_px_hdpi_two_px_xhdpi);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, divHeight);
		lp.leftMargin = margin;
		lp.rightMargin = margin;
		lp.topMargin = margin;
		lp.bottomMargin = margin;
		v.setLayoutParams(lp);
		return v;
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
					String subject = getShareSubject(mItinCardData);
					String shortMessage = getShareTextShort(mItinCardData);
					String longMessage = getShareTextLong(mItinCardData);

					mOnItinCardClickListener.onShareButtonClicked(subject, shortMessage, longMessage);

					switch (getType()) {
					case FLIGHT:
						OmnitureTracking.trackItinFlightShare(getContext());
						break;
					case HOTEL:
						OmnitureTracking.trackItinHotelShare(getContext());
						break;
					case CAR:
						OmnitureTracking.trackItinCarShare(getContext());
						break;
					case ACTIVITY:
						OmnitureTracking.trackItinActivityShare(getContext());
						break;
					}
				}
				break;
			}
			}
		}
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("ITIN: ItinCard.onTouchEvent");
		return super.onTouchEvent(event);
	}
}
