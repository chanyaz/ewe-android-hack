package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.widget.NavigationButton;
import com.expedia.bookings.widget.NavigationDropdownAdapter;
import com.expedia.bookings.widget.NavigationDropdownAdapter.NoOpButton;
import com.mobiata.android.util.Ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FlightPaymentOptionsActivity extends SherlockFragmentActivity {

	SectionLocation mSectionCurrentBillingAddress;
	SectionBillingInfo mSectionCurrentCreditCard;
	SectionStoredCreditCard mSectionStoredPayment;
	View mNewCreditCardBtn;

	TextView mStoredPaymentsLabel;
	TextView mCurrentPaymentLabel;
	TextView mNewPaymentLabel;
	ViewGroup mCurrentPaymentContainer;
	ViewGroup mStoredCardsContainer;
	ViewGroup mStoredPaymentContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_options);

		mSectionCurrentBillingAddress = Ui.findView(this, R.id.current_payment_address_section);
		mSectionCurrentCreditCard = Ui.findView(this, R.id.current_payment_cc_section);
		mSectionStoredPayment = Ui.findView(this, R.id.stored_creditcard_section);

		mStoredPaymentsLabel = Ui.findView(this, R.id.stored_payments_label);
		mCurrentPaymentLabel = Ui.findView(this, R.id.current_payment_label);
		mNewPaymentLabel = Ui.findView(this, R.id.new_payment_label);
		mCurrentPaymentContainer = Ui.findView(this, R.id.current_payment_container);
		mStoredCardsContainer = Ui.findView(this, R.id.new_payment_stored_cards);
		mStoredPaymentContainer = Ui.findView(this, R.id.stored_payment_container);

		mNewCreditCardBtn = Ui.findView(this, R.id.new_payment_new_card);

		mNewCreditCardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Db.getBillingInfo().setStoredCard(null);
				Intent intent = new Intent(FlightPaymentOptionsActivity.this, FlightPaymentAddressActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightPaymentCreditCardActivity.class);
				//TODO:Add email activity...
				yoyo.addYoYoTrick(FlightCheckoutActivity.class);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});

		mSectionCurrentBillingAddress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightPaymentOptionsActivity.this, FlightPaymentAddressActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightPaymentOptionsActivity.class);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});

		mSectionCurrentCreditCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightPaymentOptionsActivity.this, FlightPaymentCreditCardActivity.class);
				YoYo yoyo = new YoYo();
				yoyo.addYoYoTrick(FlightPaymentOptionsActivity.class);
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				startActivity(intent);
			}
		});


		List<StoredCreditCard> cards = new ArrayList<StoredCreditCard>();

		//Populate stored creditcard list
		if (User.isLoggedIn(this) && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
			cards = Db.getUser().getStoredCreditCards();
		}

		if (cards != null && cards.size() > 0) {
			//Inflate stored cards
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			Resources res = getResources();
			for (int i = 0; i < cards.size(); i++) {
				final StoredCreditCard storedCard = cards.get(i);
				SectionStoredCreditCard card = (SectionStoredCreditCard) inflater.inflate(
						R.layout.section_display_stored_credit_card, null);
				card.bind(cards.get(i));
				card.setPadding(0, 5, 0, (i == cards.size() - 1) ? 10 : 5);
				card.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Db.getBillingInfo().setStoredCard(storedCard);
						Intent intent = new Intent(FlightPaymentOptionsActivity.this, FlightCheckoutActivity.class);
						startActivity(intent);
					}
				});

				//Add dividers
				if (i != 0) {
					View divider = new View(this);
					LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_grey_divider_height));
					divLayoutParams.setMargins(0, res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_top), 0,
							res.getDimensionPixelSize(R.dimen.simple_grey_divider_margin_bottom));
					divider.setLayoutParams(divLayoutParams);
					divider.setBackgroundColor(res.getColor(R.color.divider_grey));
					mStoredCardsContainer.addView(divider);
				}
				mStoredCardsContainer.addView(card);
			}

		}

		//Set visibilities
		boolean hasAccountCards = cards != null && cards.size() > 0;
		boolean hasSelectedStoredCard = Db.getBillingInfo().getStoredCard() != null;
		boolean hasValidNewCard = !TextUtils.isEmpty(Db.getBillingInfo().getNumber());
		mCurrentPaymentLabel.setVisibility(hasSelectedStoredCard || hasValidNewCard ? View.VISIBLE : View.GONE);
		mStoredPaymentContainer.setVisibility(hasSelectedStoredCard ? View.VISIBLE : View.GONE);
		mCurrentPaymentContainer.setVisibility(!hasSelectedStoredCard && hasValidNewCard ? View.VISIBLE : View.GONE);
		mNewPaymentLabel
				.setText(hasSelectedStoredCard || hasValidNewCard ? getString(R.string.or_select_new_paymet_method)
						: getString(R.string.select_payment));
		mStoredPaymentsLabel.setVisibility(hasAccountCards ? View.VISIBLE : View.GONE);

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		NavigationButton nb = NavigationButton.createNewInstanceAndAttach(this, R.drawable.icon, actionBar);
		nb.setDropdownAdapter(new NavigationDropdownAdapter(this,NoOpButton.FLIGHTS));
		nb.setTitle(this.getTitle());
		
	}

	@Override
	public void onResume() {
		super.onResume();

		BillingInfo mBillingInfo = Db.getBillingInfo();
		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mSectionCurrentBillingAddress.bind(mBillingInfo.getLocation());
		mSectionCurrentCreditCard.bind(mBillingInfo);
		mSectionStoredPayment.bind(mBillingInfo.getStoredCard());

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_done, menu);
		menu.findItem(R.id.menu_yoyo).getActionView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightPaymentOptionsActivity.this, FlightCheckoutActivity.class);
				startActivity(intent);
			}		
		});
		return true;
	}

}
