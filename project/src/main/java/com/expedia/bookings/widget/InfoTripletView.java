package com.expedia.bookings.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.ViewUtils;

public class InfoTripletView extends LinearLayout {

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	// Minimum text size for any of the text
	public static final float MIN_TEXT_SIZE = 1;

	// Text view line spacing multiplier
	private static final float SPACING_MULT = 1.0f;

	// Text view additional line spacing
	private static final float SPACING_ADD = 0.0f;

	// Flag for text and/or size changes to force a resize
	private boolean mNeedsResize = false;

	// Desired font size. Try this first and if it doesn't fit, shrink it.
	private float mDesiredLabelTextSizeSp;
	private float mDesiredValueTextSizeSp;

	private TextView[] mValues;
	private TextView[] mLabels;
	private View[] mDividers;

	private int mInfoCountToDisplay;
	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public InfoTripletView(Context context) {
		super(context);
		init(context);
	}

	public InfoTripletView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public InfoTripletView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Overrides
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * If the text view size changed, set the force resize flag to true
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw || h != oldh) {
			mNeedsResize = true;
		}
	}

	/**
	 * Resize text after measuring
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// Hide guest count for activities if api returns it as 0.
		if (mInfoCountToDisplay == 2) {
			mLabels[2].setVisibility(View.GONE);
			mValues[2].setVisibility(View.GONE);
			mDividers[1].setVisibility(View.GONE);
		}

		if (changed || mNeedsResize) {
			resizeValues();
			resizeLabels();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void setLabels(CharSequence... labels) {
		for (int i = 0; i < labels.length && i < mLabels.length; i++) {
			mLabels[i].setText(labels[i]);
		}
		mNeedsResize = true;
		mInfoCountToDisplay = labels.length;
	}

	public void setValues(CharSequence... values) {
		for (int i = 0; i < values.length && i < mLabels.length; i++) {
			mValues[i].setText(values[i]);
		}
		mNeedsResize = true;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context) {
		setOrientation(HORIZONTAL);

		inflate(context, R.layout.widget_info_triplet, this);

		mValues = new TextView[3];
		mLabels = new TextView[3];
		mDividers = new View[2];
		mValues[0] = Ui.findView(this, R.id.value1);
		mValues[1] = Ui.findView(this, R.id.value2);
		mValues[2] = Ui.findView(this, R.id.value3);
		mLabels[0] = Ui.findView(this, R.id.label1);
		mLabels[1] = Ui.findView(this, R.id.label2);
		mLabels[2] = Ui.findView(this, R.id.label3);
		mDividers[0] = Ui.findView(this, R.id.divider1);
		mDividers[1] = Ui.findView(this, R.id.divider2);

		mDesiredValueTextSizeSp = mValues[0].getTextSize() / getResources().getDisplayMetrics().scaledDensity;
		mDesiredLabelTextSizeSp = mLabels[0].getTextSize() / getResources().getDisplayMetrics().scaledDensity;
	}

	private void resizeValues() {
		resizeAllTheThings(mValues, mDesiredValueTextSizeSp);
	}

	private void resizeLabels() {
		resizeAllTheThings(mLabels, mDesiredLabelTextSizeSp);
	}

	private void resizeAllTheThings(TextView[] views, float targetTextSizeSp) {
		float combinedTextSizeSp = targetTextSizeSp;

		// Figure out if we have to shrink targetTextSizeSp to make room for all the text.
		for (int i = 0; i < mInfoCountToDisplay; i++) {
			TextView view = views[i];
			CharSequence text = view.getText();

			// Do not resize if there is no text
			if (TextUtils.isEmpty(text)) {
				return;
			}

			int availWidthPx = view.getWidth() - view.getCompoundPaddingLeft() - view.getCompoundPaddingRight();
			float availWidthDp = availWidthPx / getResources().getDisplayMetrics().density;

			// Do not resize if the view does not have dimensions
			if (availWidthDp <= 0) {
				return;
			}

			float columnTextSizeSp = ViewUtils.getTextSizeForMaxLines(getContext(), text,
					view.getPaint(), availWidthDp, 1, targetTextSizeSp, 1);

			combinedTextSizeSp = Math.min(combinedTextSizeSp, columnTextSizeSp);
		}

		// Now set the text size for all the views.
		for (int i = 0; i < mInfoCountToDisplay; i++) {
			// Some devices try to auto adjust line spacing, so force default line spacing
			// and invalidate the layout as a side effect
			views[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, combinedTextSizeSp);
			views[i].setLineSpacing(SPACING_ADD, SPACING_MULT);
		}

		// Reset force resize flag
		mNeedsResize = false;
	}
}
