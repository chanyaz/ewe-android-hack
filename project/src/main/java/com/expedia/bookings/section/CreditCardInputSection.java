package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

public class CreditCardInputSection extends TableLayout implements View.OnClickListener {

	public static final int CODE_DELETE = -1;
	public static final int CODE_BOOK = -2;

	private View mZeroView;
	private View mOneView;
	private View mTwoView;
	private View mThreeView;
	private View mFourView;
	private View mFiveView;
	private View mSixView;
	private View mSevenView;
	private View mEightView;
	private View mNineView;
	private View mDeleteView;
	private View mBookView;

	private CreditCardInputListener mListener;

	public CreditCardInputSection(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setStretchAllColumns(true);
		LayoutInflater.from(context).inflate(R.layout.section_credit_card_input, this, true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// Cache the views
		mZeroView = Ui.findView(this, R.id.zero_button);
		mOneView = Ui.findView(this, R.id.one_button);
		mTwoView = Ui.findView(this, R.id.two_button);
		mThreeView = Ui.findView(this, R.id.three_button);
		mFourView = Ui.findView(this, R.id.four_button);
		mFiveView = Ui.findView(this, R.id.five_button);
		mSixView = Ui.findView(this, R.id.six_button);
		mSevenView = Ui.findView(this, R.id.seven_button);
		mEightView = Ui.findView(this, R.id.eight_button);
		mNineView = Ui.findView(this, R.id.nine_button);
		mDeleteView = Ui.findView(this, R.id.delete_button);
		mBookView = Ui.findView(this, R.id.book_button);

		setMeAsClickListener(mZeroView, mOneView, mTwoView, mThreeView, mFourView, mFiveView,
			mSixView, mSevenView, mEightView, mNineView, mDeleteView, mBookView);
	}

	private void setMeAsClickListener(View... children) {
		for (View child : children) {
			if (child != null) {
				child.setOnClickListener(this);
			}
		}
	}

	public void setListener(CreditCardInputListener listener) {
		mListener = listener;
	}

	public void setBookButtonEnabled(boolean enabled) {
		if (mBookView != null) {
			mBookView.setEnabled(enabled);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// View.OnClickListener
	//
	// Instead of generating a ton of listeners, just use this as the listener
	// and interpret the keypress based on the view's id

	@Override
	public void onClick(View v) {
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		int code;
		switch (v.getId()) {
		/*case R.id.zero_button:
			code = 0;
			break;
		case R.id.one_button:
			code = 1;
			break;
		case R.id.two_button:
			code = 2;
			break;
		case R.id.three_button:
			code = 3;
			break;
		case R.id.four_button:
			code = 4;
			break;
		case R.id.five_button:
			code = 5;
			break;
		case R.id.six_button:
			code = 6;
			break;
		case R.id.seven_button:
			code = 7;
			break;
		case R.id.eight_button:
			code = 8;
			break;
		case R.id.nine_button:
			code = 9;
			break;
		case R.id.delete_button:
			code = CODE_DELETE;
			break;
		case R.id.book_button:
			code = CODE_BOOK;
			break;*/
		default:
			throw new RuntimeException("How did you even get here?");
		}

		/*if (mListener != null) {
			mListener.onKeyPress(code);
		}*/
	}

	//////////////////////////////////////////////////////////////////////////
	// CreditCardInputListener

	public interface CreditCardInputListener {
		void onKeyPress(int code);
	}
}
