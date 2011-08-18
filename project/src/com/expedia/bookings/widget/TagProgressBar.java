package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.widget.gl.GLTagProgressBar;
import com.expedia.bookings.widget.gl.GLTagProgressBarRenderer.OnDrawStartedListener;
import com.mobiata.android.util.AndroidUtils;

public class TagProgressBar extends ViewGroup implements OnDrawStartedListener {
	private final static int TEXTVIEW_PADDING = 16;
	private final static int TEXTVIEW_TEXT_SIZE = 16;

	private Context mContext;

	private RelativeLayout mLayout;
	private GLTagProgressBar mGLTagProgressBar;
	private View mTagHiderView;
	private TextView mTextView;

	private int mOrientation;

	public TagProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = getWidth();
		final int height = getHeight();

		mLayout.layout(0, 0, width, height);
		mGLTagProgressBar.layout(0, 0, width, height);

		if (mOrientation == Surface.ROTATION_0 || mOrientation == Surface.ROTATION_180) {
			mTextView.layout(0, height - mTextView.getHeight(), width, height);
		}
		else {
			mTextView.layout(0, mTextView.getPaddingTop(), width, height);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
					MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void init(Context context) {
		mContext = context;

		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		final float scaledDensity = metrics.scaledDensity;

		mOrientation = ((Activity) context).getWindowManager().getDefaultDisplay().getOrientation();

		// Create views
		mLayout = new RelativeLayout(context);
		mGLTagProgressBar = new GLTagProgressBar(context);
		mTagHiderView = new View(context);
		mTextView = new TextView(context);

		mGLTagProgressBar.addOnDrawStartedListener(this);

		// Create layout params
		RelativeLayout.LayoutParams layoutParams;

		// Set params for GLTagProgressBar
		layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mGLTagProgressBar.setLayoutParams(layoutParams);

		// Set params for tag hider view
		layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mTagHiderView.setLayoutParams(layoutParams);
		mTagHiderView.setBackgroundColor(0xFFE4E4E4);

		// Set params for TextView
		final int tvPadding = (int) (TEXTVIEW_PADDING * scaledDensity);
		layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		mTextView.setLayoutParams(layoutParams);
		mTextView.setTextColor(0xFF555555);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXTVIEW_TEXT_SIZE);
		mTextView.setTypeface(Typeface.DEFAULT_BOLD);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setPadding(tvPadding, tvPadding, tvPadding, tvPadding);
		mTextView.setShadowLayer(0.1f, 0, 1, 0x88FFFFFF);

		// Add views to layout
		mLayout.addView(mGLTagProgressBar);
		mLayout.addView(mTagHiderView);
		mLayout.addView(mTextView);

		// Add layout
		addView(mLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	public boolean getShowProgress() {
		return mGLTagProgressBar.getShowProgress();
	}

	public CharSequence getText() {
		return mTextView.getText();
	}

	public void setShowProgress(boolean showProgress) {
		mGLTagProgressBar.setShowProgress(showProgress);
	}

	public void setText(int resId) {
		setText(mContext.getString(resId));
	}

	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		mTextView.setText(text);
	}

	public void onPause() {
		mGLTagProgressBar.onPause();
	}

	public void onResume() {
		mGLTagProgressBar.onResume();
	}

	@Override
	public void setVisibility(final int visibility) {
		super.setVisibility(visibility);
		if (mGLTagProgressBar != null) {
			if (visibility != View.VISIBLE) {
				mTagHiderView.post(new Runnable() {
					@Override
					public void run() {
						mTagHiderView.setVisibility(View.VISIBLE);
					}
				});
			}

			mGLTagProgressBar.setSensorManagerRegistration(visibility == View.VISIBLE);

			mTagHiderView.post(new Runnable() {
				@Override
				public void run() {
					if (AndroidUtils.isEmulator() && !AndroidUtils.isRelease(mContext)) {
						// If we're running an emulator, we should disable the GL tag
						// so that searches can finish sometime in this lifetime.
						mGLTagProgressBar.setVisibility(View.GONE);
					}
					else {
						mGLTagProgressBar.setVisibility(visibility);
					}
				}
			});
		}
	}

	@Override
	public void onDrawStarted() {
		post(new Runnable() {
			@Override
			public void run() {
				mTagHiderView.setVisibility(View.GONE);
			}
		});
	}
}
