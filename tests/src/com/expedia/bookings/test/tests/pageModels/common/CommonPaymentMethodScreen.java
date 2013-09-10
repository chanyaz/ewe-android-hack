package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

import android.widget.TextView;

public class CommonPaymentMethodScreen extends ScreenActions {

	private static final int sAddNewCardTextViewID = R.id.new_payment_new_card;
	
	public CommonPaymentMethodScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}
	
	//Object access
	
	public TextView addNewCardTextView() {
		return (TextView) getView(sAddNewCardTextViewID);
	}
	
	//Object interaction 
	
	public void clickOnAddNewCardTextView() {
		clickOnView(addNewCardTextView());
	}
}
