package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.section.StoredCreditCardSpinnerAdapter;
import com.expedia.bookings.tracking.HotelTracking;
import com.expedia.bookings.utils.BookingInfoUtils;

import butterknife.ButterKnife;

public class StoredCreditCardList extends LinearLayout {
	private ListView mStoredCardList;
	private StoredCreditCardSpinnerAdapter mStoredCreditCardAdapter;

	public StoredCreditCardList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private LineOfBusiness lineOfBusiness;

	IStoredCreditCardListener mStoredCreditCardListener;

	public interface IStoredCreditCardListener {
		void onStoredCreditCardChosen(StoredCreditCard card);

		void onTemporarySavedCreditCardChosen(BillingInfo info);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.stored_credit_card_list, this);
		ButterKnife.inject(this);
		mStoredCardList = (ListView) findViewById(R.id.stored_card_list);
		// Defect 7359 we should clear any billing info which is saved earlier.
		Db.getWorkingBillingInfoManager().clearWorkingBillingInfo();
		mStoredCardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				StoredCreditCard card = mStoredCreditCardAdapter.getItem(position);
				if (mStoredCreditCardAdapter.isTemporarilySavedCard(position)) {
					if (Db.getTemporarilySavedCard().getSaveCardToExpediaAccount()) {
						return;
					}
					mStoredCreditCardListener.onTemporarySavedCreditCardChosen(Db.getTemporarilySavedCard());
					setStatusForStoredCards(position);
				}
				else if (card != null && card.isSelectable()) {
					// Don't allow selection of invalid card types.
					boolean isValidCard = Db.getTripBucket().getItem(lineOfBusiness)
						.isPaymentTypeSupported(card.getType());

					if (!isValidCard) {
						return;
					}
					Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
					StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
					if (currentCC != null) {
						BookingInfoUtils.resetPreviousCreditCardSelectState(getContext(), currentCC);
					}
					setStatusForStoredCards(position);
					Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(card);
					Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();

					mStoredCreditCardListener.onStoredCreditCardChosen(card);
					if (lineOfBusiness == LineOfBusiness.HOTELSV2) {
						new HotelTracking().trackHotelStoredCardSelect();
					}
				}
			}
		});
		mStoredCreditCardAdapter = new StoredCreditCardSpinnerAdapter(getContext(),
				Db.getTripBucket().getItem(lineOfBusiness));
		mStoredCardList.setAdapter(mStoredCreditCardAdapter);
	}

	public void bind() {
		mStoredCreditCardAdapter.refresh(Db.getTripBucket().getItem(lineOfBusiness));
		setListViewHeightBasedOnChildren(mStoredCardList);
	}

	public void setStatusForStoredCards(int position) {
		if (mStoredCardList == null) {
			return;
		}
		for (int i = 0; i < mStoredCardList.getCount(); i++) {
			ContactDetailsCompletenessStatusImageView statusIcon = (ContactDetailsCompletenessStatusImageView) mStoredCardList
				.getChildAt(i).findViewById(R.id.card_info_status_icon);
			statusIcon.setStatus(
				i == position ? ContactDetailsCompletenessStatus.COMPLETE : ContactDetailsCompletenessStatus.DEFAULT);
		}
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public void setStoredCreditCardListener(IStoredCreditCardListener listener) {
		this.mStoredCreditCardListener = listener;
	}

	public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
	}
}
