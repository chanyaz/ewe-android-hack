package com.expedia.bookings.widget;

import java.util.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class DoubleKnobSlider extends LinearLayout {
	//State Keys
	private static final String KEY_KNOB_ONE_PERCENTAGE = "KEY_KNOB_ONE_PERCENTAGE";
	private static final String KEY_KNOB_TWO_PERCENTAGE = "KEY_KNOB_TWO_PERCENTAGE";
	private static final String KEY_SUPER_STATE = "KEY_SUPER_STATE";

	//Values
	private double mKnobOnePercent = 0.2;
	private double mKnobTwoPercent = 0.8;

	//settings
	private float mDragAlpha = 0.5f;
	private int mOverlapMin = 0;
	private boolean mAllowOverlap = true;

	//layout params for knob 1 & 2
	private LayoutParams mOneLp;
	private LayoutParams mTwoLp;

	//Gui controls
	private ViewGroup mContainer;
	private View mKnobOne;
	private View mKnobTwo;

	//State
	private int mContainerWidth;
	private int mKnobOneWidth;
	private int mKnobTwoWidth;
	private int mKnobOneHalfWidth;
	private int mKnobTwoHalfWidth;
	private int mKnobsOverlap;
	private boolean mMeasured = false;

	//Interaction
	private Handler mKnobOneChangeHandler;
	private Handler mKnobTwoChangeHandler;
	private Handler mKnobChangeHandler;

	//Throttle vars
	private int mRedrawMinTimeMs = 400;
	private long mLastKnobOneMs = 0;
	private long mLastKnobTwoMs = 0;

	
	public DoubleKnobSlider(Context context){
		this(context,null);
	}

	public DoubleKnobSlider(Context context, AttributeSet attr) {
		this(context, attr,0);
	}

	public DoubleKnobSlider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View doubleKnobSlider = inflater.inflate(R.layout.widget_double_knob_slider, this);

		mContainer = Ui.findView(doubleKnobSlider, R.id.slider_container);
		mKnobOne = Ui.findView(doubleKnobSlider, R.id.knob_one);
		mKnobTwo = Ui.findView(doubleKnobSlider, R.id.knob_two);

		mKnobOne.setOnTouchListener(new KnobOneTouchListener());
		mKnobTwo.setOnTouchListener(new KnobTwoTouchListener());

		mMeasured = false;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (!mMeasured) {
			mContainerWidth = mContainer.getWidth();
			mKnobOneWidth = mKnobOne.getWidth();
			mKnobTwoWidth = mKnobTwo.getWidth();
			mKnobOneHalfWidth = mKnobOneWidth / 2;
			mKnobTwoHalfWidth = mKnobTwoWidth / 2;
			if (mAllowOverlap) {
				mKnobsOverlap = (mKnobOneHalfWidth + mKnobTwoHalfWidth) - mOverlapMin;
			}
			else {
				mKnobsOverlap = 0;
			}
			if (mContainerWidth > 0) {
				mMeasured = true;
				setKnobPositions();
			}
		}
	}

	@Override
	public Parcelable onSaveInstanceState() {

		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
		bundle.putDouble(KEY_KNOB_ONE_PERCENTAGE, mKnobOnePercent);
		bundle.putDouble(KEY_KNOB_TWO_PERCENTAGE, mKnobTwoPercent);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {

		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;

			mKnobOnePercent = bundle.getDouble(KEY_KNOB_ONE_PERCENTAGE);
			mKnobTwoPercent = bundle.getDouble(KEY_KNOB_TWO_PERCENTAGE);

			super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE));
			return;
		}

		super.onRestoreInstanceState(state);
	}
	
	public void setKnobChangeHandler(Handler handler) {
		mKnobChangeHandler = handler;
	}

	public void setKnobOneChangeHandler(Handler handler) {
		mKnobOneChangeHandler = handler;
	}

	private void notifyKnobOneChange() {
		if (mKnobOneChangeHandler != null) {
			mKnobOneChangeHandler.sendEmptyMessage(0);
		}
		if (mKnobChangeHandler != null) {
			mKnobChangeHandler.sendEmptyMessage(0);
		}
	}

	public void setKnobTwoChangeHandler(Handler handler) {
		mKnobTwoChangeHandler = handler;
	}

	private void notifyKnobTwoChange() {
		if (mKnobTwoChangeHandler != null) {
			mKnobTwoChangeHandler.sendEmptyMessage(0);
		}
		if (mKnobChangeHandler != null) {
			mKnobChangeHandler.sendEmptyMessage(0);
		}
	}

	public double getKnobOnePercentage() {
		return mKnobOnePercent;
	}

	public double getKnobTwoPercentage() {
		return mKnobTwoPercent;
	}
	


	private void updateKnobOnePercentage(boolean useDelay) {
		double fullsize = mContainerWidth - mKnobOneWidth;
		mKnobOnePercent = mKnobOne.getLeft() / fullsize;
		if (mKnobOnePercent < 0) {
			mKnobOnePercent = 0;
		}
		else if (mKnobOnePercent > 1) {
			mKnobOnePercent = 1;
		}
		if (useDelay) {
			long now = Calendar.getInstance().getTimeInMillis();
			if (now - mLastKnobOneMs > mRedrawMinTimeMs) {
				mKnobOne.setLayoutParams(mOneLp);
				mKnobTwo.setLayoutParams(mTwoLp);
				notifyKnobOneChange();
				mLastKnobOneMs = now;
			}
		}
		else {
			notifyKnobOneChange();
		}
	}

	private void updateKnobTwoPercentage(boolean useDelay) {
		double fullsize = mContainerWidth - mKnobTwoWidth;
		mKnobTwoPercent = mKnobTwo.getLeft() / fullsize;
		if (mKnobTwoPercent < 0) {
			mKnobTwoPercent = 0;
		}
		else if (mKnobTwoPercent > 1) {
			mKnobTwoPercent = 1;
		}
		if (useDelay) {
			long now = Calendar.getInstance().getTimeInMillis();
			if (now - mLastKnobTwoMs > mRedrawMinTimeMs) {
				mKnobOne.setLayoutParams(mOneLp);
				mKnobTwo.setLayoutParams(mTwoLp);
				notifyKnobTwoChange();
				mLastKnobTwoMs = now;
			}
		}
		else {
			notifyKnobTwoChange();
		}
		//Log.i("knobTwoPercent:" + knobTwoPercent);
	}

	private void setKnobPositions() {
		mOneLp = (LayoutParams) mKnobOne.getLayoutParams();
		mTwoLp = (LayoutParams) mKnobTwo.getLayoutParams();

		int fullsize = mContainerWidth - mKnobTwoWidth;

		int k1left = (int) Math.round(mKnobOnePercent * fullsize);
		int k2left = (int) Math.round(mKnobTwoPercent * fullsize);

		mOneLp.leftMargin = k1left;
		mTwoLp.leftMargin = k2left - mKnobOneWidth - k1left;

		mKnobOne.setLayoutParams(mOneLp);
		mKnobTwo.setLayoutParams(mTwoLp);
	}

	private class KnobOneTouchListener implements OnTouchListener {

		int touchOffset = 0;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			mOneLp = (LayoutParams) mKnobOne.getLayoutParams();
			mTwoLp = (LayoutParams) mKnobTwo.getLayoutParams();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mKnobOne.setAlpha(mDragAlpha);
				touchOffset = Math.round(event.getX());
				break;
			case MotionEvent.ACTION_MOVE:
				int change = Math.round(event.getX()) - touchOffset;
				int oldK1lpMargin = mOneLp.leftMargin;

				mOneLp.leftMargin += change;

				if (mOneLp.leftMargin < 0) {
					mOneLp.leftMargin = 0;
					if (oldK1lpMargin > 0) {
						mTwoLp.leftMargin += oldK1lpMargin;
					}
				}
				else {
					mTwoLp.leftMargin -= change;
				}

				if (mOneLp.leftMargin + mKnobOneWidth + mKnobTwoWidth - mKnobsOverlap > mContainerWidth) {
					mOneLp.leftMargin = mContainerWidth - mKnobOneWidth - mKnobTwoWidth + mKnobsOverlap;
					mTwoLp.leftMargin = -mKnobsOverlap;
				}

				if (mTwoLp.leftMargin < -mKnobsOverlap)
					mTwoLp.leftMargin = -mKnobsOverlap;

				onMoveUpdate();
				break;
			case MotionEvent.ACTION_UP:
				mKnobOne.setAlpha(1f);
				onUpUpdate();
				break;
			default:
				return false;
			}
			return true;
		}
	}

	private class KnobTwoTouchListener implements OnTouchListener {

		int touchOffset = 0;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			mOneLp = (LayoutParams) mKnobOne.getLayoutParams();
			mTwoLp = (LayoutParams) mKnobTwo.getLayoutParams();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touchOffset = Math.round(event.getX());
				mKnobTwo.setAlpha(mDragAlpha);
				break;
			case MotionEvent.ACTION_MOVE:
				int change = Math.round(event.getX()) - touchOffset;

				while (change != 0) {

					mTwoLp.leftMargin += change;

					if (mTwoLp.leftMargin < -mKnobsOverlap) {
						mOneLp.leftMargin += (mTwoLp.leftMargin + mKnobsOverlap - 1);//still negative
						mTwoLp.leftMargin = -mKnobsOverlap;
					}

					if (mOneLp.leftMargin < 0) {
						mOneLp.leftMargin = 0;
					}

					if (mOneLp.leftMargin + mKnobOneWidth + mTwoLp.leftMargin + mKnobTwoWidth > mContainerWidth) {
						change = mContainerWidth
								- (mOneLp.leftMargin + mKnobOneWidth + mTwoLp.leftMargin + mKnobTwoWidth);
					}
					else {
						change = 0;
					}
				}

				onMoveUpdate();

				break;
			case MotionEvent.ACTION_UP:
				mKnobTwo.setAlpha(1f);
				onUpUpdate();
				break;
			default:
				return false;
			}
			return true;
		}

	}

	private void onMoveUpdate() {
		mKnobOne.setLayoutParams(mOneLp);
		mKnobTwo.setLayoutParams(mTwoLp);

		updateKnobOnePercentage(true);
		updateKnobTwoPercentage(true);
	}

	private void onUpUpdate() {
		updateKnobOnePercentage(false);
		updateKnobTwoPercentage(false);
	}

	//TODO:remove this, it's only useful for demonstration
	public static String percentToTime(double val) {
		int dayMins = 60 * 24;
		int timeMins = (int) Math.round(val * dayMins);

		int hour = (int) Math.floor(timeMins / 60);
		int min = timeMins % 60;

		//round to 15
		min = min - min % 15;

		String ampm = "am";

		if (hour > 12) {
			hour = hour - 12;
			ampm = "pm";
		}

		if (hour == 0) {
			hour = 12;
		}

		if (val == 1) {
			hour = 11;
			min = 59;
		}

		return "" + hour + ":" + String.format("%02d", min) + ampm + "  (" + val + ")";
	}

}
