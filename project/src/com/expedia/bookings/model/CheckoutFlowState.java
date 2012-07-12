package com.expedia.bookings.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightCheckoutActivity;
import com.expedia.bookings.activity.FlightPaymentAddressActivity;
import com.expedia.bookings.activity.FlightPaymentCreditCardActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;

/***
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 * @author jdrotos
 *
 */
public class CheckoutFlowState {
	private static CheckoutFlowState mInstance;

	Context mContext;

	SectionLocation mBillingAddress;
	SectionBillingInfo mCardInfo;
	SectionBillingInfo mCardSecurityCode;

	private CheckoutFlowState(Context context) {
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBillingAddress = (SectionLocation) inflater.inflate(R.layout.section_edit_address, null);
		mCardInfo = (SectionBillingInfo) inflater.inflate(R.layout.section_edit_creditcard, null);
		mCardSecurityCode = (SectionBillingInfo) inflater.inflate(R.layout.section_edit_creditcard_security_code, null);
	}

	public static CheckoutFlowState getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new CheckoutFlowState(context);
		}

		return mInstance;
	}

	private void bind(BillingInfo billingInfo) {
		if (billingInfo.getLocation() == null) {
			billingInfo.setLocation(new Location());
		}
		mBillingAddress.bind(billingInfo.getLocation());
		mCardInfo.bind(billingInfo);
		mCardSecurityCode.bind(billingInfo);
	}

	public boolean hasValidBillingAddress(BillingInfo billingInfo) {
		bind(billingInfo);
		return mBillingAddress.hasValidInput();
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mCardInfo.hasValidInput();
	}

	public boolean hasValidSecurityCode(BillingInfo billingInfo) {
		bind(billingInfo);
		return mCardSecurityCode.hasValidInput();
	}

	public boolean allBillingInfoIsValid(BillingInfo billingInfo) {
		bind(billingInfo);
		return mBillingAddress.hasValidInput()
				&& mCardInfo.hasValidInput() && mCardSecurityCode.hasValidInput();
	}

	public void moveToNextActivityInCheckout(Context context, BillingInfo billingInfo) {
		bind(billingInfo);
		boolean vAddr = mBillingAddress.hasValidInput();
		boolean vInfo = mCardInfo.hasValidInput();

		if (vAddr && vInfo) {
			//Back to checkout summary
			Intent backToCheckoutIntent = new Intent(context,
					FlightCheckoutActivity.class);
			backToCheckoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			backToCheckoutIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			context.startActivity(backToCheckoutIntent);

		}
		else if (vAddr) {
			//Enter card type
			Intent cardTypeIntent = new Intent(context, FlightPaymentCreditCardActivity.class);
			context.startActivity(cardTypeIntent);
		}
		else {
			//Enter billing address
			Intent billingAddressIntent = new Intent(context, FlightPaymentAddressActivity.class);
			context.startActivity(billingAddressIntent);
		}
	}

	public String getFlowButtonText(Context context, BillingInfo billingInfo) {
		bind(billingInfo);
		boolean vAddr = mBillingAddress.hasValidInput();
		boolean vInfo = mCardInfo.hasValidInput();

		if (vAddr && vInfo) {
			return context.getString(R.string.button_done);
		}
		else {
			return context.getString(R.string.next);
		}
	}

}
