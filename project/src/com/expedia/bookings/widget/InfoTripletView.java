package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

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
	private ViewGroup[] mColumns;
	private View[] mDividers;

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
	}

	public void setValues(CharSequence... values) {
		for (int i = 0; i < values.length && i < mLabels.length; i++) {
			mValues[i].setText(values[i]);
		}
		mNeedsResize = true;
	}

	public void setColumnVisibility(int colNum, int visibility) {
		mColumns[colNum].setVisibility(visibility);
		if (colNum - 1 >= 0) {
			mDividers[colNum - 1].setVisibility(visibility);
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
		mColumns = new ViewGroup[3];
		mDividers = new View[2];
		mValues[0] = Ui.findView(this, R.id.value1);
		mValues[1] = Ui.findView(this, R.id.value2);
		mValues[2] = Ui.findView(this, R.id.value3);
		mLabels[0] = Ui.findView(this, R.id.label1);
		mLabels[1] = Ui.findView(this, R.id.label2);
		mLabels[2] = Ui.findView(this, R.id.label3);
		mColumns[0] = Ui.findView(this, R.id.column1);
		mColumns[1] = Ui.findView(this, R.id.column2);
		mColumns[2] = Ui.findView(this, R.id.column3);
		mDividers[0] = Ui.findView(this, R.id.divider_1_2);
		mDividers[1] = Ui.findView(this, R.id.divider_2_3);

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
		// Figure out if we have to shrink targetTextSizeSp to make room for all the text.
		for (int i = 0; i < views.length; i++) {
			if (mColumns[i].getVisibility() == View.GONE) {
				//If a view is gone we dont need to measure it
				continue;
			}

			TextView view = views[i];
			CharSequence text = view.getText();

			int availWidthPx = view.getWidth() - view.getCompoundPaddingLeft() - view.getCompoundPaddingRight();
			float availWidthDp = availWidthPx / getResources().getDisplayMetrics().density;

			// Do not resize if the view does not have dimensions or there is no text
			if (text == null || text.length() == 0 || availWidthDp <= 0) {
				return;
			}

			targetTextSizeSp = Math.min(targetTextSizeSp, ViewUtils.getTextSizeForMaxLines(getContext(), text,
					view.getPaint(), availWidthDp, 1, targetTextSizeSp, 1));
		}

		// Now set the text size for all the views.
		for (int i = 0; i < views.length; i++) {
			// Some devices try to auto adjust line spacing, so force default line spacing
			// and invalidate the layout as a side effect
			views[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, targetTextSizeSp);
			views[i].setLineSpacing(SPACING_ADD, SPACING_MULT);
		}

		// Reset force resize flag
		mNeedsResize = false;
	}
}
