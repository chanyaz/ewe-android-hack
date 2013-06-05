package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.Validatable;
import com.expedia.bookings.animation.AnimatorListenerShort;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

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
		FlightPaymentCreditCardFragment fragment = new FlightPaymentCreditCardFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightCheckoutPaymentEditCard(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_payment_creditcard, container, false);

		mCreditCardMessageTv = Ui.findView(v, R.id.card_message);

		mAttemptToLeaveMade = savedInstanceState != null ? savedInstanceState.getBoolean(STATE_TAG_ATTEMPTED_LEAVE,
				false) : false;

		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		if (User.isLoggedIn(getActivity())) {
			mBillingInfo.setEmail(Db.getUser().getPrimaryTraveler().getEmail());
		}

		mSectionCreditCard = Ui.findView(v, R.id.creditcard_section);
		mSectionCreditCard.setLineOfBusiness(LineOfBusiness.FLIGHTS);
		mSectionCreditCard.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionCreditCard.hasValidInput();
				}
				//Attempt to save on change
				Db.getWorkingBillingInfoManager().attemptWorkingBillingInfoSave(getActivity(), false);
			}
		});

		//This change listener detects invalid card brands and or card fees and displays a helpful message
		mSectionCreditCard.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mBillingInfo.getCardType() != null) {
					FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
					if (!trip.isCardTypeSupported(mBillingInfo.getCardType())) {
						String message = getString(R.string.airline_does_not_accept_cardtype_TEMPLATE,
								CurrencyUtils.getHumanReadableCardTypeName(getActivity(), mBillingInfo.getCardType()));
						updateCardMessage(
								message,
								FlightPaymentCreditCardFragment.this.getResources().getColor(
										R.color.flight_card_unsupported_warning));
						toggleCardMessage(true, true);
					}
					else if (trip.getCardFee(mBillingInfo) != null) {
						String message = getString(R.string.airline_processing_fee_TEMPLATE,
								trip.getCardFee(mBillingInfo).getFormattedMoney());
						updateCardMessage(
								message,
								FlightPaymentCreditCardFragment.this.getResources().getColor(
										R.color.flight_card_airline_fee_warning));
						toggleCardMessage(true, true);
					}
					else {
						toggleCardMessage(false, true);
					}
				}
				else {
					toggleCardMessage(false, true);
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
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
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_TAG_ATTEMPTED_LEAVE, mAttemptToLeaveMade);
	}

	@Override
	public boolean attemptToLeave() {
		mAttemptToLeaveMade = true;
		return mSectionCreditCard != null ? mSectionCreditCard.hasValidInput() : false;
	}

	public void bindAll() {
		mSectionCreditCard.bind(mBillingInfo);
	}

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
						animator.addListener(new AnimatorListenerShort() {

							@Override
							public void onAnimationStart(Animator arg0) {
								mCreditCardMessageTv.setVisibility(View.VISIBLE);
							}

						});
					}
					else {
						animator.addListener(new AnimatorListenerShort() {

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
}
