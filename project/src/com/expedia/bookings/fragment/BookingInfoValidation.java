package com.expedia.bookings.fragment;

import android.view.View;

import com.expedia.bookings.R;
import com.mobiata.android.validation.ValidationError;
import com.mobiata.android.validation.ValidationProcessor;

public class BookingInfoValidation {
	
	// Tracking
	private boolean mGuestsCompleted;
	private boolean mBillingCompleted;
	private boolean mCardCompleted;

	public static final int ERROR_INVALID_CARD_NUMBER = 101;
	public static final int ERROR_INVALID_MONTH = 102;
	public static final int ERROR_EXPIRED_YEAR = 103;
	public static final int ERROR_SHORT_SECURITY_CODE = 104;
	public static final int ERROR_INVALID_CARD_TYPE = 105;
	public static final int ERROR_AMEX_BAD_CURRENCY = 106;
	public static final int ERROR_NO_TERMS_CONDITIONS_AGREEMEMT = 107;

	public void checkBookingSectionsCompleted(ValidationProcessor validationProcessor) {
		boolean guestsCompleted = true;
		boolean billingCompleted = true;
		boolean cardCompleted = true;

		for (ValidationError error : validationProcessor.validate()) {
			View view = (View) error.getObject();
			if (view.getId() == R.id.first_name_edit_text || view.getId() == R.id.last_name_edit_text
					|| view.getId() == R.id.telephone_country_code_edit_text
					|| view.getId() == R.id.telephone_edit_text || view.getId() == R.id.email_edit_text) {
				guestsCompleted = false;
			}
			else if (view.getId() == R.id.address1_edit_text || view.getId() == R.id.city_edit_text 
					|| view.getId() == R.id.state_edit_text
					|| view.getId() == R.id.postal_code_edit_text) {
				billingCompleted = false;
			}
			else if (view.getId() == R.id.card_number_edit_text || view.getId() == R.id.expiration_month_edit_text 
					|| view.getId() == R.id.expiration_year_edit_text
					|| view.getId()== R.id.security_code_edit_text) {
				cardCompleted = false;
			}
		}

		mGuestsCompleted = guestsCompleted;
		mBillingCompleted = billingCompleted;
		mCardCompleted = cardCompleted;
	}

	public boolean isGuestsSectionCompleted() {
		return mGuestsCompleted;
	}

	public boolean isBillingSectionCompleted() {
		return mBillingCompleted;
	}

	public boolean isCardSectionCompleted() {
		return mCardCompleted;
	}
	
	public void setGuestsSectionCompleted(boolean isGuestsCompleted) {
		mGuestsCompleted = isGuestsCompleted;
	}
	
	public void setBillingSectionCompleted(boolean isBillingCompleted) {
		mBillingCompleted = isBillingCompleted;
	}
	
	public void setCardSectionCompleted(boolean isCardSectionCompleted) {
		mCardCompleted = isCardSectionCompleted;
	}
	
	public void markAllSectionsAsIncomplete() {
		mGuestsCompleted = mBillingCompleted = mCardCompleted = false;
	}

}
