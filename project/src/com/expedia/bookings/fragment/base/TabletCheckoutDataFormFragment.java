package com.expedia.bookings.fragment.base;

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
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.CheckoutLoginButtonsFragment;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class TabletCheckoutDataFormFragment extends LobableFragment
	implements CheckoutLoginButtonsFragment.ILoginStateChangedListener {

	public interface ICheckoutDataFormListener{
		public void onFormRequestingClosure(TabletCheckoutDataFormFragment caller, boolean animate);
	}

	private ViewGroup mRootC;
	private ViewGroup mFormContentC;
	private TextView mHeadingText;
	private TextView mHeadingButton;
	private TextView mCreditCardMessageTv;

	//Animation vars for the card message
	private ObjectAnimator mLastCardMessageAnimator;
	private boolean mCardMessageShowing = false;

	private ICheckoutDataFormListener mListener;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, ICheckoutDataFormListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return null;
		}

		mRootC = Ui.inflate(R.layout.fragment_tablet_checkout_data_form, container, false);
		mFormContentC = Ui.findView(mRootC, R.id.content_container);
		mHeadingText = Ui.findView(mRootC, R.id.header_tv);
		mHeadingButton = Ui.findView(mRootC, R.id.header_text_button_tv);
		mCreditCardMessageTv = Ui.findView(mRootC,R.id.credit_card_fees_message);

		setUpFormContent(mFormContentC);

		return mRootC;
	}

	public void closeForm(boolean animate){
		mListener.onFormRequestingClosure(this, animate);
	}

	public void setHeadingText(CharSequence seq) {
		if (mHeadingText != null) {
			mHeadingText.setText(seq);
		}
	}

	public void updateCardMessageText(String message) {
		if (message != null) {
			mCreditCardMessageTv.setText(Html.fromHtml(message));
		}
	}

	public void setHeadingButtonText(CharSequence seq) {
		if (mHeadingButton != null) {
			mHeadingButton.setText(seq);
		}
	}

	public void setHeadingButtonOnClick(OnClickListener listener) {
		if (mHeadingButton != null) {
			mHeadingButton.setOnClickListener(listener);
		}
	}

	public TextView getHeadingTextView() {
		return mHeadingText;
	}

	public TextView getHeadingButtonTextView() {
		return mHeadingButton;
	}

	/**
	 * Attaches a view to this data form fragment's extra_heading_container (i.e. the
	 * choose-a-stored-credit-card dropdown)
	 * @param headingView
	 */
	public void attachExtraHeadingView(View headingView) {
		ViewGroup extraHeadingContainer = Ui.findView(mRootC, R.id.extra_heading_container);
		extraHeadingContainer.addView(headingView);
	}

	public void clearExtraHeadingView() {
		ViewGroup extraHeadingContainer = Ui.findView(mRootC, R.id.extra_heading_container);
		extraHeadingContainer.removeAllViews();
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mFormContentC != null) {
			setUpFormContent(mFormContentC);
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

	///////////////////////////////////////////////////////////////////////////////////////////////
	// CheckoutLoginButtonsFragment.ILoginStateChangedListener
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onLoginStateChanged() {
		setUpFormContent(mFormContentC);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Abstract methods to be implemented by concrete children
	///////////////////////////////////////////////////////////////////////////////////////////////

	public abstract void setUpFormContent(ViewGroup formContainer);

	public abstract void onFormClosed();

	public abstract void onFormOpened();
}
