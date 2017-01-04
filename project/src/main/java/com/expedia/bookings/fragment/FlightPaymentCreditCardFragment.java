package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CreditCardUtils;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

public class FlightPaymentCreditCardFragment extends Fragment implements Validatable {

	private static final String STATE_TAG_ATTEMPTED_LEAVE = "STATE_TAG_ATTEMPTED_LEAVE";

	private BillingInfo mBillingInfo;
	boolean mAttemptToLeaveMade = false;

	private SectionBillingInfo mSectionCreditCard;
	private TextView mCreditCardMessageTv;

	//Animation vars for the card message
	private ObjectAnimator mLastCardMessageAnimator;
	private boolean mCardMessageShowing = false;

	public static FlightPaymentCreditCardFragment newInstance() {
		return new FlightPaymentCreditCardFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mAttemptToLeaveMade = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_payment_creditcard, container, false);

		mCreditCardMessageTv = Ui.findView(v, R.id.card_message);
		hideCardMessageOrDisplayDefault(true);

		mAttemptToLeaveMade = savedInstanceState != null ? savedInstanceState.getBoolean(STATE_TAG_ATTEMPTED_LEAVE,
			false) : false;

		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		mSectionCreditCard = Ui.findView(v, R.id.creditcard_section);
		mSectionCreditCard.setLineOfBusiness(LineOfBusiness.FLIGHTS);
		mSectionCreditCard.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionCreditCard.performValidation();
				}
			}
		});

		//This change listener detects invalid card brands and or card fees and displays a helpful message
		mSectionCreditCard.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mBillingInfo.getPaymentType() != null) {
					TripBucketItemFlight flightItem = Db.getTripBucket().getFlight();
					Money paymentFee = flightItem.getPaymentFee(mBillingInfo);
					if (!flightItem.isPaymentTypeSupported(mBillingInfo.getPaymentType())) {
						String cardName = CreditCardUtils
							.getHumanReadableName(getActivity(), mBillingInfo.getPaymentType());
						String message = Phrase.from(getContext(), R.string.airline_does_not_accept_cardtype_TEMPLATE)
							.put("card_type", cardName)
							.format().toString();
						updateCardMessage(message, getResources().getColor(R.color.flight_card_unsupported_warning));
						toggleCardMessage(true, true);
					}
					else if (paymentFee != null && !paymentFee.isZero()) {
						String message = Phrase.from(getContext(), R.string.airline_processing_fee_TEMPLATE)
							.put("card_fee", paymentFee.getFormattedMoney())
							.format().toString();
						updateCardMessage(message,
							ContextCompat.getColor(getContext(), R.color.flight_card_airline_fee_warning));
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
		});

		mSectionCreditCard.addInvalidCharacterListener(new InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
			}
		});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightCheckoutPaymentEditCard();
	}

	@Override
	public void onResume() {
		super.onResume();
		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
		bindAll();

		View focused = this.getView().findFocus();
		if (focused == null || !(focused instanceof EditText)) {
			focused = Ui.findView(mSectionCreditCard, R.id.edit_creditcard_number);
		}
		if (focused != null && focused instanceof EditText) {
			FocusViewRunnable.focusView(this, focused);
		}

		if (User.isLoggedIn(getActivity())) {
			EditText cardHolderName = Ui.findView(mSectionCreditCard, R.id.edit_name_on_card);
			if (TextUtils.isEmpty(cardHolderName.getText())) {
				cardHolderName.setText(Db.getUser().getPrimaryTraveler().getFullName());
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_TAG_ATTEMPTED_LEAVE, mAttemptToLeaveMade);
	}

	@Override
	public boolean attemptToLeave() {
		mAttemptToLeaveMade = true;
		return mSectionCreditCard != null ? mSectionCreditCard.performValidation() : false;
	}

	public void bindAll() {
		mSectionCreditCard.bind(mBillingInfo);
	}

	/**
	 * Set the message that displays above the virtual keyboard.
	 */
	private void updateCardMessage(String message, int backgroundColor) {
		mCreditCardMessageTv.setBackgroundColor(backgroundColor);
		mCreditCardMessageTv.setText(HtmlCompat.fromHtml(message));
	}

	/**
	 * Toggle the message that displays above the virtual keyboard.
	 */
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

	/**
	 * Hide the card message OR display a default message.
	 * Some POSes have messages like "Dont use debit cards" that need to display all the time.
	 */
	private void hideCardMessageOrDisplayDefault(boolean animate) {
		if (PointOfSale.getPointOfSale().doesNotAcceptDebitCardsForFlights()) {
			Resources res = FlightPaymentCreditCardFragment.this.getResources();
			updateCardMessage(res.getString(R.string.debit_cards_not_accepted),
				res.getColor(R.color.flight_card_no_debit_warning));
			toggleCardMessage(true, animate);
		}
		else {
			toggleCardMessage(false, animate);
		}
	}
}
