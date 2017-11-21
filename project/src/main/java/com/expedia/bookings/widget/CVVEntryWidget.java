package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.CreditCardInputSection;
import com.expedia.bookings.section.CreditCardInputSection.CreditCardInputListener;
import com.expedia.bookings.section.CreditCardSection;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.larvalabs.svgandroid.widget.SVGView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CVVEntryWidget extends LinearLayout implements CreditCardInputListener {

	public static final String TAG = CVVEntryWidget.class.getName();

	public CreditCardSection mCreditCardSection;

	private CVVTextView mCVVTextView;
	private TextView mCVVPromptTextView;
	private SVGView svgAmexLogo;
	private SVGView svgAmexHead;

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
	public Toolbar toolbar;

	@InjectView(R.id.main_container)
	RelativeLayout mainContainer;

	public void setCVVEntryListener(CVVEntryFragmentListener listener) {
		mListener = listener;
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setOrientation(VERTICAL);
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
		toolbar.setNavigationContentDescription((getContext().getString(R.string.toolbar_nav_icon_cont_desc)));
		toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance);
		toolbar.setSubtitleTextAppearance(getContext(), R.style.ToolbarSubtitleTextAppearance);
		toolbar.setBackgroundColor(Ui.obtainThemeColor(getContext(), R.attr.primary_color));

		// Cache views
		mCreditCardSection = Ui.findView(v, R.id.credit_card_section);
		mCVVTextView = Ui.findView(v, R.id.cvv_text_view);
		mCreditCardInputSection = Ui.findView(v, R.id.credit_card_input_section);
		mCVVPromptTextView = Ui.findView(v, R.id.cvv_prompt_text_view);
		svgAmexLogo = Ui.findView(v, R.id.svg_amex_logo);
		svgAmexHead = Ui.findView(v, R.id.svg_amex_head);

		LayoutUtils.setSVG(svgAmexLogo, R.raw.american_express_logo );
		LayoutUtils.setSVG(svgAmexHead, R.raw.ic_amex_head );

		// Set this up to listen to the credit card IME
		mCreditCardInputSection.setListener(this);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = Ui.obtainThemeColor(getContext(), R.attr.primary_color);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			addView(Ui.setUpStatusBar(getContext(), toolbar, null, color));
		}

		updateActionBar();

	}

	private void syncBookButtonState() {
		enableBookButton(mCVVTextView.getCvv().length() >= mMinCvvLen);
	}

	private void syncCVVTextFilter() {
		if (mCVVTextView != null) {
			InputFilter[] filters = new InputFilter[1];
			if (getCurrentCCType() == PaymentType.CARD_AMERICAN_EXPRESS) {
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

	private PaymentType getCurrentCCType() {
		StoredCreditCard cc = Db.getBillingInfo().getStoredCard();
		if (cc != null) {
			return cc.getType();
		}
		else if (Db.getBillingInfo().getNumber() != null) {
			String ccNumber = Db.getBillingInfo().getNumber();
			return CurrencyUtils.detectCreditCardBrand(ccNumber, getContext());
		}
		else {
			return PaymentType.CARD_UNKNOWN;
		}
	}

	public void setCvvErrorMode(boolean enabled) {
		mCVVTextView.setCvvErrorMode(enabled);
	}

	public void bind(BillingInfo billingInfo) {

		PaymentType cardType = getCurrentCCType();

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

		mCVVPromptTextView.setText(
			HtmlCompat.fromHtml(getResources().getString(R.string.security_code_TEMPLATE, cardName)));

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
		boolean amex = cardType == PaymentType.CARD_AMERICAN_EXPRESS;

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
	}

	private String getFirstCharacter(String input) {
		if (!TextUtils.isEmpty(input)) {
			return input.substring(0, 1);
		}

		return null;
	}

	// Special case for the subprompt ("see back of card"), if it's in the ActionBar (for phone)
	private void updateActionBar() {
		if (!(getContext() instanceof AppCompatActivity)) {
			return;
		}

		toolbar.setSubtitle(R.string.cvv_enter_security_code);
	}

	public void enableBookButton(boolean enabled) {
		mCreditCardInputSection.setBookButtonEnabled(enabled);
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
		void onBook(String cvv);
	}
}
