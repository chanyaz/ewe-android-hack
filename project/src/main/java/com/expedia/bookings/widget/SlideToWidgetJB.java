package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

/**
 * Implements a Slide-to-* widget with visual styling similar to the Jelly Bean lock screen.
 *
 * Created by Doug Melton on 3/10/14.
 */
public class SlideToWidgetJB extends RelativeLayout {

	private ImageView mTouchTarget;
	private ImageView mDestinationImage;
	private View mSliderLine;
	private TextView mSliderText;

	// Dots mask
	private Bitmap mMask;
	private final Paint mPaint = new Paint();
	private final Paint mPaintBackground = new Paint();

	private float mPartialSlide = -1;
	private boolean mIsSliding = false;
	private boolean mHitDestination = false;

	public SlideToWidgetJB(Context context) {
		this(context, null);
	}

	public SlideToWidgetJB(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public SlideToWidgetJB(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attr) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View widget = inflater.inflate(R.layout.widget_slide_to_jb, this);

		mTouchTarget = Ui.findView(widget, R.id.touch_target);
		mSliderLine = Ui.findView(widget, R.id.slider_line);
		mSliderText = Ui.findView(widget, R.id.slider_text);
		mDestinationImage = Ui.findView(widget, R.id.destination_image);

		setWillNotDraw(false);

		mTouchTarget.setOnTouchListener(new SliderTouchListener());

		if (attr != null) {
			TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.SlideToWidget, 0, 0);
			setText(ta.getText(R.styleable.SlideToWidget_sliderText));
			mPaintBackground.setColor(ta.getColor(R.styleable.SlideToWidget_sliderBackgroundColor, Color.TRANSPARENT));
			Drawable drawable = ta.getDrawable(R.styleable.SlideToWidget_sliderImage);
			if (drawable != null) {
				mTouchTarget.setImageDrawable(drawable);
			}
			ta.recycle();
		}

		// Setup alpha mask
		mMask = convertToAlphaMask(BitmapFactory.decodeResource(getResources(), R.drawable.slide_dots_mask));
		Bitmap targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.slide_dots_pattern);
		Shader targetShader = new BitmapShader(targetBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mPaint.setShader(targetShader);
		mPaintBackground.setStyle(Paint.Style.FILL);
		mPaintBackground.setAntiAlias(true);

		resetSlider();
	}

	public void resetSlider() {
		abortSlide();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Rect rect = new Rect(mTouchTarget.getLeft(), mTouchTarget.getTop(), mDestinationImage.getRight(),
			mDestinationImage.getBottom());
		RectF rectF = new RectF(rect);
		canvas.drawRoundRect(rectF, mDestinationImage.getMeasuredHeight() / 2, mDestinationImage.getMeasuredHeight() / 2, mPaintBackground);

		if (mMask != null && mIsSliding && !mHitDestination) {
			int shift = (getHeight() - mMask.getHeight()) / 2;
			canvas.drawBitmap(mMask, mPartialSlide, shift, mPaint);
		}
	}

	/*
	 * Getters and setters
	 */

	public void setText(CharSequence text) {
		mSliderText.setText(text);
	}

	public void setText(int resId) {
		mSliderText.setText(resId);
	}

	/*
	 * Sliding motion
	 */

	private class SliderTouchListener implements OnTouchListener {
		private float mInitialTouchOffsetX;
		private float mTotalSlide;

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mInitialTouchOffsetX = event.getX();
				activateSlide();
				fireSlideStart();

				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				mHitDestination = false;

				mPartialSlide = 0;
				mTotalSlide = mDestinationImage.getLeft() - mTouchTarget.getLeft() - mTouchTarget.getWidth() / 2;

				mSliderLine.setPivotX(calculateSliderLineWidth());

				break;
			}
			case MotionEvent.ACTION_MOVE: {
				float pixels = event.getX() - mInitialTouchOffsetX;
				pixels = Math.min(mTotalSlide, Math.max(0, pixels));

				// Don't fire slide progress over and over if the progress hasn't changed
				if (pixels != mPartialSlide) {
					mPartialSlide = pixels;

					mSliderLine.setScaleX(1.0f - (mPartialSlide / mTotalSlide));

					if (mPartialSlide == mTotalSlide) {
						if (!mHitDestination) {
							performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							mHitDestination = true;
							mDestinationImage.setImageResource(R.drawable.slide_finished_checked);
						}
					}
					else {
						if (mHitDestination) {
							mHitDestination = false;
							mDestinationImage.setImageResource(R.drawable.slide_finish_unchecked);
						}
					}
					fireSlideProgress(mPartialSlide, mTotalSlide);
				}

				break;
			}
			case MotionEvent.ACTION_UP: {

				if (mHitDestination) {
					fireSlideAllTheWay();
				}
				else {
					float pixels = event.getX() - mInitialTouchOffsetX;
					pixels = Math.min(mTotalSlide, Math.max(0, pixels));

					ValueAnimator anim = ValueAnimator.ofFloat(pixels, 0);
					anim.setDuration(200);
					anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator valueAnimator) {
							mPartialSlide = (float)valueAnimator.getAnimatedValue();

							mSliderLine.setScaleX(1.0f - (mPartialSlide / mTotalSlide));
							invalidate();

							fireSlideProgress(mPartialSlide, mTotalSlide);
						}
					});
					anim.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							abortSlide();
							fireSlideAbort();
						}
					});
					anim.start();
				}

				break;
			}
			default:
				return false;
			}
			invalidate();
			return true;
		}
	}

	private static Bitmap convertToAlphaMask(Bitmap inBitmap) {
		Bitmap outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), Bitmap.Config.ALPHA_8);
		Canvas canvas = new Canvas(outBitmap);
		canvas.drawBitmap(inBitmap, 0.0f, 0.0f, null);
		return outBitmap;
	}

	private void activateSlide() {
		mIsSliding = true;
		mHitDestination = false;

		if (mTouchTarget != null) {
			mTouchTarget.setVisibility(View.INVISIBLE);
		}
		if (mSliderText != null) {
			mSliderText.setVisibility(View.INVISIBLE);
		}
		if (mDestinationImage != null) {
			mDestinationImage.setImageResource(R.drawable.slide_finish_unchecked);
		}

		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(getDrawLineAnimator(), getShowDestAnimator());
		animSet.setDuration(100);
		animSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				mSliderLine.setVisibility(View.VISIBLE);
				mDestinationImage.setVisibility(View.VISIBLE);
			}
		});
		animSet.start();
	}

	private void abortSlide() {
		mIsSliding = false;
		mHitDestination = false;

		if (mTouchTarget != null) {
			mTouchTarget.setVisibility(View.VISIBLE);
		}
		if (mSliderText != null) {
			mSliderText.setVisibility(View.VISIBLE);
		}
		if (mSliderLine != null) {
			mSliderLine.setVisibility(View.INVISIBLE);
		}
		if (mDestinationImage != null) {
			mDestinationImage.setVisibility(View.INVISIBLE);
			mDestinationImage.setImageResource(R.drawable.slide_finish_unchecked);
		}

		mPartialSlide = -1;
	}

	private int calculateSliderLineWidth() {
		LayoutParams params = (LayoutParams) mSliderLine.getLayoutParams();
		return getWidth() - params.leftMargin - params.rightMargin;
	}

	private Animator getDrawLineAnimator() {
		LayoutParams params = (LayoutParams) mSliderLine.getLayoutParams();
		int padding = (int)(12 * getResources().getDisplayMetrics().density);
		params.leftMargin = mTouchTarget.getRight() - padding;
		params.rightMargin = getWidth() - mDestinationImage.getLeft() - padding;
		params.width = calculateSliderLineWidth();

		mSliderLine.setLayoutParams(params);

		mSliderLine.setPivotX(0);

		return ObjectAnimator.ofFloat(mSliderLine, "scaleX", 0, 1);
	}

	/**
	 * This animation gets run when the touch target is touched from a rest state.
	 *
	 * @return
	 */
	private Animator getShowDestAnimator() {
		float xstart = -getWidth() / 3;
		PropertyValuesHolder trans = PropertyValuesHolder.ofFloat("translationX", xstart, 0f);
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
		return ObjectAnimator.ofPropertyValuesHolder(mDestinationImage, trans, alpha);
	}

	/*
	 * Listeners
	 */

	private final List<ISlideToListener> mSlideToListeners = new ArrayList<ISlideToListener>();

	public boolean addSlideToListener(ISlideToListener listener) {
		return mSlideToListeners.add(listener);
	}

	protected void fireSlideStart() {
		for (ISlideToListener listener : mSlideToListeners) {
			listener.onSlideStart();
		}
	}

	protected void fireSlideProgress(float pixels, float total) {
		for (ISlideToListener listener : mSlideToListeners) {
			listener.onSlideProgress(pixels, total);
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
		 * The slider has slid partially across. The fraction of the distance can be computed
		 * by: pixels / total.
		 */
		void onSlideProgress(float pixels, float total);

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
