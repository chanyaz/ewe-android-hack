package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

public class InfoTripletView extends LinearLayout {

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private TextView[] mValues;
	private TextView[] mLabels;

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
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void setLabels(CharSequence... labels) {
		for (int i = 0; i < labels.length && i < mLabels.length; i++) {
			mLabels[i].setText(labels[i]);
		}
	}

	public void setValues(CharSequence... values) {
		for (int i = 0; i < values.length && i < mLabels.length; i++) {
			mValues[i].setText(values[i]);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context) {
		setOrientation(HORIZONTAL);

		inflate(context, R.layout.widget_info_triplet, this);

		mValues = new TextView[3];
		mLabels = new TextView[3];
		mValues[0] = Ui.findView(this, R.id.value1);
		mValues[1] = Ui.findView(this, R.id.value2);
		mValues[2] = Ui.findView(this, R.id.value3);
		mLabels[0] = Ui.findView(this, R.id.label1);
		mLabels[1] = Ui.findView(this, R.id.label2);
		mLabels[2] = Ui.findView(this, R.id.label3);
	}
}
