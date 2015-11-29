package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.section.StoredCreditCardSpinnerAdapter;
import com.expedia.bookings.tracking.HotelV2Tracking;
import com.expedia.bookings.utils.BookingInfoUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PaymentButton extends LinearLayout {
	private ListPopupWindow mStoredCardPopup;
	private StoredCreditCardSpinnerAdapter mStoredCreditCardAdapter;

	public PaymentButton(Context context) {
		super(context);
	}

	public PaymentButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PaymentButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private LineOfBusiness lineOfBusiness;

	@InjectView(R.id.select_payment_button)
	Button selectPayment;

	IPaymentButtonListener mPaymentButtonListener;

	@OnClick(R.id.select_payment_button)
	public void onShowTraveler() {
		showStoredCards();
	}

	public interface IPaymentButtonListener {
		void onAddNewCreditCardSelected();

		void onStoredCreditCardChosen(StoredCreditCard card);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.checkout_payment_button, this);
		ButterKnife.inject(this);
	}

	public void bind() {
		mStoredCreditCardAdapter = new StoredCreditCardSpinnerAdapter(getContext(),
			Db.getTripBucket().getItem(lineOfBusiness), false);
	}

	public void setPaymentButtonListener(IPaymentButtonListener listener) {
		this.mPaymentButtonListener = listener;
	}

	public void showStoredCards() {
		if (mStoredCardPopup == null) {
			mStoredCardPopup = new ListPopupWindow(getContext());
			mStoredCardPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
					// Todo - Commenting this code temporarily. Since for cars MVP we don't support "Add new CC" option. When we do want to add it back just uncomment this.
					/*if (position == mStoredCreditCardAdapter.getCount() - 1) {
						mPaymentButtonListener.onAddNewCreditCardSelected();
						mStoredCardPopup.dismiss();
						return;
					}
					else if (position == 0) {
						return;
					}*/
					if (position == 0) {
						return;
					}
					StoredCreditCard card = mStoredCreditCardAdapter.getItem(position);
					if (card != null && card.isSelectable()) {

						// Don't allow selection of invalid card types.
						boolean isValidCard = Db.getTripBucket().getItem(lineOfBusiness)
							.isCardTypeSupported(card.getType());

						if (isValidCard) {
							Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
							StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
							if (currentCC != null) {
								BookingInfoUtils.resetPreviousCreditCardSelectState(getContext(), currentCC);
							}
							Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(card);
							Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
							mStoredCardPopup.dismiss();
							mPaymentButtonListener.onStoredCreditCardChosen(card);
							if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
								new HotelV2Tracking().trackHotelV2StoredCardSelect();
							}
						}
					}
				}
			});
		}
		mStoredCardPopup.setAnchorView(selectPayment);
		mStoredCardPopup.setAdapter(mStoredCreditCardAdapter);
		mStoredCardPopup.show();
	}

	public void dismissPopup() {
		if (mStoredCardPopup != null) {
			mStoredCardPopup.dismiss();
		}
	}

	public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
	}
}
