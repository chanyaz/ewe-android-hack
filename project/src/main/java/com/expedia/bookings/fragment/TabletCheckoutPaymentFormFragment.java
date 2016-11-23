package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CreditCardUtils;
import com.mobiata.android.util.Ui;
import com.squareup.phrase.Phrase;

public class TabletCheckoutPaymentFormFragment extends TabletCheckoutDataFormFragment {

	public static TabletCheckoutPaymentFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutPaymentFormFragment frag = new TabletCheckoutPaymentFormFragment();
		frag.setLob(lob);
		return frag;
	}

	private static final String STATE_FORM_IS_OPEN = "STATE_FORM_IS_OPEN";

	private boolean mAttemptToLeaveMade = false;
	private SectionBillingInfo mSectionBillingInfo;
	private SectionLocation mSectionLocation;
	private ICheckoutDataListener mListener;
	private boolean mFormOpen = false;

	private TextView mCreditCardMessageTv;

	//Animation vars for the card message
	private ObjectAnimator mLastCardMessageAnimator;
	private boolean mCardMessageShowing = false;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mAttemptToLeaveMade = false;
		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mFormOpen = savedInstanceState.getBoolean(STATE_FORM_IS_OPEN, false);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		bindToDb();
		if (mFormOpen) {
			onFormOpened();
		}
		else {
			onFormClosed();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_FORM_IS_OPEN, mFormOpen);
	}


	public void bindToDb() {
		if (mSectionBillingInfo != null) {
			BillingInfo workingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(workingInfo);
			mSectionBillingInfo.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
		}
		setHeadingText(getString(R.string.payment_method));
		setHeadingButtonText(getString(R.string.done));
		setHeadingButtonOnClick(mTopRightClickListener);

		if (mSectionLocation != null) {
			mSectionLocation.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getLocation());
		}
	}

	private OnClickListener mTopRightClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mAttemptToLeaveMade = true;

			if (Db.getBillingInfo().hasStoredCard()) {
				//If we have a saved card we're good to go
				commitAndLeave();
			}
			else {
				//If we don't have a saved card, we must validate, if we have valid input, close
				boolean requiresAddress = PointOfSale.getPointOfSale().requiresBillingAddressFlights();
				boolean hasValidBillingInfo = mSectionBillingInfo != null && mSectionBillingInfo.performValidation();
				boolean hasValidLocation =
					!requiresAddress || mSectionLocation != null && mSectionLocation.performValidation();

				if (hasValidBillingInfo && hasValidLocation) {
					commitAndLeave();
				}
			}
		}
	};

	private void resetValidation() {
		if (mSectionBillingInfo != null) {
			mSectionBillingInfo.resetValidation();
		}
		if (mSectionLocation != null) {
			mSectionLocation.resetValidation();
		}
	}

	private void commitAndLeave() {
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		resetValidation();
		mListener.onCheckoutDataUpdated();
		Ui.hideKeyboard(getActivity(), InputMethodManager.HIDE_NOT_ALWAYS);
		closeForm(true);
	}

	@Override
	public void setUpFormContent(ViewGroup formContainer) {
		//This will probably end up having way more moving parts than this...
		formContainer.removeAllViews();

		//Add a focus stealer
		View view = new View(getActivity());
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		formContainer.addView(view, new ViewGroup.LayoutParams(0, 0));

		// Actual form data
		if (getLob() == LineOfBusiness.HOTELS) {
			mSectionBillingInfo = Ui.inflate(this, R.layout.section_hotel_edit_creditcard, null);
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mSectionBillingInfo = Ui.inflate(this, R.layout.section_flight_edit_creditcard, null);
		}

		mCreditCardMessageTv = getFormEntryMessageTextView();

		mSectionBillingInfo.setLineOfBusiness(getLob());
		mSectionBillingInfo.addChangeListener(new ISectionEditable.SectionChangeListener() {
			@Override
			public void onChange() {
				if (getActivity() == null) {
					return;
				}

				if (mAttemptToLeaveMade) {
					mSectionBillingInfo.performValidation();
				}

				// Let's show airline fees (LCC Fees) or messages if any
				if (getLob() == LineOfBusiness.FLIGHTS) {
					BillingInfo mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
					if (mBillingInfo.getPaymentType() != null) {
						TripBucketItem item = Db.getTripBucket().getFlight();
						if (!item.isPaymentTypeSupported(mBillingInfo.getPaymentType())) {
							String cardName = CreditCardUtils
								.getHumanReadableName(getActivity(), mBillingInfo.getPaymentType());
							String message = getString(R.string.airline_does_not_accept_cardtype_TEMPLATE, cardName);
							updateCardMessageText(message);
							toggleCardMessage(true, true);
						}
						else if (item.getPaymentFee(mBillingInfo) != null) {
							String message = Phrase.from(getContext(), R.string.airline_processing_fee_TEMPLATE)
								.put("card_fee", item.getPaymentFee(mBillingInfo).getFormattedMoney())
								.format().toString();
							updateCardMessageText(message);
							toggleCardMessage(true, true);
						}
						else {
							hideCardMessageOrDisplayDefault(getLob(), true);
						}
					}
					else {
						hideCardMessageOrDisplayDefault(getLob(), true);
					}
				}

				if (getLob() == LineOfBusiness.HOTELS) {
					BillingInfo mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
					if (mBillingInfo.getPaymentType() != null) {
						TripBucketItem item = Db.getTripBucket().getHotel();
						if (!item.isPaymentTypeSupported(mBillingInfo.getPaymentType())) {
							String cardName = CreditCardUtils
								.getHumanReadableName(getActivity(), mBillingInfo.getPaymentType());
							String message = getString(R.string.hotel_does_not_accept_cardtype_TEMPLATE, cardName);
							updateCardMessageText(message);
							toggleCardMessage(true, true);
						}
						else {
							hideCardMessageOrDisplayDefault(getLob(), true);
						}
					}
					else {
						hideCardMessageOrDisplayDefault(getLob(), true);
					}
				}
			}
		});

		formContainer.addView(mSectionBillingInfo);

		mSectionLocation = Ui.findView(mSectionBillingInfo, R.id.section_location_address);
		mSectionLocation.setLineOfBusiness(getLob());
		mSectionLocation.addChangeListener(new ISectionEditable.SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					mSectionLocation.performValidation();
				}
			}
		});
	}

	@Override
	public void onFormClosed() {
		if (isResumed() && mFormOpen) {
			mAttemptToLeaveMade = false;
			resetValidation();
			mListener.onCheckoutDataUpdated();
		}
		mFormOpen = false;
	}

	@Override
	public void onFormOpened() {
		if (Db.getBillingInfo().hasStoredCard()) {
			showStoredCardContainer();

			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(Db.getBillingInfo());
		}
		else {
			showNewCardContainer();
		}
		mFormOpen = true;
	}

	@Override
	public boolean showBoardingMessage() {
		return false;
	}

	public void updateCardMessageText(String message) {
		if (message != null) {
			mCreditCardMessageTv.setText(HtmlCompat.fromHtml(message));
		}
	}

	/**
	 * Hide the card message OR display a default message.
	 * Some POSes have messages like "Dont use debit cards" that need to display all the time.
	 */
	public void hideCardMessageOrDisplayDefault(LineOfBusiness lob, boolean animate) {
		if (lob == LineOfBusiness.FLIGHTS && PointOfSale.getPointOfSale().doesNotAcceptDebitCardsForFlights()) {
			Resources res = getResources();
			updateCardMessageText(res.getString(R.string.debit_cards_not_accepted));
			toggleCardMessage(true, animate);
		}
		else {
			toggleCardMessage(false, animate);
		}
	}

	/**
	 * Toggle the message that displays above the virtual keyboard.
	 */
	public void toggleCardMessage(final boolean show, final boolean animate) {
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
				mCreditCardMessageTv.getViewTreeObserver()
					.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
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

	//////////////////////////////////////////////////////////////////////////
	// Stored cards

	private void showStoredCardContainer() {
		StoredCreditCard card = Db.getBillingInfo().getStoredCard();
		String cardName = card.getDescription();
		PaymentType cardType = card.getType();
		showStoredCardContainer(cardName, cardType);
	}

	private void showStoredCardContainer(String cardName, PaymentType cardType) {
		Ui.findView(getParentFragment().getActivity(), R.id.new_card_container).setVisibility(View.GONE);
		View storedCardContainer = Ui.findView(getParentFragment().getActivity(), R.id.stored_card_container);
		storedCardContainer.setVisibility(View.VISIBLE);

		TextView cardNameView = Ui.findView(storedCardContainer, R.id.stored_card_name);
		cardNameView.setText(cardName);

		ImageView cardTypeIcon = Ui.findView(mSectionBillingInfo, R.id.display_credit_card_brand_icon_tablet);
		if (cardType != null) {
			cardTypeIcon.setImageResource(BookingInfoUtils.getTabletCardIcon(cardType));
		}
		else {
			cardTypeIcon.setImageResource(R.drawable.ic_tablet_checkout_generic_credit_card);
		}

		Ui.findView(storedCardContainer, R.id.remove_stored_card_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// Let's reset the selectable/clickable state (in the stored card picker, checkout overview screen) of the currentCC
				StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
				if (currentCC != null) {
					BookingInfoUtils.resetPreviousCreditCardSelectState(getParentFragment().getActivity(), currentCC);
				}
				Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
				Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setLocation(new Location());

				//This is a special case, and it maybe deserves some more consideration. It is here to improve the following
				//use case: A user has a stored card selected, they open the payment form, they hit the X button and remove the
				//stored credit card, they start to enter in a CC number manually. At this point what should happen if
				//they hit the back button? Typically we don't commit the changes until the user hits the Done button
				//, however, it feels wrong to move back to the overview screen with the saved card back in the picture
				//after they had removed it completely and started entering a card # manually. So we clear the Db version.
				Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();

				showNewCardContainer();
			}
		});
	}

	private void showNewCardContainer() {
		Ui.findView(getParentFragment().getActivity(), R.id.stored_card_container).setVisibility(View.GONE);
		Ui.findView(getParentFragment().getActivity(), R.id.new_card_container).setVisibility(View.VISIBLE);
		mSectionBillingInfo.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
	}
}
