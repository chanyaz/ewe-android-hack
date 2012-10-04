package com.expedia.bookings.section;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.section.CreditCardInputSection.CreditCardInputListener;
import com.expedia.bookings.utils.Ui;

public class CVVSection extends RelativeLayout implements CreditCardInputListener {

	private static final int SHADE_COLOR = 0x66000000;

	private Paint mPlainPaint;
	private Paint mShadePaint;

	private TextView mCvvTextView;
	private TextView mCvvExplanationTextView;

	public CVVSection(Context context, AttributeSet attrs) {
		super(context, attrs);

		setWillNotDraw(false);

		mPlainPaint = new Paint();

		mShadePaint = new Paint();
		mShadePaint.setColor(SHADE_COLOR);
		mShadePaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mCvvTextView = Ui.findView(this, R.id.cvv_text_view);
		mCvvExplanationTextView = Ui.findView(this, R.id.cvv_explanation_text_view);

		mCvvTextView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/OCRAStd.otf"));
	}

	public void setExplanationText(CharSequence text) {
		mCvvExplanationTextView.setText(text);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw the shaded portion everywhere except the 
		Rect bounds = canvas.getClipBounds();

		canvas.saveLayer(bounds.left, bounds.top, bounds.right, bounds.bottom, null, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);

		// Draw knocked-out portion
		canvas.drawRect(mCvvTextView.getLeft(), mCvvTextView.getTop(), mCvvTextView.getRight(),
				mCvvTextView.getBottom(), mPlainPaint);

		// Fill rest of rectangle
		canvas.drawRect(bounds, mShadePaint);

		canvas.restore();
	}

	public String getCvv() {
		return mCvvTextView.getText().toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// CreditCardInputListener

	@Override
	public void onKeyPress(int code) {
		String currText = mCvvTextView.getText().toString();
		int len = currText.length();
		if (code == CreditCardInputSection.CODE_DELETE) {
			if (len != 0) {
				currText = currText.substring(0, len - 1);
			}
		}
		else {
			if (len != 4) {
				currText += code;
			}
		}
		mCvvTextView.setText(currText);
	}
}
