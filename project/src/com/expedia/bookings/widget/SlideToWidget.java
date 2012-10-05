package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
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
	private ImageView mHiddenImage;
	private TextView mSliderText;

	private int mContainerWidth;
	private int mImageWidth;
	private int mMaxLeftMargin;
	private int mTargetLeftMargin;

	private List<ISlideToListener> mSlideToListeners = new ArrayList<ISlideToListener>();

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
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View widget = inflater.inflate(R.layout.widget_slide_to, this);

		mContainer = this;
		mSlider = Ui.findView(widget, R.id.slider_image);
		mSliderText = Ui.findView(widget, R.id.slider_text);
		mHiddenImage = Ui.findView(widget, R.id.hidden_image);

		mSlider.setOnTouchListener(new SliderTouchListener());

		if (attr != null) {
			TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.SlideToWidget, 0, 0);
			Drawable drawable = ta.getDrawable(R.styleable.SlideToWidget_sliderImage);
			mSlider.setImageDrawable(drawable);
			mHiddenImage.setImageDrawable(drawable);
			mSliderText.setText(ta.getText(R.styleable.SlideToWidget_sliderText));
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

	public void setTextAlpha(float alpha) {
		setTextViewAlpha(mSliderText, alpha);
	}

	public void setSlideDrawable(Drawable drawable) {
		mSlider.setImageDrawable(drawable);
	}

	/**
	 * Resets the slider position and the text alpha
	 */
	public void resetSlider() {
		if (mSlider != null && mSlider.getLayoutParams() != null) {
			RelativeLayout.LayoutParams sliderParams = (LayoutParams) mSlider.getLayoutParams();
			sliderParams.leftMargin = 0;
			mSlider.setLayoutParams(sliderParams);
		}
		if (mSliderText != null) {
			setTextViewAlpha(mSliderText, 1);
		}
	}

	private void updateMeasurements() {
		mContainerWidth = mContainer.getWidth();
		mImageWidth = mSlider.getWidth();
		mMaxLeftMargin = mContainerWidth - mImageWidth;
		mTargetLeftMargin = mMaxLeftMargin - (mImageWidth / 2);//doesn't have to be all the way over...
	}

	private float getSliderPercentage(int sliderLeft, int maxRight) {
		return ((float) sliderLeft) / maxRight;
	}

	private class SliderTouchListener implements OnTouchListener {
		int touchOffsetX;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			RelativeLayout.LayoutParams sliderParams = (LayoutParams) mSlider.getLayoutParams();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//We do this here, because they can't click until the thing is drawn
				updateMeasurements();
				touchOffsetX = Math.round(event.getX());
				fireSlideStart();

				break;
			case MotionEvent.ACTION_MOVE:
				int change = Math.round(event.getX()) - touchOffsetX;

				sliderParams.leftMargin += change;

				if (sliderParams.leftMargin < 0) {
					sliderParams.leftMargin = 0;
				}

				if (sliderParams.leftMargin > mMaxLeftMargin) {
					sliderParams.leftMargin = mMaxLeftMargin;
				}

				setTextViewAlpha(mSliderText, 1.0f - getSliderPercentage(sliderParams.leftMargin, mMaxLeftMargin));

				mSlider.setLayoutParams(sliderParams);

				break;
			case MotionEvent.ACTION_UP:
				if (sliderParams.leftMargin < mTargetLeftMargin) {
					sliderParams.leftMargin = 0;
					fireSlideAbort();
				}
				else {
					sliderParams.leftMargin = mMaxLeftMargin;
					fireSlideAllTheWay();
				}
				setTextViewAlpha(mSliderText, 1.0f - getSliderPercentage(sliderParams.leftMargin, mMaxLeftMargin));
				mSlider.setLayoutParams(sliderParams);

				break;
			default:
				return false;
			}
			return true;
		}
	}

	/**
	 * Lame alpha setter for TextViews pre api 11
	 * @param view
	 * @param alpha
	 */
	private void setTextViewAlpha(TextView view, float alpha) {
		if (view instanceof TextView) {
			int iAlpha = Math.round(alpha * 255);
			TextView tv = (TextView) view;
			tv.setTextColor(tv.getTextColors().withAlpha(iAlpha));
			if (tv.getBackground() != null) {
				tv.getBackground().setAlpha(iAlpha);
			}
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
		public void onSlideStart();

		/**
		 * If the user slides the widget all the way over.
		 */
		public void onSlideAllTheWay();

		/**
		 * If the user starts a slide, but doesn't make it all the way, and the slide is reset
		 */
		public void onSlideAbort();
	}

}
