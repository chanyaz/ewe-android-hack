package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

public class SlideToWidgetLL extends RelativeLayout {

	private ImageView mTouchTarget;
	private Space mDestinationSpace;
	private TextView mSliderText;

	// Touch mask
	private Bitmap mArrowMask;
	private Bitmap mCheckMask;
	private final Paint mArrowPaint = new Paint();
	private final Paint mCheckPaint = new Paint();
	private final Paint mGreenPaint = new Paint();
	private final Paint mPaintBackground = new Paint();

	private float mPartialSlide = -1;
	private boolean mIsSliding = false;
	private boolean mHitDestination = false;

	private RectF mRectF;

	public SlideToWidgetLL(Context context) {
		this(context, null);
	}

	public SlideToWidgetLL(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public SlideToWidgetLL(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View widget = inflater.inflate(R.layout.widget_slide_to_ll, this);

		mTouchTarget = Ui.findView(widget, R.id.touch_target);
		mSliderText = Ui.findView(widget, R.id.slider_text);
		mDestinationSpace = Ui.findView(widget, R.id.destination_space);

		setWillNotDraw(false);

		mTouchTarget.setOnTouchListener(new SliderTouchListener());

		// Setup alpha mask
		mArrowMask = BitmapFactory.decodeResource(getResources(), R.drawable.slide_arrow_cars);
		mCheckMask = BitmapFactory.decodeResource(getResources(), R.drawable.slide_check_cars);
		mPaintBackground.setStyle(Paint.Style.FILL);
		mPaintBackground.setAntiAlias(true);
		mPaintBackground.setColor(Ui.obtainThemeColor(getContext(), R.attr.primary_color));
		mGreenPaint.setStyle(Paint.Style.FILL);
		mGreenPaint.setAntiAlias(true);
		mGreenPaint.setColor(getResources().getColor(R.color.cars_checkmark_color));
		resetSlider();
	}

	public void resetSlider() {
		abortSlide();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (mRectF == null) {
			Rect rect = new Rect(mTouchTarget.getLeft(), mTouchTarget.getTop(), mDestinationSpace.getRight(), mTouchTarget.getBottom());
			mRectF = new RectF(rect);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRoundRect(mRectF, mTouchTarget.getMeasuredWidth() / 2, mTouchTarget.getMeasuredHeight() / 2, mPaintBackground);
		canvas.drawRoundRect(mRectF, mTouchTarget.getMeasuredWidth() / 2, mTouchTarget.getMeasuredHeight() / 2, mGreenPaint);

		if (mArrowMask != null && mIsSliding) {
			int shift = (getHeight() - mArrowMask.getHeight()) / 2;
			canvas.drawBitmap(mArrowMask, mPartialSlide, shift, mArrowPaint);
			canvas.drawBitmap(mCheckMask, mPartialSlide, shift, mCheckPaint);
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

	public void hideTouchTarget() {
		mTouchTarget.setVisibility(View.INVISIBLE);

		RelativeLayout.LayoutParams mLayoutParams = (RelativeLayout.LayoutParams) mSliderText.getLayoutParams();
		mLayoutParams.removeRule(RelativeLayout.RIGHT_OF);
		mLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mSliderText.setAlpha(1f);
		mSliderText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
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
				mTotalSlide = mDestinationSpace.getRight() - mTouchTarget.getWidth();

				break;
			}
			case MotionEvent.ACTION_MOVE: {
				float pixels = event.getX() - mInitialTouchOffsetX;
				pixels = Math.min(mTotalSlide, Math.max(0, pixels));

				int backgroundAlpha = (int) (pixels / mTotalSlide * 255f);
				mGreenPaint.setAlpha(backgroundAlpha);
				mArrowPaint.setAlpha(Math.abs(backgroundAlpha - 255));
				mCheckPaint.setAlpha(backgroundAlpha);

				float alpha = pixels / (mTotalSlide * 2);
				mSliderText.setAlpha(Math.abs(alpha - .5f));

				// Don't fire slide progress over and over if the progress hasn't changed
				if (pixels != mPartialSlide) {
					mPartialSlide = pixels;

					if (mPartialSlide == mTotalSlide) {
						if (!mHitDestination) {
							performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
							mHitDestination = true;
						}
					}
					else {
						if (mHitDestination) {
							mHitDestination = false;
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

	private void activateSlide() {
		mIsSliding = true;
		mHitDestination = false;

		if (mTouchTarget != null) {
			mTouchTarget.setVisibility(INVISIBLE);
		}
	}

	private void abortSlide() {
		mIsSliding = false;
		mHitDestination = false;

		mArrowPaint.setAlpha(255);
		mCheckPaint.setAlpha(0);
		mPaintBackground.setAlpha(255);
		mGreenPaint.setAlpha(0);

		if (mTouchTarget != null) {
			mTouchTarget.setVisibility(VISIBLE);
		}
		if (mSliderText != null) {
			mSliderText.setVisibility(View.VISIBLE);
			mSliderText.setAlpha(1f);
		}

		mPartialSlide = -1;
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

	public void fireSlideAllTheWay() {
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
