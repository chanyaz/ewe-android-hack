package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.widget.gl.GLTagProgressBar;
import com.mobiata.android.Log;

public class TagProgressBar extends ViewGroup {
	private Context mContext;

	private RelativeLayout mLayout;
	private GLTagProgressBar mGLTagProgressBar;
	private TextView mTextView;

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
		
		mTextView.layout(0, height - mTextView.getMeasuredHeight(), width, height);
		
		Log.t("h: %d", mTextView.getMeasuredHeight());
	}

	private void init(Context context) {
		mContext = context;

		// Create views
		mLayout = new RelativeLayout(context);
		mGLTagProgressBar = new GLTagProgressBar(context);
		mTextView = new TextView(context);

		// Create layout params
		RelativeLayout.LayoutParams layoutParams;

		// Set params for GLTagProgressBar
		layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mGLTagProgressBar.setLayoutParams(layoutParams);

		// Set params for TextView
		layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		mTextView.setLayoutParams(layoutParams);
		mTextView.setTextColor(0xFF555555);
		mTextView.setGravity(Gravity.CENTER);

		// Add views to layout
		mLayout.addView(mGLTagProgressBar);
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
}
