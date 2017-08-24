package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class SlideToWidget extends RelativeLayout {

	private RelativeLayout mContainer;
	private ImageView mSlider;
	private ImageView mDestImage;
	private ImageView mHiddenImage;
	private ImageView mSliderHolder;
	private TextView mSliderText;
	private View mSliderLine;
	private View mSliderDot;

	private Drawable mSliderDrawable;
	private Drawable mDragingDrawable;
	private Drawable mSlideGoalDrawable;
	private Drawable mSlideCompleteDrawable;

	private int mContainerWidth;
	private int mImageWidth;
	private int mMaxLeftMargin;
	private int mTargetLeftMargin;

	private final List<ISlideToListener> mSlideToListeners = new ArrayList<ISlideToListener>();

	private boolean mPerformedHapticForTarget;

	public SlideToWidget(Context context) {
		super(context);
		init(context, null);
	}

	public SlideToWidget(Context context, AttributeSet attr) {
		super(context, attr);
		init(context, attr);
	}

	public SlideToWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attr) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View widget = inflater.inflate(R.layout.widget_slide_to, this);

		mContainer = this;
		mSlider = Ui.findView(widget, R.id.slider_image);
		mSliderText = Ui.findView(widget, R.id.slider_text);
		mDestImage = Ui.findView(widget, R.id.destination_image);
		mHiddenImage = Ui.findView(widget, R.id.hidden_image);
		mSliderLine = Ui.findView(widget, R.id.slider_line);
		mSliderDot = Ui.findView(widget, R.id.slider_line_start_dot);
		mSliderHolder = Ui.findView(widget, R.id.slider_image_holder);
		mSlider.setOnTouchListener(new SliderTouchListener());

		if (attr != null) {
			TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.SlideToWidget, 0, 0);
			mSliderDrawable = ta.getDrawable(R.styleable.SlideToWidget_sliderImage);
			mDragingDrawable = ta.getDrawable(R.styleable.SlideToWidget_dragImage);

			mSlideGoalDrawable = ta.getDrawable(R.styleable.SlideToWidget_destImage);
			mSlideCompleteDrawable = ta.getDrawable(R.styleable.SlideToWidget_destComplete);

			mSliderLine.setBackgroundColor(ta.getColor(R.styleable.SlideToWidget_lineColor, Color.rgb(255, 255, 255)));

			mSlider.setImageDrawable(mSliderDrawable);
			mSliderHolder.setImageDrawable(mSliderDrawable);
			mHiddenImage.setImageDrawable(mDragingDrawable);
			mSliderText.setText(ta.getText(R.styleable.SlideToWidget_sliderText));
			mDestImage.setImageDrawable(mSlideGoalDrawable);

			ta.recycle();
		}
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

	public boolean addSlideToListener(ISlideToListener listener) {
		return mSlideToListeners.add(listener);
	}

	public boolean removeSlideToListener(ISlideToListener listener) {
		return mSlideToListeners.remove(listener);
	}

	public void clearSlideToListeners() {
		mSlideToListeners.clear();
	}

	public void setText(CharSequence text) {
		mSliderText.setText(text);
	}

	public void setSlideDrawable(Drawable drawable) {
		mSlider.setImageDrawable(drawable);
	}

	/**
	 * Resets the slider position and the text alpha
	 */
	public void resetSlider() {
		if (mSlider != null) {
			mSlider.setImageDrawable(mSliderDrawable);
			if (mSlider.getLayoutParams() != null) {
				RelativeLayout.LayoutParams sliderParams = (LayoutParams) mSlider.getLayoutParams();
				sliderParams.leftMargin = 0;
				mSlider.setLayoutParams(sliderParams);
			}
			mSlider.setVisibility(View.VISIBLE);
		}
		if (mDestImage != null) {
			mDestImage.clearAnimation();
			mDestImage.setVisibility(View.INVISIBLE);
			mDestImage.setImageDrawable(mSlideGoalDrawable);
		}
		if (mSliderText != null) {
			mSliderText.setVisibility(View.VISIBLE);
		}
		if (mSliderLine != null) {
			LayoutParams lineParams = (LayoutParams) mSliderLine.getLayoutParams();
			lineParams.width = 0;
			mSliderLine.setLayoutParams(lineParams);
			mSliderLine.setVisibility(View.INVISIBLE);
			mSliderLine.clearAnimation();
		}
		if (mSliderDot != null) {
			mSliderDot.setVisibility(View.INVISIBLE);
			mSliderDot.clearAnimation();
		}
	}

	public void activateSlide() {
		if (mSlider != null) {
			mSlider.setImageDrawable(mDragingDrawable);
		}
		if (mSliderText != null) {
			mSliderText.setVisibility(View.INVISIBLE);
		}
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(getDrawLineAnimator(), getShowDestAnimator());
		animSet.setDuration(500);
		animSet.start();
	}

	private void updateMeasurements() {
		mContainerWidth = mContainer.getWidth();
		mImageWidth = mHiddenImage.getWidth();
		mMaxLeftMargin = mContainerWidth - mImageWidth;
		mTargetLeftMargin = mMaxLeftMargin - (mImageWidth / 2);//doesn't have to be all the way over...
	}

	@SuppressLint("NewApi")
	private Animator getDrawLineAnimator() {
		LayoutParams params = (LayoutParams) mSliderLine.getLayoutParams();
		int margin = mHiddenImage.getWidth() / 2;
		params.width = mContainerWidth - 2 * margin;
		params.leftMargin = margin;
		params.rightMargin = margin;

		mSliderLine.setLayoutParams(params);

		LayoutParams dotParams = (LayoutParams) mSliderDot.getLayoutParams();
		dotParams.leftMargin = mHiddenImage.getWidth() / 2 - mSliderDot.getWidth() / 2;
		mSliderDot.setLayoutParams(dotParams);

		mSliderLine.setPivotX(0);
		ObjectAnimator drawLine = ObjectAnimator.ofFloat(this.mSliderLine, "scaleX", 0, 1);
		drawLine.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				mSliderLine.setVisibility(View.VISIBLE);
				mSliderDot.setVisibility(View.VISIBLE);
			}
		});
		return drawLine;
	}

	private Animator getShowDestAnimator() {
		ObjectAnimator destAlphaAnimator = ObjectAnimator.ofFloat(this.mDestImage, "alpha", 0, 1);
		destAlphaAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				mDestImage.setVisibility(View.VISIBLE);
			}
		});

		return destAlphaAnimator;
	}

	private class SliderTouchListener implements OnTouchListener {
		int touchOffsetX;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			RelativeLayout.LayoutParams dragParams = (LayoutParams) mSlider.getLayoutParams();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//We do this here, because they can't click until the thing is drawn
				updateMeasurements();
				activateSlide();
				touchOffsetX = Math.round(event.getX());
				fireSlideStart();

				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				mPerformedHapticForTarget = false;

				break;
			case MotionEvent.ACTION_MOVE:
				int change = Math.round(event.getX()) - touchOffsetX;

				dragParams.leftMargin += change;

				if (dragParams.leftMargin < 0) {
					dragParams.leftMargin = 0;
				}

				if (dragParams.leftMargin > mMaxLeftMargin) {
					dragParams.leftMargin = mMaxLeftMargin;
				}

				if (dragParams.leftMargin > mTargetLeftMargin) {
					dragParams.leftMargin = mMaxLeftMargin;
				}

				if (!mPerformedHapticForTarget && dragParams.leftMargin >= mTargetLeftMargin) {
					performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					mPerformedHapticForTarget = true;
				}

				if (dragParams.leftMargin < mTargetLeftMargin) {
					mPerformedHapticForTarget = false;
				}

				mSlider.setLayoutParams(dragParams);
				break;
			case MotionEvent.ACTION_UP:
				if (dragParams.leftMargin < mTargetLeftMargin) {
					dragParams.leftMargin = 0;
					fireSlideAbort();
					resetSlider();
				}
				else {
					dragParams.leftMargin = mMaxLeftMargin;
					mDestImage.setImageDrawable(mSlideCompleteDrawable);
					mSlider.setVisibility(View.INVISIBLE);
					fireSlideAllTheWay();
				}
				mSlider.setLayoutParams(dragParams);

				break;
			default:
				return false;
			}
			return true;
		}
	}

	protected void fireSlideStart() {
		for (ISlideToListener listener : mSlideToListeners) {
			listener.onSlideStart();
		}
	}

	protected void fireSlideAllTheWay() {
		for (ISlideToListener listener : mSlideToListeners) {
			listener.onSlideAllTheWay();
		}
	}

	protected void fireSlideAbort() {
		for (ISlideToListener listener : mSlideToListeners) {
			listener.onSlideAbort();
		}
	}

	public interface ISlideToListener {
		/**
		 * The user has clicked on the slider...
		 */
		void onSlideStart();

		/**
		 * If the user slides the widget all the way over.
		 */
		void onSlideAllTheWay();

		/**
		 * If the user starts a slide, but doesn't make it all the way, and the slide is reset
		 */
		void onSlideAbort();
	}
}
