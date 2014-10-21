package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.section.StoredCreditCardSpinnerAdapter;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutPaymentFormFragment extends TabletCheckoutDataFormFragment {

	public static TabletCheckoutPaymentFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutPaymentFormFragment frag = new TabletCheckoutPaymentFormFragment();
		frag.setLob(lob);
		return frag;
	}

	private static final String STATE_FORM_IS_OPEN = "STATE_FORM_IS_OPEN";

	private StoredCreditCardSpinnerAdapter mStoredCreditCardAdapter;
	private boolean mAttemptToLeaveMade = false;
	private SectionBillingInfo mSectionBillingInfo;
	private SectionLocation mSectionLocation;
	private ICheckoutDataListener mListener;
	private boolean mFormOpen = false;

	private ViewGroup mMeasureParent;

	private TextView mCreditCardMessageTv;

	//Animation vars for the card message
	private ObjectAnimator mLastCardMessageAnimator;
	private boolean mCardMessageShowing = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (Db.getWorkingBillingInfoManager().getAttemptToLoadFromDisk() && Db.getWorkingBillingInfoManager()
				.hasBillingInfoOnDisk(getActivity())) {
				Db.getWorkingBillingInfoManager().loadWorkingBillingInfoFromDisk(getActivity());
			}
			mFormOpen = savedInstanceState.getBoolean(STATE_FORM_IS_OPEN, false);
		}

		TripBucketItem item = null;
		if (getLob() == LineOfBusiness.FLIGHTS) {
			item = Db.getTripBucket().getFlight();
		}
		if (getLob() == LineOfBusiness.HOTELS) {
			item = Db.getTripBucket().getHotel();
		}
		mStoredCreditCardAdapter = new StoredCreditCardSpinnerAdapter(getActivity(), item);

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

			if (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard()) {
				//If we have a saved card we're good to go
				commitAndLeave();
			}
			else  {
				//If we don't have a saved card, we must validate, if we have valid input, close
				boolean requiresAddress = PointOfSale.getPointOfSale().requiresBillingAddressFlights();
				boolean hasValidBillingInfo = mSectionBillingInfo != null && mSectionBillingInfo.performValidation();
				boolean hasValidLocation = !requiresAddress || mSectionLocation != null && mSectionLocation.performValidation();

				if (hasValidBillingInfo && hasValidLocation) {
					commitAndLeave();
				}
			}
		}
	};

	private void commitAndLeave() {
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
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

				//We attempt to save on change
				Db.getWorkingBillingInfoManager().attemptWorkingBillingInfoSave(getActivity(), false);

				// Let's show airline fees (LCC Fees) or messages if any
				if (getLob() == LineOfBusiness.FLIGHTS) {
					BillingInfo mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
					if (mBillingInfo.getCardType() != null) {
						TripBucketItem item = Db.getTripBucket().getFlight();
						if (!item.isCardTypeSupported(mBillingInfo.getCardType())) {
							String message = getString(R.string.airline_does_not_accept_cardtype_TEMPLATE, mBillingInfo
								.getCardType().getHumanReadableName(getActivity()));
							updateCardMessageText(message);
							toggleCardMessage(true, true);
						}
						else if (item.getCardFee(mBillingInfo) != null) {
							String message = getString(R.string.airline_processing_fee_TEMPLATE,
								item.getCardFee(mBillingInfo).getFormattedMoney());
							updateCardMessageText(message);
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

				if (getLob() == LineOfBusiness.HOTELS) {
					BillingInfo mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
					if (mBillingInfo.getCardType() != null) {
						TripBucketItem item = Db.getTripBucket().getHotel();
						if (!item.isCardTypeSupported(mBillingInfo.getCardType())) {
							String message = getString(R.string.hotel_does_not_accept_cardtype_TEMPLATE, mBillingInfo
								.getCardType().getHumanReadableName(getActivity()));
							updateCardMessageText(message);
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

		setUpStoredCards();
	}

	@Override
	public void onFormClosed() {
		if (isResumed() && mFormOpen) {
			mAttemptToLeaveMade = false;
			Db.getWorkingBillingInfoManager().deleteWorkingBillingInfoFile(getActivity());
			mListener.onCheckoutDataUpdated();
			if (mStoredCardPopup != null) {
				mStoredCardPopup.dismiss();
				mStoredCardPopup.setAdapter(null);
			}
		}
		mFormOpen = false;
	}

	@Override
	public void onFormOpened() {
		setUpStoredCards();
		if (Db.getBillingInfo().hasStoredCard()) {
			if (Db.getBillingInfo().getStoredCard().isGoogleWallet()) {
				showStoredCardContainerGoogleWallet();
			}
			else {
				showStoredCardContainer();
			}
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

	public boolean isFormOpen() {
		return mFormOpen;
	}

	public void updateCardMessageText(String message) {
		if (message != null) {
			mCreditCardMessageTv.setText(Html.fromHtml(message));
		}
	}

	/**
	 * Hide the card message OR display a default message.
	 * Some POSes have messages like "Dont use debit cards" that need to display all the time.
	 *
	 * @param animate
	 */
	public void hideCardMessageOrDisplayDefault(boolean animate) {
		if (PointOfSale.getPointOfSale().doesNotAcceptDebitCardsForFlights()) {
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
	 *
	 * @param show
	 * @param animate
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
				mCreditCardMessageTv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
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

	private ListPopupWindow mStoredCardPopup;

	private void setUpStoredCards() {
		int count = mStoredCreditCardAdapter.getCount();
		clearExtraHeadingView();
		// If there is already a stored credit card, set the Working Billing Info to have it.
		if (Db.getBillingInfo().getStoredCard() != null) {
			Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(Db.getBillingInfo().getStoredCard());
		}
		if (count != 0) {
			TextView storedCardButton = Ui.inflate(getParentFragment(), R.layout.include_stored_card_spinner, null);
			storedCardButton.setOnClickListener(mStoredCardButtonClickListener);
			attachExtraHeadingView(storedCardButton);
		}
	}

	private void showStoredCardContainer() {
		StoredCreditCard card = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard();
		String cardName = card.getDescription();
		CreditCardType cardType = card.getType();
		showStoredCardContainer(cardName, cardType);
	}

	private void showStoredCardContainerGoogleWallet() {
		String cardName = getString(R.string.google_wallet);
		CreditCardType cardType = CreditCardType.GOOGLE_WALLET;
		showStoredCardContainer(cardName, cardType);
	}

	private void showStoredCardContainer(String cardName, CreditCardType cardType) {
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
				Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
				Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setLocation(new Location());

				//This is a special case, and it maybe deserves some more consideration. It is here to improve the following
				//use case: A user has a stored card selected, they open the payment form, they hit the X button and remove the
				//stored credit card, they start to enter in a CC number manually. At this point what should happen if
				//they hit the back button? Typically we don't commit the changes until the user hits the Done button
				//, however, it feels wrong to move back to the overview screen with the saved card back in the picture
				//after they had removed it completely and started entering a card # manually. So we clear the Db version.
				Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();

				setUpStoredCards();
				showNewCardContainer();
			}
		});
	}

	private void showNewCardContainer() {
		Ui.findView(getParentFragment().getActivity(), R.id.stored_card_container).setVisibility(View.GONE);
		Ui.findView(getParentFragment().getActivity(), R.id.new_card_container).setVisibility(View.VISIBLE);
		mSectionBillingInfo.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
	}

	private OnClickListener mStoredCardButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (mStoredCardPopup == null) {
				mStoredCardPopup = new ListPopupWindow(getActivity());
				mStoredCardPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
						StoredCreditCard card = mStoredCreditCardAdapter.getItem(position);
						if (card != null) {
							Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
							// Don't allow selection of invalid card types.

							boolean isValidCard = true;
							if (getLob() == LineOfBusiness.FLIGHTS &&
								!Db.getTripBucket().getFlight().isCardTypeSupported(card.getType())) {
								isValidCard = false;
							}
							if (getLob() == LineOfBusiness.HOTELS &&
								!Db.getTripBucket().getHotel().isCardTypeSupported(card.getType())) {
								isValidCard = false;
							}

							if (isValidCard) {
								Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(card);
								commitAndLeave();
								showStoredCardContainer();
								mStoredCardPopup.dismiss();
							}
						}
					}
				});
			}
			mStoredCardPopup.setAnchorView(view);
			mStoredCardPopup.setAdapter(mStoredCreditCardAdapter);
			mStoredCardPopup.setContentWidth(measureContentWidth(mStoredCreditCardAdapter));
			mStoredCardPopup.show();
		}
	};

	// Copied from AOSP, ListPopupWindow.java
	private int measureContentWidth(ListAdapter adapter) {
		// Menus don't tend to be long, so this is more sane than it looks.
		int width = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}
			if (mMeasureParent == null) {
				mMeasureParent = new FrameLayout(getActivity());
			}
			itemView = adapter.getView(i, itemView, mMeasureParent);
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}
		return width + 32;
	}

}
