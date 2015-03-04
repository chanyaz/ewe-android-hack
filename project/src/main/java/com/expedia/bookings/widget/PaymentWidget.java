package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PaymentWidget extends ExpandableCardView {

	public PaymentWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.payment_info_text)
	TextView paymentInfoText;

	@InjectView(R.id.payment_info)
	ViewGroup paymentInfoBlock;

	@InjectView(R.id.edit_credit_card)
	EditText creditCardNumber;

	@InjectView(R.id.edit_cvv)
	EditText creditCardCVV;

	@InjectView(R.id.edit_postal_code)
	EditText creditCardPostalCode;

	@OnClick(R.id.payment_info_card_view)
	public void onCardExpanded() {
		if (paymentInfoBlock.getVisibility() != VISIBLE && mToolbarListener != null) {
			mToolbarListener.onWidgetExpanded(this);
		}
		setExpanded(true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		creditCardPostalCode.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					setExpanded(false);
					mToolbarListener.onWidgetClosed();
				}
				return false;
			}
		});
		creditCardNumber.setOnFocusChangeListener(this);
		creditCardCVV.setOnFocusChangeListener(this);
		creditCardPostalCode.setOnFocusChangeListener(this);
	}

	@Override
	public void setExpanded(boolean expand) {
		super.setExpanded(expand);
		if (expand && mToolbarListener != null) {
			mToolbarListener.setActionBarTitle(getActionBarTitle());
		}
		paymentInfoText.setVisibility(expand ? GONE : VISIBLE);
		paymentInfoBlock.setVisibility(expand ? VISIBLE : GONE);
	}

	@Override
	public boolean getDoneButtonFocus() {
		if (creditCardPostalCode != null) {
			return creditCardPostalCode.hasFocus();
		}
		return false;
	}

	@Override
	public String getActionBarTitle() {
		return getResources().getString(R.string.cars_payment_details_text);
	}

	@Override
	public void onDonePressed() {
		setExpanded(false);
	}

}
