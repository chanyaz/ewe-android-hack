package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CreditCardUtils;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.Ui;

public class HotelPaymentCreditCardFragment extends Fragment implements Validatable {

	private static final String STATE_TAG_ATTEMPTED_LEAVE = "STATE_TAG_ATTEMPTED_LEAVE";

	private BillingInfo mBillingInfo;

	private SectionBillingInfo mSectionBillingInfo;
	private SectionLocation mSectionLocation;

	private boolean mAttemptToLeaveMade = false;

	private TextView mCreditCardMessageTv;

	//Animation vars for the card message
	private ObjectAnimator mLastCardMessageAnimator;
	private boolean mCardMessageShowing = false;

	public static HotelPaymentCreditCardFragment newInstance() {
		return new HotelPaymentCreditCardFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_hotel_payment_creditcard, container, false);

		mCreditCardMessageTv = Ui.findView(v, R.id.card_message);
		hideCardMessageOrDisplayDefault(true);

		mAttemptToLeaveMade = savedInstanceState != null ? savedInstanceState.getBoolean(STATE_TAG_ATTEMPTED_LEAVE,
				false) : false;

		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		InvalidCharacterListener invalidCharacterListener = new InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
			}
		};

		mSectionBillingInfo = Ui.findView(v, R.id.creditcard_section);
		mSectionBillingInfo.setLineOfBusiness(LineOfBusiness.HOTELS);
		mSectionLocation = Ui.findView(v, R.id.section_location_address);
		mSectionLocation.setLineOfBusiness(LineOfBusiness.HOTELS);

		mSectionBillingInfo.addChangeListener(mSectionListener);
		mSectionBillingInfo.addChangeListener(mValidFormsOfPaymentListener);
		mSectionLocation.addChangeListener(mSectionListener);

		mSectionBillingInfo.addInvalidCharacterListener(invalidCharacterListener);
		mSectionLocation.addInvalidCharacterListener(invalidCharacterListener);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsCheckoutPaymentEditCard();
	}

	@Override
	public void onResume() {
		super.onResume();
		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
		bindAll();

		View focused = this.getView().findFocus();
		if (focused == null || !(focused instanceof EditText)) {
			focused = Ui.findView(mSectionBillingInfo, R.id.edit_creditcard_number);
		}
		if (focused != null && focused instanceof EditText) {
			FocusViewRunnable.focusView(this, focused);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_TAG_ATTEMPTED_LEAVE, mAttemptToLeaveMade);
	}

	/**
	 * Performs validation on the form. We can possibly have both SectionBillingInfo and SectionLocation, so we must
	 * account for the different combinations. SectionLocation is null when it is not required based on the POS, which
	 * ultimately means that the location validation is successful (as it does not exist, heh).
	 */
	@Override
	public boolean attemptToLeave() {
		mAttemptToLeaveMade = true;
		boolean hasValidCreditCard = mSectionBillingInfo != null ? mSectionBillingInfo.performValidation() : false;
		boolean hasValidPaymentLocation = mSectionLocation != null ? mSectionLocation.performValidation() : true;
		return hasValidCreditCard && hasValidPaymentLocation;
	}

	public void bindAll() {
		mSectionBillingInfo.bind(mBillingInfo);
	}

	final SectionChangeListener mSectionListener = new SectionChangeListener() {
		@Override
		public void onChange() {
			if (mAttemptToLeaveMade) {
				//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
				if (mSectionBillingInfo != null) {
					mSectionBillingInfo.performValidation();
				}
				if (mSectionLocation != null) {
					mSectionLocation.performValidation();
				}
			}
		}
	};

	final SectionChangeListener mValidFormsOfPaymentListener = new SectionChangeListener() {
		@Override
		public void onChange() {
			if (mBillingInfo.getCardType() != null) {
				if (!Db.getTripBucket().getHotel().isCardTypeSupported(mBillingInfo.getCardType())) {
					String cardName = CreditCardUtils.getHumanReadableName(getActivity(), mBillingInfo.getCardType());
					String message = getString(R.string.hotel_does_not_accept_cardtype_TEMPLATE, cardName);
					updateCardMessage(message, getResources().getColor(R.color.flight_card_unsupported_warning));
					toggleCardMessage(true, true);
				}
				else {
					hideCardMessageOrDisplayDefault(true);
				}
			}
			else {
				hideCardMessageOrDisplayDefault(true);
			}
		}
	};

	private void updateCardMessage(String message, int backgroundColor) {
		mCreditCardMessageTv.setBackgroundColor(backgroundColor);
		mCreditCardMessageTv.setText(Html.fromHtml(message));
	}

	private void toggleCardMessage(final boolean show, final boolean animate) {
		if (!animate) {
			if (mLastCardMessageAnimator != null && mLastCardMessageAnimator.isRunning()) {
				mLastCardMessageAnimator.end();
			}
			mCreditCardMessageTv.setVisibility(show ? View.VISIBLE : View.GONE);
			mCardMessageShowing = show;
		}
		else {
			int totalHeight = mCreditCardMessageTv.getHeight();
			if (show && !mCardMessageShowing && totalHeight <= 0) {
				mCreditCardMessageTv.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						mCreditCardMessageTv.getViewTreeObserver().removeOnPreDrawListener(this);
						toggleCardMessage(show, animate);
						return true;
					}
				});
				mCreditCardMessageTv.setVisibility(View.VISIBLE);
			}
			else {
				if (show != mCardMessageShowing) {
					if (mLastCardMessageAnimator != null && mLastCardMessageAnimator.isRunning()) {
						mLastCardMessageAnimator.cancel();
					}
					float start = show ? mCreditCardMessageTv.getHeight() : 0f;
					float end = show ? 0f : mCreditCardMessageTv.getHeight();

					ObjectAnimator animator = ObjectAnimator.ofFloat(mCreditCardMessageTv, "translationY",
							start, end);
					animator.setDuration(300);
					if (show) {
						animator.addListener(new AnimatorListenerAdapter() {

							@Override
							public void onAnimationStart(Animator arg0) {
								mCreditCardMessageTv.setVisibility(View.VISIBLE);
							}

						});
					}
					else {
						animator.addListener(new AnimatorListenerAdapter() {

							@Override
							public void onAnimationEnd(Animator arg0) {
								mCreditCardMessageTv.setVisibility(View.GONE);
							}

						});
					}
					mLastCardMessageAnimator = animator;
					animator.start();
					mCardMessageShowing = show;
				}
			}
		}
	}

	private void hideCardMessageOrDisplayDefault(boolean animate) {
		toggleCardMessage(false, animate);
	}
}
