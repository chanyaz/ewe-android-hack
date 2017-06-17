package com.expedia.bookings.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.enums.MerchandiseSpam;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.hotel.HotelTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.accessibility.AccessibleEditText;
import com.expedia.util.RxKt;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class TravelerContactDetailsWidget extends ExpandableCardView implements TravelerButton.ITravelerButtonListener {

	private LineOfBusiness lineOfBusiness;

	public TravelerContactDetailsWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	//@InjectView(R.id.travelerStatusIcon)
	ContactDetailsCompletenessStatusImageView driverCheckoutStatusRightImageView;

	//@InjectView(R.id.travelerNameIcon)
	ContactInitialsImageView driverCheckoutStatusLeftImageView;

	//@InjectView(R.id.section_traveler_info_container)
	public SectionTravelerInfo sectionTravelerInfo;

	//@InjectView(R.id.enter_details_text)
	TextView enterDetailsText;

	//@InjectView(R.id.traveler_phone_text)
	TextView travelerPhoneText;

	//@InjectView(R.id.edit_first_name)
	public AccessibleEditText firstName;

	//@InjectView(R.id.edit_last_name)
	public AccessibleEditText lastName;

	//@InjectView(R.id.edit_email_address)
	public AccessibleEditText emailAddress;

	//@InjectView(R.id.edit_phone_number_country_code_spinner)
	public TelephoneSpinner phoneSpinner;

	//@InjectView(R.id.edit_phone_number)
	public AccessibleEditText phoneNumber;

	//@InjectView(R.id.traveler_contact_info_container)
	ViewGroup travelerContactInfoContainer;

	//@InjectView(R.id.traveler_button)
	TravelerButton travelerButton;

	//@InjectView(R.id.merchandise_guest_opt_checkbox)
	CheckBox merchandiseOptCheckBox;

	public Boolean emailOptIn;
	MerchandiseSpam emailOptInStatus;

	public PublishSubject<Boolean> filledIn = PublishSubject.create();
	public CompositeSubscription compositeSubscription = new CompositeSubscription();

	private UserStateManager userStateManager;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		userStateManager = Ui.getApplication(getContext()).appComponent().userStateManager();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		if (PointOfSale.getPointOfSale().showLastNameFirst()) {
			inflater.inflate(R.layout.traveler_contact_details_widget_reversed, this);
		}
		else {
			inflater.inflate(R.layout.traveler_contact_details_widget, this);
		}
		ButterKnife.inject(this);

		phoneSpinner.selectPOSCountry();
		travelerButton.setVisibility(GONE);
		travelerButton.setTravelButtonListener(this);
		phoneNumber.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					setExpanded(false);
					mToolbarListener.onWidgetClosed();
				}
				return false;
			}
		});
		firstName.setOnFocusChangeListener(this);
		lastName.setOnFocusChangeListener(this);
		emailAddress.setOnFocusChangeListener(this);
		phoneNumber.setOnFocusChangeListener(this);

		sectionTravelerInfo.addInvalidCharacterListener(new InvalidCharacterHelper.InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, InvalidCharacterHelper.Mode mode) {
				AppCompatActivity activity = (AppCompatActivity) getContext();
				InvalidCharacterHelper.showInvalidCharacterPopup(activity.getSupportFragmentManager(), mode);
			}
		});
		bind();
	}

	@Override
	protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == View.VISIBLE) {
			compositeSubscription = new CompositeSubscription();
			compositeSubscription.add(RxKt.subscribeTextChange(firstName, formFilledSubscriber));
			compositeSubscription.add(RxKt.subscribeTextChange(lastName, formFilledSubscriber));
			compositeSubscription.add(RxKt.subscribeTextChange(phoneNumber, formFilledSubscriber));
		}
		else {
			compositeSubscription.unsubscribe();
		}
	}

	private Observer formFilledSubscriber = new Observer<String>() {
		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(String o) {
			filledIn.onNext(isCompletelyFilled());
		}
	};

	public void setUPEMailOptCheckBox(MerchandiseSpam value) {
		emailOptInStatus = value;
		if (lineOfBusiness == LineOfBusiness.HOTELS && !userStateManager.isUserAuthenticated()) {
			if (emailOptInStatus != null) {
				switch (emailOptInStatus) {
				case ALWAYS:
					//default value
					emailOptIn = true;
					break;
				case CONSENT_TO_OPT_IN:
					//default value
					emailOptIn = false;
					merchandiseOptCheckBox.setText(Phrase.from(getContext(), R.string.hotel_checkout_merchandise_guest_opt_in_TEMPLATE).put("brand", BuildConfig.brand).format());
					initUsers();
					break;
				case CONSENT_TO_OPT_OUT:
					//default value
					emailOptIn = true;
					merchandiseOptCheckBox.setText(Phrase.from(getContext(), R.string.hotel_checkout_merchandise_guest_opt_out_TEMPLATE).put("brand", BuildConfig.brand).format());
					initUsers();
					break;
				}
			}
		}
	}

	private void initUsers() {
		merchandiseOptCheckBox.setVisibility(View.VISIBLE);
		merchandiseOptCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if (emailOptInStatus.equals(MerchandiseSpam.CONSENT_TO_OPT_IN)) {
					emailOptIn = isChecked;
				}
				else if (emailOptInStatus.equals(MerchandiseSpam.CONSENT_TO_OPT_OUT)) {
					emailOptIn = !isChecked;
				}
			}
		});
	}

	public void setEnterDetailsText(String text) {
		enterDetailsText.setText(text);
	}

	public void setEnterDetailsContentDescription(String text) {
		enterDetailsText.setContentDescription(text);
	}

	public void bind() {

		// Cases
		// User is logged in - default to primary
		// User is logged in - selected a saved traveler
		// User is logged in - entering a new traveler(but not saving it)

		// User not logged in - empty form
		// User not logged in - entering a new traveler
		boolean isLoggedIn = userStateManager.isUserAuthenticated();
		Traveler traveler = sectionTravelerInfo.getTraveler();

		if (isLoggedIn) {
			// User is logged in - default to primary
			if (traveler == null) {
				traveler = Db.getUser().getPrimaryTraveler();
			}
			Db.getWorkingTravelerManager().shiftWorkingTraveler(traveler);
			traveler.setEmail(Db.getUser().getPrimaryTraveler().getEmail());
			sectionTravelerInfo.refreshOnLoginStatusChange();
			sectionTravelerInfo.bind(traveler);
			lastName.setNextFocusRightId(phoneNumber.getId());
			lastName.setNextFocusDownId(phoneNumber.getId());
			FontCache.setTypeface(enterDetailsText, FontCache.Font.ROBOTO_MEDIUM);
			enterDetailsText.setText(traveler.getFullName());
			travelerPhoneText.setText(traveler.getPhoneNumber());
			travelerPhoneText.setVisibility(VISIBLE);
		}
		else {
			// Default
			if (traveler == null) {
				traveler = new Traveler();
			}
			sectionTravelerInfo.refreshOnLoginStatusChange();
			sectionTravelerInfo.bind(traveler);
			FontCache.setTypeface(enterDetailsText, FontCache.Font.ROBOTO_MEDIUM);
			lastName.setNextFocusRightId(emailAddress.getId());
			lastName.setNextFocusDownId(emailAddress.getId());
		}

		if (TextUtils.isEmpty(traveler.getFullName())) {
			if (lineOfBusiness == LineOfBusiness.HOTELS) {
				FontCache.setTypeface(enterDetailsText, FontCache.Font.ROBOTO_MEDIUM);
				enterDetailsText.setText(getResources().getString(R.string.checkout_hotelsv2_enter_guest_details_line1));
				travelerPhoneText.setVisibility(VISIBLE);
				travelerPhoneText.setText(getResources().getString(R.string.checkout_hotelsv2_enter_guest_details_line2));
			}
			else {
				FontCache.setTypeface(enterDetailsText, FontCache.Font.ROBOTO_REGULAR);
				enterDetailsText.setText(Ui.obtainThemeResID(getContext(), R.attr.traveler_details_text));
				travelerPhoneText.setText("");
				travelerPhoneText.setVisibility(GONE);
			}

			driverCheckoutStatusLeftImageView.setTraveler(null);
			driverCheckoutStatusLeftImageView.setStatus(ContactDetailsCompletenessStatus.DEFAULT);
			driverCheckoutStatusRightImageView.setStatus(ContactDetailsCompletenessStatus.DEFAULT);
			sectionTravelerInfo.resetValidation();
			this.setContentDescription(getTravelerContentDescriptionString(traveler, false));
			return;
		}

		// Validate
		boolean isValid = isFilled() && sectionTravelerInfo.performValidation();
		enterDetailsText.setText(traveler.getFullName());
		travelerPhoneText.setText(traveler.getPhoneNumber());
		travelerPhoneText.setVisibility(!TextUtils.isEmpty(traveler.getPhoneNumber()) ? VISIBLE : GONE);
		driverCheckoutStatusLeftImageView.setTraveler(traveler);
		driverCheckoutStatusLeftImageView.setStatus(
			isValid ? ContactDetailsCompletenessStatus.COMPLETE : ContactDetailsCompletenessStatus.INCOMPLETE);
		driverCheckoutStatusRightImageView.setStatus(
			isValid ? ContactDetailsCompletenessStatus.COMPLETE : ContactDetailsCompletenessStatus.INCOMPLETE);
		this.setContentDescription(getTravelerContentDescriptionString(traveler, isValid));
		Db.getWorkingTravelerManager().setWorkingTravelerAndBase(traveler);
	}

	@Override
	public void setExpanded(boolean expand, boolean animate) {
		super.setExpanded(expand, animate);
		travelerContactInfoContainer.setVisibility(expand ? GONE : VISIBLE);
		sectionTravelerInfo.setVisibility(expand ? VISIBLE : GONE);
		if (expand) {
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getActionBarTitle());
			}
			if (userStateManager.isUserAuthenticated()) {
				travelerButton.setVisibility(VISIBLE);
			}
			else {
				travelerButton.setVisibility(GONE);
			}
			bind();
			if (lineOfBusiness == LineOfBusiness.HOTELS) {
				HotelTracking.Companion.trackHotelCheckoutTraveler();
			}
			else {
				OmnitureTracking.trackCheckoutTraveler(lineOfBusiness);
			}

			if (PointOfSale.getPointOfSale().showLastNameFirst()) {
				lastName.requestFocus();
				Ui.showKeyboard(lastName, null);
			}
			else {
				firstName.requestFocus();
				Ui.showKeyboard(firstName, null);
			}
			filledIn.onNext(isCompletelyFilled());
		}
		else {
			bind();
			Db.getWorkingTravelerManager().commitWorkingTravelerToDB(0);
			travelerButton.dismissPopup();
		}
	}

	@Override
	public void onTravelerChosen(Traveler traveler) {
		sectionTravelerInfo.bind(traveler);
	}

	@Override
	public void onAddNewTravelerSelected() {
		Traveler traveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		sectionTravelerInfo.bind(traveler);
	}

	@Override
	public boolean getMenuDoneButtonFocus() {
		if (phoneNumber != null) {
			return phoneNumber.hasFocus();
		}
		return false;
	}

	@Override
	public String getMenuButtonTitle() {
		return getResources().getString(R.string.Done);
	}

	@Override
	public String getActionBarTitle() {
		if (lineOfBusiness == LineOfBusiness.HOTELS) {
			return getResources().getString(R.string.checkout_hotelsv2_enter_guest_details_line1);
		}
		else {
			return getResources().getString(Ui.obtainThemeResID(getContext(), R.attr.traveler_toolbar_text));
		}
	}

	@Override
	public void onMenuButtonPressed() {
		int numberOfInvalidFields = sectionTravelerInfo.getNumberOfInvalidFields();
		if (sectionTravelerInfo.performValidation() && numberOfInvalidFields == 0) {
			setExpanded(false);
		}
		else {
			Ui.hideKeyboard(sectionTravelerInfo);
			StringBuilder numberOfErrorsPleaseReviewString = new StringBuilder();
			numberOfErrorsPleaseReviewString.append(Phrase.from(getContext().getResources().
				getQuantityString(R.plurals.number_of_errors_TEMPLATE, numberOfInvalidFields))
				.put("number", numberOfInvalidFields)
				.format()
				.toString())
				.append(" ")
				.append(getContext().getString(R.string.accessibility_announcement_please_review_and_resubmit));
			announceForAccessibility(numberOfErrorsPleaseReviewString);
		}
	}

	@Override
	public void onLogin() {
		sectionTravelerInfo.bind(null);
		setExpanded(false);
	}

	@Override
	public void onLogout() {
		sectionTravelerInfo.bind(new Traveler());
		setExpanded(false);
	}

	@Override
	public boolean isComplete() {
		return isFilled() && sectionTravelerInfo.performValidation();
	}

	public boolean isFilled() {
		return !firstName.getText().toString().isEmpty() || !lastName.getText().toString().isEmpty() || !phoneNumber.getText().toString().isEmpty();
	}

	public boolean isCompletelyFilled() {
		return !firstName.getText().toString().isEmpty() &&
			!lastName.getText().toString().isEmpty() &&
			!phoneNumber.getText().toString().isEmpty();
	}

	public void setInvalid(String field) {
		// Error field from Cars APi is mainMobileTraveler.lastname and for LX it is lastName.
		if (field.contains("lastName")) {
			sectionTravelerInfo.setLastNameValid(false);
		}
		else if (field.contains("firstName")) {
			sectionTravelerInfo.setFirstNameValid(false);
		}
		else if (field.contains("phone")) {
			sectionTravelerInfo.setPhoneValid(false);
		}
	}

	public void setLineOfBusiness(LineOfBusiness lob) {
		lineOfBusiness = lob;
	}

	public String getTravelerContentDescriptionString(Traveler traveler, Boolean isValid) {
		String description;
		if (isValid) {
			description = Phrase.from(getContext(), R.string.traveler_details_complete_cont_desc_TEMPLATE)
				.put("title", traveler.getFullName())
				.put("subtitle", traveler.getPhoneNumber())
				.format().toString();
		}
		else {
			String title = Strings.isEmpty(traveler.getFirstName()) ? "" : traveler.getFirstName();
			description = Phrase.from(getContext(), R.string.traveler_details_incomplete_cont_desc_TEMPLATE)
				.put("title", title).format().toString();
		}
		return description;
	}
}
