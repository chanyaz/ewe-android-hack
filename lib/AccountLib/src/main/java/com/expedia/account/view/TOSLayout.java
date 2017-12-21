package com.expedia.account.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.data.Db;
import com.expedia.account.data.PartialUser;
import com.expedia.account.util.Events;

public class TOSLayout extends FrameLayout {

	protected LinearLayout vTermsOfUseLayout;
	protected CheckBox vTermsOfUseCheckBox;
	protected TextView vTermsOfUseText;
	protected LinearLayout vSpamOptInLayout;
	protected CheckBox vSpamOptInCheckBox;
	protected TextView vSpamOptInText;
	protected LinearLayout vEnrollInLoyaltyLayout;
	protected CheckBox vEnrollInLoyaltyCheckBox;
	protected TextView vEnrollInLoyaltyText;
	protected Button vCreateAccountButton;

	private boolean mConfigShowSpamOptIn;
	private boolean mConfigEnableSpamByDefault;
	private boolean mConfigUserRewardsEnrollmentCheck;
	private boolean mConfigAutoEnrollUserInRewards;
	private CharSequence mTOSText;
	private CharSequence mMarketingText;
	private CharSequence mRewardsText;

	private boolean mUserModifiedSpamOptIn = false;
	private boolean mSystemModifyingSpamOptIn = false;

	protected int mCheckedColorResId = R.color.acct__tos_checkbox_checked;
	protected int mUncheckedColorResId = R.color.acct__tos_checkbox;

	public TOSLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_tos, this);

		vTermsOfUseLayout = (LinearLayout)findViewById(R.id.terms_of_use_layout);
		vTermsOfUseCheckBox = (CheckBox) findViewById(R.id.terms_of_use_checkbox);
		vTermsOfUseText = (TextView) findViewById(R.id.terms_of_use_text);
		vSpamOptInLayout = (LinearLayout)findViewById(R.id.agree_to_spam_layout);
		vSpamOptInCheckBox = (CheckBox) findViewById(R.id.agree_to_spam_checkbox);
		vSpamOptInText = (TextView) findViewById(R.id.agree_to_spam_text);
		vEnrollInLoyaltyLayout = (LinearLayout)findViewById(R.id.enroll_in_loyalty_layout);
		vEnrollInLoyaltyCheckBox = (CheckBox) findViewById(R.id.enroll_in_loyalty_checkbox);
		vEnrollInLoyaltyText = (TextView) findViewById(R.id.enroll_in_loyalty_text);
		vCreateAccountButton = (Button) findViewById(R.id.button_create_account);

		vTermsOfUseLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vTermsOfUseCheckBox.toggle();
			}
		});

		vSpamOptInLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vSpamOptInCheckBox.toggle();
			}
		});

		vEnrollInLoyaltyLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vEnrollInLoyaltyCheckBox.toggle();
			}
		});

		vTermsOfUseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
				fixupColors(vTermsOfUseCheckBox);
				vCreateAccountButton.setEnabled(checked);
				vEnrollInLoyaltyLayout.setEnabled(checked);
				vEnrollInLoyaltyCheckBox.setEnabled(checked);
				vEnrollInLoyaltyText.setEnabled(checked);
				vSpamOptInLayout.setEnabled(checked);
				vSpamOptInCheckBox.setEnabled(checked);
				vSpamOptInText.setEnabled(checked);
				refreshCheckboxContentDesc(vTermsOfUseLayout);
				fixupColors(vEnrollInLoyaltyCheckBox);
				fixupColors(vSpamOptInCheckBox);
			}
		});

		vSpamOptInCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
				if (mSystemModifyingSpamOptIn) {
					mSystemModifyingSpamOptIn = false;
				}
				else {
					mUserModifiedSpamOptIn = true;
				}
				fixupColors(vSpamOptInCheckBox);
				PartialUser user = Db.getNewUser();
				user.expediaEmailOptin = checked;
				Events.post(new Events.UserChangedSpamOptin(checked));
				refreshCheckboxContentDesc(vSpamOptInLayout);
			}
		});

		vEnrollInLoyaltyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
				fixupColors(vEnrollInLoyaltyCheckBox);
				setUserEnrollInLoyalty(checked);
				refreshCheckboxContentDesc(vEnrollInLoyaltyLayout);
			}
		});

		vCreateAccountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Events.post(new Events.TOSContinueButtonClicked());
			}
		});

		vTermsOfUseText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vTermsOfUseCheckBox.toggle();
			}
		});

		vSpamOptInText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vSpamOptInCheckBox.toggle();
			}
		});

		vEnrollInLoyaltyText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vEnrollInLoyaltyCheckBox.toggle();
			}
		});

		vSpamOptInLayout.setEnabled(vTermsOfUseCheckBox.isChecked());
		vSpamOptInCheckBox.setEnabled(vTermsOfUseCheckBox.isChecked());
		vSpamOptInText.setEnabled(vTermsOfUseCheckBox.isChecked());
		vEnrollInLoyaltyLayout.setEnabled(vTermsOfUseCheckBox.isChecked());
		vEnrollInLoyaltyCheckBox.setEnabled(vTermsOfUseCheckBox.isChecked());
		vEnrollInLoyaltyText.setEnabled(vTermsOfUseCheckBox.isChecked());
		vCreateAccountButton.setEnabled(vTermsOfUseCheckBox.isChecked());
	}

	protected void refreshCheckboxContentDesc(LinearLayout checkboxContainer) {
		CheckBox checkbox = (CheckBox)checkboxContainer.getChildAt(0);
		TextView textview = (TextView)checkboxContainer.getChildAt(1);
		String text = textview.getText().toString();
		if (checkbox.isChecked()) {
			text += " " + (getContext().getString(R.string.acct__cont_desc_role_checkbox_checked));
		}
		else {
			text +=  " " + (getContext().getString(R.string.acct__cont_desc_role_checkbox_unchecked));
		}
		checkboxContainer.setContentDescription(text);
	}

	public void styleizeFromAccountView(TypedArray a) {
		if (a.hasValue(R.styleable.acct__AccountView_acct__sign_in_button_background_drawable)) {
			vCreateAccountButton.setBackground(a.getDrawable(
				R.styleable.acct__AccountView_acct__sign_in_button_background_drawable));
		}
		if (a.hasValue(R.styleable.acct__AccountView_acct__sign_in_button_text_color)) {
			vCreateAccountButton.setTextColor(a.getColorStateList(R.styleable.acct__AccountView_acct__sign_in_button_text_color));
		}
	}

	public void configurePOS(boolean showSpamOptIn, boolean enableSpamByDefault, boolean hasUserRewardsEnrollmentCheck, boolean shouldAutoEnrollUserInRewards,
		CharSequence tosText, CharSequence marketingText, CharSequence rewardsText) {

		mConfigShowSpamOptIn = showSpamOptIn;
		mConfigEnableSpamByDefault = enableSpamByDefault;
		mTOSText = tosText;
		mMarketingText = marketingText;
		mConfigUserRewardsEnrollmentCheck = hasUserRewardsEnrollmentCheck;
		mRewardsText = rewardsText;
		mConfigAutoEnrollUserInRewards = shouldAutoEnrollUserInRewards;

		vTermsOfUseText.setText(mTOSText);
		vTermsOfUseText.setMovementMethod(containsLinks(mTOSText) ? LinkMovementMethod.getInstance() : null);

		vSpamOptInLayout.setVisibility(mConfigShowSpamOptIn ? View.VISIBLE : View.GONE);
		if (!mUserModifiedSpamOptIn) {
			mSystemModifyingSpamOptIn = true;
			vSpamOptInCheckBox.setChecked(mConfigEnableSpamByDefault);
		}

		vSpamOptInText.setText(mMarketingText);
		vSpamOptInText.setMovementMethod(containsLinks(mMarketingText) ? LinkMovementMethod.getInstance() : null);

		refreshCheckboxContentDesc(vTermsOfUseLayout);
		refreshCheckboxContentDesc(vSpamOptInLayout);

		fixupColors(vTermsOfUseCheckBox);
		fixupColors(vSpamOptInCheckBox);

		if(mConfigUserRewardsEnrollmentCheck) {
			vEnrollInLoyaltyLayout.setVisibility(View.VISIBLE);
			vEnrollInLoyaltyText.setText(this.mRewardsText);
			vEnrollInLoyaltyText.setMovementMethod(containsLinks(this.mRewardsText) ? LinkMovementMethod.getInstance() : null);
			setUserEnrollInLoyalty(false);
			refreshCheckboxContentDesc(vEnrollInLoyaltyLayout);

			fixupColors(vEnrollInLoyaltyCheckBox);
		}
		else {
			// Set the auto enroll flag assigned for the particular POS
			setUserEnrollInLoyalty(mConfigAutoEnrollUserInRewards);
		}
	}

	private void setUserEnrollInLoyalty(boolean isUserEnrollInLoyality) {
		PartialUser user = Db.getNewUser();
		user.enrollInLoyalty = isUserEnrollInLoyality;
	}

	protected void fixupColors(CheckBox v) {
	}

	private boolean containsLinks(CharSequence text) {
		if (text == null) {
			return false;
		}

		if (!(text instanceof Spannable)) {
			return false;
		}

		Spannable spannable = (Spannable) text;
		ClickableSpan[] spans = spannable.getSpans(0, text.length(), ClickableSpan.class);

		return spans.length > 0;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putBoolean("userModifiedOptIn", mUserModifiedSpamOptIn);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mUserModifiedSpamOptIn = bundle.getBoolean("userModifiedOptIn");
			state = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(state);
	}
}
