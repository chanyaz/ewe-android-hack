package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.CreditCardInputSection;
import com.expedia.bookings.section.CreditCardInputSection.CreditCardInputListener;
import com.expedia.bookings.section.CreditCardSection;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CVVEntryWidget extends FrameLayout implements CreditCardInputListener {

	public static final String TAG = CVVEntryWidget.class.getName();

	private CreditCardSection mCreditCardSection;

	private CVVTextView mCVVTextView;
	private TextView mCVVPromptTextView;
	private TextView mCVVSubpromptTextView;
	private View mBookButton;
	private MaskView mCVVMaskView;

	private CreditCardInputSection mCreditCardInputSection;

	private CVVEntryFragmentListener mListener;

	private int mMinCvvLen;

	public CVVEntryWidget(Context context) {
		super(context);
	}

	public CVVEntryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CVVEntryWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@InjectView(R.id.cvv_toolbar)
	Toolbar toolbar;

	@InjectView(R.id.main_container)
	RelativeLayout mainContainer;

	public void setCVVEntryListener(CVVEntryFragmentListener listener) {
		mListener = listener;
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View v = inflater.inflate(R.layout.cvv_entry_widget, this);
		ButterKnife.inject(this);

		Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp).mutate();
		drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(drawable);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		toolbar.setTitle(getContext().getString(R.string.Finish_Booking));
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance);
		toolbar.setBackgroundColor(Ui.obtainThemeColor(getContext(), R.attr.primary_color));

		// Cache views
		mCreditCardSection = Ui.findView(v, R.id.credit_card_section);
		mCVVTextView = Ui.findView(v, R.id.cvv_text_view);
		mCreditCardInputSection = Ui.findView(v, R.id.credit_card_input_section);
		mCVVPromptTextView = Ui.findView(v, R.id.cvv_prompt_text_view);
		mCVVSubpromptTextView = Ui.findView(v, R.id.cvv_subprompt_text_view);
		mBookButton = Ui.findView(v, R.id.finish_booking_button);
		mCVVMaskView = Ui.findView(v, R.id.mask_cvv_widget);

		// Set this up to listen to the credit card IME
		mCreditCardInputSection.setListener(this);

		// Setup a listener for the finish booking button
		if (mBookButton != null) {
			mBookButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mListener.onBook(mCVVTextView.getCvv());
				}
			});
		}

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = Ui.obtainThemeColor(getContext(), R.attr.primary_color);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			addView(Ui.setUpStatusBar(getContext(), toolbar, mainContainer, color));
		}

	}

	private void syncBookButtonState() {
	    mCreditCardInputSection.setBookButtonEnabled(mCVVTextView.getCvv().length() >= mMinCvvLen);
		if (mBookButton != null) {
			mBookButton.setEnabled(mCVVTextView.getCvv().length() >= mMinCvvLen);
		}
	}

	private void syncCVVTextFilter() {
		if (mCVVTextView != null) {
			InputFilter[] filters = new InputFilter[1];
			if (getCurrentCCType() == CreditCardType.AMERICAN_EXPRESS) {
				filters[0] = new InputFilter.LengthFilter(4);
			}
			else {
				filters[0] = new InputFilter.LengthFilter(3);
			}
			mCVVTextView.setFilters(filters);
		}

	}

	public void resetCVVText() {
		if (mCVVTextView != null) {
			mCVVTextView.setText("");
		}
		syncCVVTextFilter();
	}

	private CreditCardType getCurrentCCType() {
		StoredCreditCard cc = Db.getBillingInfo().getStoredCard();
		if (cc != null) {
			return cc.getType();
		}
		else if (Db.getBillingInfo().getNumber() != null) {
			String ccNumber = Db.getBillingInfo().getNumber();
			return CurrencyUtils.detectCreditCardBrand(ccNumber);
		}
		else {
			return CreditCardType.UNKNOWN;
		}
	}

	public void setCvvErrorMode(boolean enabled) {
		mCVVTextView.setCvvErrorMode(enabled);

		updateActionBar();
	}

	public void bind(BillingInfo billingInfo) {

		CreditCardType cardType = getCurrentCCType();

		String personFirstInitial = null;
		String personLastName = null;
		String cardNumber = null;
		String cardName = null;
		if (billingInfo.getStoredCard() != null) {
			StoredCreditCard storedCard = billingInfo.getStoredCard();
			Traveler traveler = Db.getTravelers().get(0);
			personFirstInitial = getFirstCharacter(traveler.getFirstName());
			personLastName = traveler.getLastName();
			cardNumber = storedCard.getCardNumber();
			cardName = storedCard.getDescription();
		}
		else if (billingInfo.getNumber() != null && billingInfo.getNumber().length() >= 4) {
			final String nameOnCard = billingInfo.getNameOnCard();
			personFirstInitial = getFirstCharacter(nameOnCard);
			final int lastNameIndex = nameOnCard.indexOf(' ') + 1;
			personLastName = nameOnCard.substring(lastNameIndex);
			cardNumber = billingInfo.getNumber();
			cardName = getResources().getString(R.string.card_ending_TEMPLATE, cardNumber.substring(cardNumber.length() - 4));
		}

		resetCVVText();

		mCVVPromptTextView.setText(Html.fromHtml(getResources().getString(R.string.security_code_TEMPLATE, cardName)));

		// Subprompt, i.e. "see front/back of card"
		if (mCVVSubpromptTextView != null) {
			mCVVSubpromptTextView.setText(
				cardType == CreditCardType.AMERICAN_EXPRESS
					? R.string.See_front_of_card
					: R.string.See_back_of_card
			);
		}

		StringBuilder signatureNameBuilder = new StringBuilder();
		if (!TextUtils.isEmpty(personFirstInitial)) {
			signatureNameBuilder.append(personFirstInitial);
			signatureNameBuilder.append(". ");
		}
		if (!TextUtils.isEmpty(personLastName)) {
			signatureNameBuilder.append(personLastName);
		}
		String signatureName = signatureNameBuilder.toString();
		mCreditCardSection.bind(signatureName, cardType, cardNumber);

		// A few minor UI tweaks on phone, depending on if amex
		boolean amex = cardType == CreditCardType.AMERICAN_EXPRESS;

		mCVVPromptTextView.setVisibility(amex ? View.GONE : View.VISIBLE);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCVVTextView.getLayoutParams();
		if (amex) {
			params.addRule(RelativeLayout.ALIGN_RIGHT, 0 );
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		else {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.signature_strip_frame);
			params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.signature_strip_frame);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.signature_strip_frame);
		}
		int right = amex ? R.dimen.cvv_text_view_right_margin_amex : R.dimen.cvv_text_view_right_margin_other;
		params.rightMargin = getResources().getDimensionPixelOffset(right);
		mCVVTextView.setLayoutParams(params);

		// Configuration vars that drive this fragment (on phone or tablet)
		mMinCvvLen = amex ? 4 : 3;

		syncBookButtonState();
		mCVVMaskView.invalidate();
	}

	private String getFirstCharacter(String input) {
		if (!TextUtils.isEmpty(input)) {
			return input.substring(0, 1);
		}

		return null;
	}

	// Special case for the subprompt ("see back of card"), if it's in the ActionBar (for phone)
	private void updateActionBar() {
		if (!(getContext() instanceof ActionBarActivity)) {
			return;
		}

		CreditCardType cardType = getCurrentCCType();
		toolbar.setSubtitle(
			cardType == CreditCardType.AMERICAN_EXPRESS
				? R.string.See_front_of_card
				: R.string.See_back_of_card
		);

	}

	//////////////////////////////////////////////////////////////////////////
	// CreditCardInputListener

	@Override
	public void onKeyPress(int code) {
		if (code == CreditCardInputSection.CODE_BOOK) {
			mListener.onBook(mCVVTextView.getCvv());
		}
		else {
			mCVVTextView.onKeyPress(code);
			syncBookButtonState();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// CVVEntryFragmentListener

	public interface CVVEntryFragmentListener {
		public void onBook(String cvv);
	}
}
